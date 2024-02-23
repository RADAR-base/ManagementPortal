/*
 * Copyright (c) 2021  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.radarbase.management.security.Constants
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A Group.
 */
@Entity
@Table(name = "radar_group")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Group : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @Column(name = "name", length = 50, nullable = false)
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) @Size(min = 1, max = 50) var name: String? = null

    @JvmField
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var project: Project? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val group = other as Group
        return if (group.id == null || id == null) {
            false
        } else id == group.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("Group{"
                + "id=" + id + ", "
                + "name=" + name + ", "
                + "project='" + project?.projectName + "', "
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
