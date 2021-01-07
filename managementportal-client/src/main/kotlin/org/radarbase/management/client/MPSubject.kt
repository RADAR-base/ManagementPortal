package org.radarbase.management.client

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/** ManagementPortal Subject DTO. */
data class MPSubject(
    /** User id, a name that identifies it uniquely. */
    @JsonProperty("login") val id: String?,
    /** Project id that the subject belongs to. */
    @JsonIgnore val projectId: String? = null,
    /** Full project details that a subject belongs to. */
    val project: MPProject? = null,
    /** ID in an external system for the user. */
    val externalId: String? = null,
    /** ID Link in an external system for the user. */
    val externalLink: String? = null,
    /** User status in the project. */
    val status: String = "DEACTIVATED",
    /** Additional attributes of the user. */
    val attributes: Map<String, String> = emptyMap(),
)
