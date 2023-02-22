package org.radarbase.auth.authorization

import org.radarbase.auth.util.plus

data class AuthorityReferenceSet(
    /** Identity has global authority. */
    val global: Boolean = false,
    /** Identity has explicit authority over these organizations. */
    val organizations: Set<String> = emptySet(),
    /** Identity has explicit authority over these projects. */
    val projects: Set<String> = emptySet(),
    /** Identity has explicit personal authority over these projects. */
    val personalProjects: Set<String> = emptySet(),
) {
    /** Identity does not have any authority. */
    fun isEmpty(): Boolean = !global && organizations.isEmpty() && projects.isEmpty()

    /** Identity has authority over the given [organization]. */
    fun hasOrganization(organization: String): Boolean = organization in organizations

    val allProjects: Set<String>
        get() = projects + personalProjects

    /** Identity has authority over any project, personal or not. */
    fun hasAnyProject(project: String) = project in projects || project in personalProjects

    /** Identity has authority over any project. */
    fun hasAnyProjects() = projects.isNotEmpty() || personalProjects.isNotEmpty()
}
