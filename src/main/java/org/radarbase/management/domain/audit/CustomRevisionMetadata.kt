package org.radarbase.management.domain.audit

import org.springframework.data.history.RevisionMetadata
import org.springframework.util.Assert
import java.time.Instant
import java.util.*

class CustomRevisionMetadata(entity: CustomRevisionEntity) : RevisionMetadata<Int> {
    private val entity: CustomRevisionEntity

    /**
     * Creates a new [CustomRevisionMetadata].
     *
     * @param entity must not be null.
     */
    init {
        Assert.notNull(entity, "The CustomRevisionEntity can not be null")
        this.entity = entity
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.history.RevisionMetadata#getRevisionNumber()
     */
    override fun getRevisionNumber(): Optional<Int> {
        return Optional.of(entity.id)
    }

    override fun getRequiredRevisionNumber(): Int {
        return super.getRequiredRevisionNumber()
    }

    override fun getRevisionInstant(): Optional<Instant> {
        return Optional.ofNullable(entity.timestamp).map { ts: Date -> ts.toInstant() }
    }

    override fun getRequiredRevisionInstant(): Instant {
        return super.getRequiredRevisionInstant()
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.history.RevisionMetadata#getDelegate()
     */
    override fun <T> getDelegate(): T {
        return entity as T
    }

    override fun getRevisionType(): RevisionMetadata.RevisionType {
        return super.getRevisionType()
    }
}
