# radar-auth-jersey

Library to facilitate integration with a Jersey-based REST API.

# Usage

Any path or resource that should be authenticated against the ManagementPortal, should be annotated with `@Authenticated`. Specific authorization can be checked by adding a `@NeedsPermission` annotation. An `Auth` object can be injected to get app-specific information. Examples:

```kotlin
@Path("/projects")
@Authenticated
class Users(@Context projectService: ProjectService) {

    @GET
    @NeedsPermission(PROJECT, READ)
    fun getProjects(@Context auth: Auth): List<Project> {
        return projectService.read()
            .filter { auth.token.hasPermissionOnProject(PROJECT_READ, it.name) }
    } 

    @POST
    @Path("/{projectId}")
    @NeedsPermission(PROJECT, UPDATE, "projectId")
    fun updateProject(@PathParam("projectId") projectId: String, project: Project) {
        return projectService.update(projectId, project)
    }

    @GET
    @Path("/{projectId}/users/{userId}")
    @NeedsPermission(SUBJECT, READ, "projectId", "userId")
    fun getUsers(@PathParam("projectId") projectId: String, @PathParam("userId") userId: String) {
        return projectService.readUser(projectId, userId)
    }
}
```

These APIs can be activated by implementing the `ProjectService` that ensures that a project exists and by running, during ResourceConfig setup:
```kotlin
val authConfig = AuthConfig(
        managementPortalUrl = "http://...",
        jwtResourceName = "res_MyResource")

val radarEnhancer = RadarJerseyResourceEnhancer(authConfig)
val mpEnhancer = ManagementPortalResourceEnhancer()

val resourceConfig = ResourceConfig()
resourceConfig.packages(*radarEnhancer.packages)
resourceConfig.packages(*mpEnhancer.packages)

resourceConfig.register(object : AbstractBinder() {
    override fun configure() {
        bind(MyProjectService::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

        radarEnhancer.enhance(this)
        mpEnhancer.enhance(this)
    }
})
```

## Error handling

This package adds some error handling. Specifically, `org.radarbase.auth.jersey.exception.HttpApplicationException` can be used and extended to serve detailed error messages with customized logging and HTML templating. They can be thrown from any resource.

To serve custom HTML error messages for error codes 400 to 599, add a Mustache template to the classpath in directory `org/radarbase/auth/jersey/exception/<code>.html`. You can use special cases `4xx.html` and `5xx.html` as a catch-all template. The templates can use variables `status` for the HTTP status code, `code` for short-hand code for the specific error, and an optional `detailedMessage` for a human-readable message.
