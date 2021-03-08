# ManagementPortal client library

This Kotlin library interfaces with the ManagementPortal REST API. Import it into your project using the following configuration:

```kotlin

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.radarbase:managementportal-client:<version>")
}
```

To use the client, an OAuth client with `client_credentials` grant flow needs to be configured in ManagementPortal. The client can then be used in the following way:

```kotlin
val mpClient = MPClient(
    serverConfig = MPServerConfig(
        url = "https://<baseurl>/managementportal/",
        clientId = "<clientId>",
        clientSecret = "<clientSecret",
    )
)

val projects = mpClient.requestProjects()
val subjects = mpClient.requestSubjects(projectId = "radar")
val clients = mpClient.requestClients()

// read custom URLs
val userMapper = ObjectMapper().readerForListOf(MPUser::class.java)
val userTypes = mpClient.request(userMapper, {
    addPathSegments("api/users")
    addQueryParameter("page", "0")
    addQueryParameter("size", Int.MAX_VALUE.toString())
})
```
