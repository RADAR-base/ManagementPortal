package org.radarbase.management.service.dto

import org.hibernate.envers.RevisionType
import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.springframework.data.history.Revision
import java.time.Instant

class RevisionDTO {
    var id = 0
        private set
    var timestamp: Instant? = null
        private set
    var author: String? = null
        private set
    var entity: Any? = null
        private set
    var revisionType: RevisionType? = null
        private set

    constructor()

    /**
     * Create a DTO from a revision, type and entity.
     * @param revision the revision
     * @param revisionType the type
     * @param entity the entity
     */
    constructor(revision: Revision<*, *>, revisionType: RevisionType?, entity: Any?) {
        id = revision.requiredRevisionNumber.toInt()
        timestamp = revision.requiredRevisionInstant
        author = (revision.metadata.getDelegate() as CustomRevisionEntity).auditor
        this.entity = entity
        this.revisionType = revisionType
    }

    fun setId(id: Int): RevisionDTO {
        this.id = id
        return this
    }

    fun setTimestamp(timestamp: Instant?): RevisionDTO {
        this.timestamp = timestamp
        return this
    }

    fun setAuthor(author: String?): RevisionDTO {
        this.author = author
        return this
    }

    fun setEntity(entity: Any?): RevisionDTO {
        this.entity = entity
        return this
    }

    fun setRevisionType(revisionType: RevisionType?): RevisionDTO {
        this.revisionType = revisionType
        return this
    }
}
