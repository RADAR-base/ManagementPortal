package org.radarbase.auth.authorization

/** Service to determine the relationship between entities. */
interface EntityRelationService {
    /** From a [project] name, return an organization name. */
    fun findOrganizationOfProject(project: String): String

    /** Whether given [organization] name has a [project] with given name. */
    fun organizationContainsProject(organization: String, project: String): Boolean =
        findOrganizationOfProject(project) == organization
}
