# Build stage
FROM openjdk:11-jdk as builder

# install node
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get install -yq nodejs build-essential && \
    npm install -g npm && \
    npm install -g yarn
# Headless Chrome dependencies
RUN apt-get install -yq \
    ca-certificates fonts-liberation libappindicator3-1 libasound2 \
    libatk-bridge2.0-0 libatk1.0-0 libc6 libcairo2 libcups2 libdbus-1-3 \
    libexpat1 libfontconfig1 libgbm1 libgcc1 libglib2.0-0 libgtk-3-0 libnspr4 \
    libnss3 libpango-1.0-0 libpangocairo-1.0-0 libstdc++6 libx11-6 \
    libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 libxext6 \
    libxfixes3 libxi6 libxrandr2 libxrender1 libxss1 libxtst6 lsb-release \
    wget xdg-utils

# installing the node and java packages before adding the src directory
# will allow us to re-use these image layers when only the souce code changes
WORKDIR /app

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.project.prod=true"

COPY package.json yarn.lock /app/
RUN yarn install

COPY gradlew /app/
COPY gradle/wrapper gradle/wrapper
RUN ./gradlew --version

COPY gradle gradle
COPY build.gradle gradle.properties settings.gradle /app/
COPY radar-auth/build.gradle radar-auth/
COPY radar-auth/deprecated-auth0/build.gradle radar-auth/deprecated-auth0/
COPY oauth-client-util/build.gradle oauth-client-util/

RUN ./gradlew downloadDependencies :radar-auth:shadowJar

# now we copy our application source code and build it

COPY angular.json proxy.conf.json tsconfig.app.json \
    tsconfig.spec.json tsconfig.json tslint.json /app/
COPY webpack webpack

COPY radar-auth radar-auth
COPY src src
RUN ./gradlew -s bootWar

# Run stage
FROM openjdk:11-jre-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0

# Add the war and changelogs files from build stage
COPY --from=builder /app/build/libs/*.war /app.war
COPY --from=builder /app/src/main/docker/etc /mp-includes

EXPOSE 8080 5701/udp
CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom \
        -cp /mp-includes:/app.war org.springframework.boot.loader.WarLauncher
