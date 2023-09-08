package org.radarbase.management.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** ManagementPortal Project DTO. */
@Serializable
data class MPProject(
    /** Project id, a name that identifies it uniquely. */
    @SerialName("projectName") val id: String,
    /** Project name, to be shown to users. */
    @SerialName("humanReadableProjectName") val name: String? = null,
    /** Where a project is organized. */
    val location: String? = null,
    /** Organization that organizes the project. */
    val organization: MPOrganization? = null,
    /** Free-text name of the organization. */
    val organizationName: String? = null,
    /** Project description. */
    val description: String? = null,
    /** Any other attributes. */
    val attributes: Map<String, String> = emptyMap(),
    val projectStatus: String? = null,
    /** ZonedDateTime */
    val startDate: String? = null,
    /** ZonedDateTime */
    val endDate: String? = null,
    val sourceTypes: List<MPSourceType> = listOf(),
)
