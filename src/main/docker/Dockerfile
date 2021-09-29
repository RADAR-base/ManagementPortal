FROM openjdk:11-jre-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0

# add directly the war
ADD *.war /app.war

COPY ./etc /mp-includes

EXPOSE 8080 5701/udp

CMD echo "The application will start in ${JHIPSTER_SLEEP}s..." && \
    sleep ${JHIPSTER_SLEEP} && \
    java $JAVA_OPTS \
            -Djava.security.egd=file:/dev/./urandom \
            --add-modules java.se \
            --add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
            --add-opens java.base/java.lang=ALL-UNNAMED \
            --add-opens java.base/java.nio=ALL-UNNAMED \
            --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
            --add-opens java.management/sun.management=ALL-UNNAMED \
            --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
            -cp /mp-includes:/app.war \
             org.springframework.boot.loader.WarLauncher
