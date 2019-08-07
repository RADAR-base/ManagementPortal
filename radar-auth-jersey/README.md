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

val authConfig = AuthConfig(
        managementPortalUrl = "http://...",
        jwtResourceName = "res_MyResource")

resourceConfig.register(object : AbstractBinder() {
    override fun configure() {
        bind(MyProjectService::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

        RadarJerseyResourceEnhancer(authConfig)
                .enhance(resourceConfig, this)
        ManagementPortalResourceEnhancer()
                .enhance(resourceConfig, this)
    }
})
```
