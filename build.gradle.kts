import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootWar
import org.springframework.boot.gradle.tasks.run.BootRun
import org.springframework.boot.gradle.tasks.buildinfo.BuildInfo
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask


buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    application
    idea
    war
    java
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version Versions.springBoot
    id("com.github.node-gradle.node") version "3.6.0"
    id("io.spring.dependency-management") version Versions.springDependencyManagement
    id("de.undercouch.download") version "5.5.0" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.github.ben-manes.versions") version "0.47.0"
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin
    id("org.jetbrains.kotlin.kapt") version Versions.kotlin
    id("org.jetbrains.kotlin.plugin.serialization") version Versions.kotlin apply false
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jetbrains.kotlin.plugin.allopen") version Versions.kotlin
}

allprojects {
    group = "org.radarbase"
    version = "2.1.1-SNAPSHOT" // project version

    // The comment on the previous line is only there to identify the project version line easily
    // with a sed command, to auto-update the version number with the prepare-release-branch.sh
    // script, do not remove it.

    apply(plugin = "java-library")
    apply(plugin = "idea")
    apply(plugin = "org.jetbrains.dokka")

    extra.apply {
        set("githubRepoName", "RADAR-base/ManagementPortal")
        set("githubUrl", "https://github.com/RADAR-base/ManagementPortal")
        set("website", "https://radar-base.org")
    }

    repositories {
        mavenCentral()
        // This repo is used temporarily when we use an internal SNAPSHOT library version.
        // When we publish the library, we comment out the repository again to speed up dependency resolution.
        // maven { url = "https://oss.sonatype.org/content/repositories/snapshots" }
    }

    idea {
        module {
            setDownloadJavadoc(true)
            setDownloadSources(true)
        }
    }

    tasks.register("ghPages") {
        dependsOn(
            provider {
                tasks.filter { task -> task.name.startsWith("ghPages") && task.name != "ghPages" }
            }
        )
    }
}

description = "MangementPortal application to manage studies and participants"

defaultTasks = mutableListOf("bootRun")

configurations {
    compileOnly {
        exclude(module = "spring-boot-starter-tomcat")
    }
}

application {
    mainClass.set("org.radarbase.management.ManagementPortalApp")
    applicationDefaultJvmArgs = listOf(
            "--add-modules", "java.se",
            "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.nio=ALL-UNNAMED",
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens", "java.management/sun.management=ALL-UNNAMED",
            "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED",
    )
}

tasks.withType<BootWar>().configureEach {
    launchScript()
}

tasks.named<BootRun>("bootRun") {
    sourceResources(sourceSets["main"])
}

springBoot {
    buildInfo()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        apiVersion = KotlinVersion.KOTLIN_1_8
        languageVersion = KotlinVersion.KOTLIN_1_8
    }
}

tasks.test {
    jvmArgs = listOf(
            "--add-modules", "java.se",
            "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.nio=ALL-UNNAMED",
            "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-opens", "java.management/sun.management=ALL-UNNAMED",
            "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED",
    )
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
    useJUnitPlatform()
}

apply(from = "gradle/liquibase.gradle")
apply(from = "gradle/gatling.gradle")
apply(from = "gradle/mapstruct.gradle")
apply(from = "gradle/docker.gradle")
apply(from = "gradle/style.gradle")
apply(from = "gradle/openapi.gradle")
if (project.hasProperty("prod")) {
    apply(from = "gradle/profile_prod.gradle")
} else {
    apply(from = "gradle/profile_dev.gradle")
}

extra.apply {
    //set("moduleDescription", "Management Portal application")
    set("findbugAnnotation", "3.0.2")
    set("projectLanguage", "kotlin")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    implementation("tech.jhipster:jhipster-framework:${Versions.jhipsterServer}")
    implementation("tech.jhipster:jhipster-dependencies:${Versions.jhipsterServer}")
    implementation("io.micrometer:micrometer-core:${Versions.micrometer}")
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // version set via BOM
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5") // version set via BOM
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv") // version set via BOM
    implementation("com.fasterxml.jackson.core:jackson-annotations") // version set via BOM
    implementation("com.fasterxml.jackson.core:jackson-databind") // version set via BOM
    implementation("com.hazelcast:hazelcast:${Versions.hazelcast}")
    implementation("com.hazelcast:hazelcast-spring:${Versions.hazelcast}")
    runtimeOnly("com.hazelcast:hazelcast-hibernate53:${Versions.hazelcastHibernate}")
    runtimeOnly("com.zaxxer:HikariCP:${Versions.hikaricp}")
    implementation("com.google.code.findbugs:jsr305:${Versions.findbugAnnotation}")
    implementation("org.liquibase:liquibase-core:${Versions.liquibase}")
    runtimeOnly("com.mattbertolini:liquibase-slf4j:${Versions.liquibaseSlf4j}")
    implementation("org.springframework.boot:spring-boot-starter-actuator") // version set via dependency-management plugin
    implementation("org.springframework.boot:spring-boot-autoconfigure") // version set via dependency-management plugin
    implementation("org.springframework.boot:spring-boot-starter-mail") // version set via dependency-management plugin
    runtimeOnly("org.springframework.boot:spring-boot-starter-logging") // version set via dependency-management plugin
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group ="org.hibernate", module = "hibernate-entitymanager")
    } // version set via dependency-management plugin
    implementation("org.springframework.security:spring-security-data") // version set via BOM

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(module = "spring-boot-starter-tomcat")
    } // version set via dependency-management plugin
    runtimeOnly("org.springframework.boot:spring-boot-starter-security") // version set via dependency-management plugin
    implementation("org.springframework.boot:spring-boot-starter-undertow") // version set via dependency-management plugin

    implementation("org.hibernate:hibernate-core")
    implementation("org.hibernate:hibernate-envers")
    implementation("org.hibernate:hibernate-validator:${Versions.hibernateValidator}")

    runtimeOnly("org.postgresql:postgresql:${Versions.postgresql}")
    runtimeOnly("org.hsqldb:hsqldb:${Versions.hsqldb}")

    // Fix vulnerabilities
    runtimeOnly("io.undertow:undertow-websockets-jsr:2.2.25.Final")
    runtimeOnly("io.undertow:undertow-servlet:2.2.25.Final")
    runtimeOnly("io.undertow:undertow-core:2.2.25.Final")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    runtimeOnly("org.thymeleaf:thymeleaf:${Versions.thymeleaf}")
    runtimeOnly("org.thymeleaf:thymeleaf-spring5:${Versions.thymeleaf}")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.session:spring-session-hazelcast") // version set via BOM

    implementation("org.springframework.security.oauth:spring-security-oauth2:2.5.2.RELEASE")
    implementation("org.springframework.security:spring-security-web:5.7.8")
    implementation("org.springdoc:springdoc-openapi-ui:${Versions.springdoc}")
    runtimeOnly("javax.inject:javax.inject:1")
    implementation(project(":radar-auth"))
    implementation("org.springframework.data:spring-data-envers")

    implementation("org.mockito:mockito-core:${Versions.mockito}")
    implementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")

    runtimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.javaxXmlBind}")
    runtimeOnly("org.glassfish.jaxb:jaxb-core:${Versions.javaxJaxbCore}")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:${Versions.javaxJaxbRuntime}")
    runtimeOnly("javax.activation:activation:${Versions.javaxActivation}")
    runtimeOnly("org.javassist:javassist:3.29.2-GA")

    testImplementation("com.jayway.jsonpath:json-path")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.assertj:assertj-core:${Versions.assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation("com.mattbertolini:liquibase-slf4j:${Versions.liquibaseSlf4j}")
    testImplementation("org.hamcrest:hamcrest-library")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

allOpen {
    annotation("org.springframework.stereotype.Component")
    annotation("org.springframework.boot.test.context.SpringBootTest")
    annotation("org.springframework.web.bind.annotation.RestController")
}

dependencyManagement {
    imports {
        mavenBom("com.fasterxml.jackson:jackson-bom:${Versions.jackson}")
        // We do ot need this, right?
//        mavenBom("org.springframework:spring-framework-bom:${Versions.springFramework}")
        mavenBom("org.springframework.data:spring-data-bom:${Versions.springData}")
        mavenBom("org.springframework.session:spring-session-bom:${Versions.springSession}")
    }
}

tasks.clean {
    delete = setOf("target")
}

tasks.register("cleanResources", Delete::class) {
    delete = setOf("build/resources")
}

tasks.wrapper {
    gradleVersion = "8.3"
}

tasks.register("stage") {
    dependsOn("bootWar")
}

tasks.register<Copy>("ghPagesJavadoc") {
    from(layout.buildDirectory.file("dokka/javadoc"))
    into(layout.projectDirectory.file("public/management-portal-javadoc"))
    dependsOn(tasks.dokkaJavadoc)
}


tasks.register("ghPagesOpenApiSpec", Copy::class) {
    from(file(layout.buildDirectory.dir("swagger-spec")))
    into(file("$rootDir/public/apidoc"))
}

tasks.compileJava {
    dependsOn("processResources")
}
tasks.processResources {
    dependsOn("cleanResources")
    dependsOn("bootBuildInfo")
}
tasks.named<BuildInfo>("bootBuildInfo") {
    mustRunAfter("cleanResources")
}

tasks.register("downloadDependencies") {
    doLast {
        configurations.resolveAll()
        buildscript.configurations.resolveAll()
    }
}

fun ConfigurationContainer.resolveAll() = this
    .filter { it.isCanBeResolved }
    .forEach { it.resolve() }

apply(from = "gradle/artifacts.gradle")

nexusPublishing {
    repositories {
        sonatype {
            if (project.hasProperty("ossrh.user")) project.property("ossrh.user") else System.getenv("OSSRH_USER")
            if (project.hasProperty("ossrh.password")) project.property("ossrh.password") else System.getenv("OSSRH_PASSWORD")
        }
    }
}

fun isNonStable(string: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { string.uppercase().contains(it) }
    val regex = Regex("^[0-9,.v-]+(-r)?$")
    return !stableKeyword && !string.matches(regex)
}

tasks.withType<DependencyUpdatesTask> {
    doFirst {
        allprojects {
            repositories.removeAll {
                it is MavenArtifactRepository && it.url.toString().contains("snapshot")
            }
        }
    }
    rejectVersionIf {
        currentVersion.split("\\.")[0] != candidate.version.split("\\.")[0]
            || isNonStable(candidate.version)
    }
}
