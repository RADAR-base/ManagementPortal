package org.radarbase.management.domain.audit;

import org.springframework.data.history.RevisionMetadata;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Optional;

public class CustomRevisionMetadata implements RevisionMetadata<Integer> {

    private final CustomRevisionEntity entity;

    /**
     * Creates a new {@link CustomRevisionMetadata}.
     *
     * @param entity must not be {@literal null}.
     */
    public CustomRevisionMetadata(CustomRevisionEntity entity) {
        Assert.notNull(entity, "The CustomRevisionEntity can not be null");
        this.entity = entity;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.history.RevisionMetadata#getRevisionNumber()
     */
    @Override
    public Optional<Integer> getRevisionNumber() {
        return Optional.of(entity.getId());
    }

    @Override
    public Integer getRequiredRevisionNumber() {
        return RevisionMetadata.super.getRequiredRevisionNumber();
    }

    @Override
    public Optional<Instant> getRevisionInstant() {
        if (entity.getTimestamp() == null) {
            return Optional.empty();
        }
        return Optional.of(entity.getTimestamp().toInstant());
    }

    @Override
    public Instant getRequiredRevisionInstant() {
        return RevisionMetadata.super.getRequiredRevisionInstant();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.history.RevisionMetadata#getDelegate()
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDelegate() {
        return (T) entity;
    }

    @Override
    public RevisionType getRevisionType() {
        return RevisionMetadata.super.getRevisionType();
    }
}
