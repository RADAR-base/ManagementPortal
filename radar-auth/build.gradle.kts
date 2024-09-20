plugins {
    id("project-conventions")
}

description = "Library for authentication and authorization of JWT tokens issued by the RADAR platform"

dependencies {
    api(libs.auth0.jwt)
    api(platform(libs.kotlin.coroutines.bom))
    api(libs.kotlin.coroutines.core)

    implementation(libs.radar.commons.kotlin)

    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.slf4j.api)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.wiremock)
    testImplementation(libs.hamcrest)

    testRuntimeOnly(libs.logback.classic)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

radarKotlin {
    slf4jVersion.set(libs.versions.slf4j)
}

tasks.test {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    useJUnitPlatform()
}

tasks.register<Copy>("ghPagesJavadoc") {
    from("${layout.buildDirectory}/dokka/javadoc")
    into("$rootDir/public/radar-auth-javadoc")
    dependsOn(tasks.named("dokkaJavadoc"))
}

idea {
    module {
        isDownloadSources = true
    }
}
