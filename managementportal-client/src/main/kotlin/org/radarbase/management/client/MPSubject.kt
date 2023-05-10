package org.radarbase.management.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** ManagementPortal Subject DTO. */
@Serializable
data class MPSubject(
    /** User id, a name that identifies it uniquely. */
    @SerialName("login") val id: String?,
    /** Project id that the subject belongs to. */
    @kotlinx.serialization.Transient val projectId: String? = null,
    /** Full project details that a subject belongs to. */
    val project: MPProject? = null,
    /** ID in an external system for the user. */
    val externalId: String? = null,
    /** ID Link in an external system for the user. */
    val externalLink: String? = null,
    /** User status in the project. */
    val status: String = "DEACTIVATED",
    /** Group of the subject. */
    val group: String? = null,
    /** Additional attributes of the user. */
    val attributes: Map<String, String> = emptyMap(),
)
