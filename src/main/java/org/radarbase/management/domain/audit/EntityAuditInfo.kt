package org.radarbase.management.domain.audit;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * POJO only used to easily get entity audit information, i.e. created by, created at, modified
 * by and modified at.
 */
public class EntityAuditInfo {
    private ZonedDateTime createdAt;
    private String createdBy;
    private ZonedDateTime lastModifiedAt;
    private String lastModifiedBy;

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public EntityAuditInfo setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public EntityAuditInfo setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ZonedDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public EntityAuditInfo setLastModifiedAt(ZonedDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public EntityAuditInfo setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EntityAuditInfo that = (EntityAuditInfo) o;

        return Objects.equals(createdAt, that.createdAt)
                && Objects.equals(createdBy, that.createdBy)
                && Objects.equals(lastModifiedAt, that.lastModifiedAt)
                && Objects.equals(lastModifiedBy, that.lastModifiedBy);
    }

    @Override
    public int hashCode() {
        return lastModifiedAt != null ? lastModifiedAt.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "EntityAuditInfo{"
                + "createdAt=" + createdAt
                + ", createdBy='" + createdBy + '\''
                + ", lastModifiedAt=" + lastModifiedAt
                + ", lastModifiedBy='" + lastModifiedBy + '\''
                + '}';
    }
}
