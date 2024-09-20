package org.radarbase.management.domain.audit

import java.time.ZonedDateTime

/**
 * POJO only used to easily get entity audit information, i.e. created by, created at, modified
 * by and modified at.
 */
class EntityAuditInfo {
    var createdAt: ZonedDateTime? = null
        private set
    var createdBy: String? = null
        private set
    var lastModifiedAt: ZonedDateTime? = null
        private set
    var lastModifiedBy: String? = null
        private set

    fun setCreatedAt(createdAt: ZonedDateTime?): EntityAuditInfo {
        this.createdAt = createdAt
        return this
    }

    fun setCreatedBy(createdBy: String?): EntityAuditInfo {
        this.createdBy = createdBy
        return this
    }

    fun setLastModifiedAt(lastModifiedAt: ZonedDateTime?): EntityAuditInfo {
        this.lastModifiedAt = lastModifiedAt
        return this
    }

    fun setLastModifiedBy(lastModifiedBy: String?): EntityAuditInfo {
        this.lastModifiedBy = lastModifiedBy
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as EntityAuditInfo
        return createdAt == that.createdAt &&
            createdBy == that.createdBy &&
            lastModifiedAt == that.lastModifiedAt &&
            lastModifiedBy == that.lastModifiedBy
    }

    override fun hashCode(): Int = if (lastModifiedAt != null) lastModifiedAt.hashCode() else 0

    override fun toString(): String =
        (
            "EntityAuditInfo{" +
                "createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", lastModifiedAt=" + lastModifiedAt +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                '}'
            )
}
