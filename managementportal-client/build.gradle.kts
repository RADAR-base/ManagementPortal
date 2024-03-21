import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat


/*
 * Copyright (c) 2020. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
description = "Kotlin ManagementPortal client"

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.coroutines}"))
    api(platform("io.ktor:ktor-bom:${Versions.ktor}"))

    api("io.ktor:ktor-client-core")
    api("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation ("com.github.tomakehurst:wiremock:2.27.2")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation ("org.hamcrest:hamcrest:${Versions.hamcrest}")

    testRuntimeOnly ("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
}

// TODO Should this be different from the management-portal application itself?
// If not, consider moving this to the buildSrc build logic
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        apiVersion = KotlinVersion.KOTLIN_1_8
        languageVersion = KotlinVersion.KOTLIN_1_8
    }
}

tasks.register<Copy>("ghPagesJavadoc") {
    from(layout.buildDirectory.file("dokka/javadoc"))
    into(layout.buildDirectory.file("public/managementportal-client-javadoc"))
    dependsOn(tasks.dokkaJavadoc)
}

tasks.test {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
    useJUnitPlatform()
}

extra.apply {
    set("projectLanguage", "kotlin")
    set("publishToMavenCentral", "true")
}

apply(from = "$rootDir/gradle/publishing.gradle")
