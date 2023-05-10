package org.radarbase.auth.authorization

import java.util.function.Consumer
import kotlin.math.min

/** Entity details to check with AuthorizationOracle. */
data class EntityDetails(
    /** Organization name. */
    var organization: String? = null,
    /** Project name. */
    var project: String? = null,
    /** Subject login. */
    var subject: String? = null,
    /** User login. */
    var user: String? = null,
    /** Source name */
    var source: String? = null,
) {
    /**
     * Return the entity most basic in this EntityDetails.
     * If no field is set, e.g. this is a global Entity, returns null.
     */
    fun minimumEntityOrNull(): Permission.Entity? = when {
        user != null -> Permission.Entity.USER
        source != null -> Permission.Entity.SOURCE
        subject != null -> Permission.Entity.SUBJECT
        project != null -> Permission.Entity.PROJECT
        organization != null -> Permission.Entity.ORGANIZATION
        else -> null
    }

    val isGlobal: Boolean
        get() = minimumEntityOrNull() == null

    fun organization(organization: String?) = apply {
        this.organization = organization
    }

    fun project(project: String?) = apply {
        this.project = project
    }

    fun subject(subject: String?) = apply {
        this.subject = subject
    }

    fun user(user: String?) = apply {
        this.user = user
    }

    fun source(source: String?) = apply {
        this.source = source
    }

    companion object {
        val global = EntityDetails()
    }
}

inline fun entityDetails(
    config: EntityDetails.() -> Unit,
): EntityDetails = EntityDetails().apply(config)

fun entityDetailsBuilder(
    config: Consumer<EntityDetails>
): EntityDetails = entityDetails(config::accept)
