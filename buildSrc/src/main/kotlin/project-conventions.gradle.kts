plugins {
    idea
    id("org.radarbase.radar-kotlin")
    id("org.radarbase.radar-publishing")
    id("org.radarbase.radar-dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "org.radarbase"

radarPublishing {
    githubUrl.set("https://github.com/RADAR-base/ManagementPortal")
    developers {
        developer {
            id.set("pvanierop")
            name.set("Pim van Nierop")
            email.set("pim@thehyve.nl")
            organization.set("The Hyve")
        }
        developer {
            id.set("nivemaham")
            name.set("Nivethika Mahasivam")
            email.set("nivethika@thehyve.nl")
            organization.set("The Hyve")
        }
        developer {
            id.set("dennyverbeeck")
            name.set("Denny Verbeeck")
            email.set("dverbeec@its.jnj.com")
            organization.set("Janssen R&D")
        }
    }
}

tasks.register("ghPages") {
    dependsOn(
        provider {
            tasks.filter { it.name.startsWith("ghPages") && it.name != "ghPages" }
        },
    )
}
