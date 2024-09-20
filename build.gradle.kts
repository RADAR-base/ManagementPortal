plugins {
    id("project-conventions")
    id("org.radarbase.radar-root-project")
    id("org.jetbrains.kotlin.kapt")

    alias(libs.plugins.kotlin.allopen)

    application
    war

    alias(libs.plugins.undercouch.download)
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

radarRootProject {
    projectVersion.set(properties["projectVersion"] as String)
}

repositories {
    mavenCentral()
    // This repo can be used for local testing and development.
    // Keep commented when making a PR.
//    mavenLocal()
    // This repo is used temporarily when we use an internal SNAPSHOT library version.
    // When we publish the library, we comment out the repository again to speed up dependency resolution.
//        maven { url = "https://oss.sonatype.org/content/repositories/snapshots" }
}

description = "MangementPortal application to manage studies and participants"

dependencies {
    // Project dependencies
    implementation(project(":radar-auth"))
    implementation(project(":managementportal-client"))

    // Radar dependencies
    implementation(libs.radar.commons.kotlin)

    // Versions are determined by Spring Boot dependency-management plugin
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.hibernate", module = "hibernate-entitymanager")
    }
    implementation("org.springframework.security:spring-security-data")
    implementation("org.springframework:spring-context-support")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("org.springframework.boot:spring-boot-test")
    implementation("org.springframework.session:spring-session-hazelcast")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.data:spring-data-envers")
    implementation("org.hibernate:hibernate-envers")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("org.hamcrest:hamcrest-library")

    // Other
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(platform(libs.kotlin.coroutines.bom))
    implementation(libs.kotlin.coroutines.reactor)

    implementation(libs.kotlin.reflect)
    implementation(libs.spring.security.oauth)
    implementation(libs.springdoc.openapi)
    implementation(libs.jhipster.framework)
    implementation(libs.jhipster.dependencies)
    implementation(libs.hazelcast)
    implementation(libs.hazelcast.spring)
    implementation(libs.hibernate.validator)
    runtimeOnly(libs.hazelcast.hybernate53)
    runtimeOnly(libs.hikari.cp)
    runtimeOnly(libs.postgresql)
    implementation(libs.swagger.annotations)
    runtimeOnly(libs.javax.activation)
    runtimeOnly(libs.javax.inject)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.liquibase.slf4j)
    implementation(libs.google.findbugs)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.wiremock)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.hsqldb)
    testRuntimeOnly(libs.slf4j.simple)
    testImplementation(libs.testcontainers)
}

ext {
    // This is needed to prevent conflict with junit version provided by root project...
    set("junit-jupiter.version", "5.10.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        suppressWarnings = true
    }
}

allOpen {
    annotation("org.springframework.stereotype.Component")
    annotation("org.springframework.boot.test.context.SpringBootTest")
    annotation("org.springframework.web.bind.annotation.RestController")
}

dependencyManagement {
    imports {
        mavenBom("com.fasterxml.jackson:jackson-bom:${libs.versions.jackson.get()}")
        mavenBom("org.springframework:spring-framework-bom:${libs.versions.springFramework.get()}")
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
        mavenBom("org.springframework.data:spring-data-bom:${libs.versions.springData.get()}")
        mavenBom("org.springframework.session:spring-session-bom:${libs.versions.springSession.get()}")
    }
    // Prevents loading of detachedConfigurations that takes a lot of time; remove after development?
    applyMavenExclusions(false)
}

radarKotlin {
    slf4jVersion.set(libs.versions.slf4j)
}

defaultTasks("bootRun")

configurations {
    compileOnly {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
}

application {
    mainClass.set("org.radarbase.management.ManagementPortalApp")
    applicationDefaultJvmArgs =
        listOf(
            "--add-modules",
            "java.se",
            "--add-exports",
            "java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.nio=ALL-UNNAMED",
            "--add-opens",
            "java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens",
            "java.management/sun.management=ALL-UNNAMED",
            "--add-opens",
            "jdk.management/com.sun.management.internal=ALL-UNNAMED",
        )
}

springBoot {
    mainClass = "org.radarbase.management.ManagementPortalApp"
    buildInfo()
}

tasks.bootWar {
    launchScript()
}

tasks.bootRun {
    sourceResources(sourceSets.main.get())
}

tasks.test {
    jvmArgs =
        listOf(
            "--add-modules",
            "java.se",
            "--add-exports",
            "java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",
            "--add-opens",
            "java.base/java.nio=ALL-UNNAMED",
            "--add-opens",
            "java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens",
            "java.management/sun.management=ALL-UNNAMED",
            "--add-opens",
            "jdk.management/com.sun.management.internal=ALL-UNNAMED",
        )
    useJUnitPlatform()
}

apply(from = "$rootDir/gradle/liquibase.gradle")
apply(from = "$rootDir/gradle/gatling.gradle")
apply(from = "$rootDir/gradle/mapstruct.gradle")
apply(from = "$rootDir/gradle/docker.gradle")
apply(from = "$rootDir/gradle/openapi.gradle")

if (project.hasProperty("prod")) {
    apply(from = "$rootDir/gradle/profile_prod.gradle")
} else {
    apply(from = "$rootDir/gradle/profile_dev.gradle")
}

idea {
    module {
        isDownloadSources = true
    }
}

tasks.clean {
    delete("target")
}

tasks.register<Delete>("cleanResources") {
    delete("build/resources")
}

tasks.register("stage") {
    dependsOn("bootWar")
}

tasks.register<Copy>("ghPagesJavadoc") {
    copySpec {
        from(tasks.javadoc.get().destinationDir)
        into(file("$rootDir/public/management-portal-javadoc"))
    }
    dependsOn(tasks.named("javadoc"))
}

tasks.register<Copy>("ghPagesOpenApiSpec") {
    copySpec {
        from(layout.buildDirectory.dir("swagger-spec"))
        into(file("$rootDir/public/apidoc"))
    }
}

tasks.compileJava {
    dependsOn("processResources")
}

tasks.processResources {
    dependsOn("cleanResources", "bootBuildInfo")
}

tasks.named("bootBuildInfo") {
    mustRunAfter("cleanResources")
}

node {
    download = true
}
