package org.radarbase.management.service.dto;

import org.hibernate.envers.RevisionType;
import org.radarbase.management.domain.audit.CustomRevisionEntity;
import org.springframework.data.history.Revision;

import java.time.Instant;

public class RevisionDTO {
    private int id;
    private Instant timestamp;
    private String author;
    private Object entity;
    private RevisionType revisionType;

    public RevisionDTO() {
        // JSON initializer
    }

    /**
     * Create a DTO from a revision, type and entity.
     * @param revision the revision
     * @param revisionType the type
     * @param entity the entity
     */
    public RevisionDTO(Revision revision, RevisionType revisionType, Object entity) {
        // TODO: Make sure no NPE is thrown.
        id = (int) revision.getRevisionNumber().get();
        // TODO: Make sure no NPE is thrown.
        timestamp = (Instant) revision.getRevisionInstant().get();
        author = ((CustomRevisionEntity) revision.getMetadata().getDelegate()).getAuditor();
        this.entity = entity;
        this.revisionType = revisionType;
    }

    public int getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public Object getEntity() {
        return entity;
    }

    public RevisionType getRevisionType() {
        return revisionType;
    }

    public RevisionDTO setId(int id) {
        this.id = id;
        return this;
    }

    public RevisionDTO setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public RevisionDTO setAuthor(String author) {
        this.author = author;
        return this;
    }

    public RevisionDTO setEntity(Object entity) {
        this.entity = entity;
        return this;
    }

    public RevisionDTO setRevisionType(RevisionType revisionType) {
        this.revisionType = revisionType;
        return this;
    }
}
