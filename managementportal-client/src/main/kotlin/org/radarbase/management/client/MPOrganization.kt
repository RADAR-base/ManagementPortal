package org.radarbase.management.client

import com.fasterxml.jackson.annotation.JsonProperty

/** ManagementPortal Project DTO. */
data class MPOrganization(
    /** Organization id, a name that identifies it uniquely. */
    @JsonProperty("name") val id: String,
    /** Where a project is organized. */
    val location: String? = null,
    /** Project description. */
    val description: String? = null,
    val projects: List<MPProject> = emptyList(),
)
