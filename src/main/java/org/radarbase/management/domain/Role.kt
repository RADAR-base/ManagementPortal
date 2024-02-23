/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.authorization.RoleAuthority.Companion.valueOfAuthorityOrNull
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

/**
 * A Role.
 */
@Entity
@Audited
@Table(name = "radar_role")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class Role : AbstractEntity, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    var users: Set<User> = HashSet()

    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var project: Project? = null

    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var organization: Organization? = null

    @JvmField
    @JsonProperty
    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinColumn(name = "authority_name", referencedColumnName = "name")
    var authority: Authority? = null

    constructor()
    constructor(authority: Authority?, project: Project?) {
        this.authority = authority
        this.project = project
    }

    constructor(authority: Authority?) {
        this.authority = authority
    }

    fun users(users: Set<User>): Role {
        this.users = users
        return this
    }

    fun project(project: Project?): Role {
        this.project = project
        return this
    }

    val role: RoleAuthority?
        get() = valueOfAuthorityOrNull(authority?.name!!)

    fun authority(authority: Authority?): Role {
        this.authority = authority
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val role = other as Role
        return authority == role.authority && project == role.project && organization == role.organization
    }

    override fun hashCode(): Int {
        return Objects.hash(authority, project, organization)
    }

    override fun toString(): String {
        return ("Role{"
                + "id=" + id + ", "
                + "organization='" + (if (organization == null) "null" else organization?.name)
                + "', "
                + "project='" + (if (project == null) "null" else project?.projectName) + "', "
                + "authority='" + authority?.name + "', "
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
