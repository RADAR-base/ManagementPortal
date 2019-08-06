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
            .filter { auth.hasPermissionOnProject(PROJECT_READ, it.name) }
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
val resourceConfig = ResourceConfig()

val authConfig = AuthConfig(managementPortalUrl = "http://...")
val radarEnhancer = RadarJerseyResourceEnhancer(authConfig)
val mpEnhancer = ManagementPortalResourceEnhancer()

radarEnhancer.enhance(resourceConfig)
mpEnhancer.enhance(resourceConfig)

resourceConfig.register(object : AbstractBinder() {
    override fun configure() {
        radarEnhancer.enhanceBinder(this)
        mpEnhancer.enhanceBinder(this)
    }
})
```
