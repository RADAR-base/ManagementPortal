dependencyResolutionManagement {
    // Allows to use the version catalog in the buildSrc module
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
