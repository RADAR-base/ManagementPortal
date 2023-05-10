package org.radarbase.management.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** ManagementPortal Project DTO. */
@Serializable
data class MPOrganization(
    /** Organization id, a name that identifies it uniquely. */
    @SerialName("name") val id: String,
    /** Where a project is organized. */
    val location: String? = null,
    /** Project description. */
    val description: String? = null,
    val projects: List<MPProject> = emptyList(),
)
