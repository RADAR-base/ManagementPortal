package org.radarcns.management.domain;

import org.radarcns.management.domain.support.AbstractEntityListener;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Base abstract class for entities which will hold definitions for created, last modified by and
 * created, last modified by date. These will be populated by {@link AbstractEntityListener} on
 * the {@code PostLoad} trigger. Since this class is not an Entity or a MappedSuperClass, we need
 * to define the entitylistener on each of the subclasses.
 */
public abstract class AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String createdBy;

    private ZonedDateTime createdDate;

    private String lastModifiedBy;

    private ZonedDateTime lastModifiedDate;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ZonedDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public abstract Long getId();
}
