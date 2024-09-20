/*
 *
 *  *  Copyright 2024 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradle.radar.kotlin)
    implementation(libs.gradle.kotlin.jvm)
    implementation(libs.gradle.kotlin.serialization)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    // This repo can be used for local testing and development.
    // Keep commented when making a PR.
//    mavenLocal()
    // This repo is used temporarily when we use an internal SNAPSHOT library version.
    // When we publish the library, we comment out the repository again to speed up dependency resolution.
//        maven { url = "https://oss.sonatype.org/content/repositories/snapshots" }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}
