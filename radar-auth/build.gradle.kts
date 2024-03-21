import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

description = "Library for authentication and authorization of JWT tokens issued by the RADAR platform"

dependencies {
    api("com.auth0:java-jwt:${Versions.oauthJwt}")
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.coroutines}"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation(platform("io.ktor:ktor-bom:${Versions.ktor}"))
    implementation("io.ktor:ktor-client-core:${Versions.ktor}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")

    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("com.github.tomakehurst:wiremock:${Versions.wiremock}")
    testImplementation("org.hamcrest:hamcrest:${Versions.hamcrest}")

    testRuntimeOnly("ch.qos.logback:logback-classic:${Versions.logback}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")

}

// TODO Should this be different from the management-portal application itself?
// If not, consider moving this to the buildSrc build logic.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        apiVersion = KotlinVersion.KOTLIN_1_8
        languageVersion = KotlinVersion.KOTLIN_1_8
    }
}

tasks.test {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
    useJUnitPlatform()
}

tasks.register<Copy>("ghPagesJavadoc") {
    from(layout.buildDirectory.file("dokka/javadoc"))
    into(layout.projectDirectory.file("public/management-auth-javadoc"))
    dependsOn(tasks.dokkaJavadoc)
}

extra.apply {
    set("projectLanguage", "kotlin")
}

apply(from = "$rootDir/gradle/style.gradle")
apply(from = "$rootDir/gradle/publishing.gradle")
