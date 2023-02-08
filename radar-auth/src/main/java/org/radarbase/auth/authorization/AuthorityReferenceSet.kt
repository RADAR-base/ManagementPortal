package org.radarbase.auth.authorization

data class AuthorityReferenceSet(
    /** Identity has global authority. */
    val global: Boolean = false,
    /** Identity has explicit authority over these organizations. */
    val organizations: Set<String> = emptySet(),
    /** Identity has explicit authority over these projects. */
    val projects: Set<String> = emptySet(),
) {
    /** Identity does not have any authority. */
    fun isEmpty(): Boolean = !global && organizations.isEmpty() && projects.isEmpty()

    /** Identity has authority over the given [organization]. */
    fun hasOrganization(organization: String): Boolean = organization in organizations

    /** Identity has authority over the given [project]. */
    fun hasProject(project: String) = project in projects

    /** Whether identity has explicit authority over any projects. */
    fun hasProjects() = projects.isNotEmpty()
}
