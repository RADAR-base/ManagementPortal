plugins {
    id("project-conventions")
}

description = "Kotlin ManagementPortal client"

dependencies {
    api(libs.kotlin.stdlib)
    api(platform(libs.kotlin.coroutines.bom))

    api(platform(libs.ktor.bom))
    api(libs.ktor.client.core)
    api(libs.ktor.client.auth)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)

    implementation(libs.kotlin.reflect)
    implementation(libs.radar.commons.kotlin)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.wiremock)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.hamcrest)

    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

radarKotlin {
    slf4jVersion.set(libs.versions.slf4j)
}

tasks.register<Copy>("ghPagesJavadoc") {
    from("${layout.buildDirectory}/dokka/javadoc")
    into("$rootDir/public/managementportal-client-javadoc")
    dependsOn(tasks.named("dokkaJavadoc"))
}

tasks.test {
    useJUnitPlatform()
}

idea {
    module {
        isDownloadSources = true
    }
}
