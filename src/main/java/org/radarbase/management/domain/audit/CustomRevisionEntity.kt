package org.radarbase.management.domain.audit

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.envers.ModifiedEntityNames
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import org.radarbase.management.config.audit.CustomRevisionListener
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@RevisionEntity(CustomRevisionListener::class)
@Table(name = "_revisions_info")
class CustomRevisionEntity : Serializable {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revisionGenerator")
    @SequenceGenerator(
        name = "revisionGenerator",
        initialValue = 2,
        allocationSize = 50,
        sequenceName = "sequence_revision"
    )
    @RevisionNumber
    var id = 0

    @JvmField
    @get:Temporal(TemporalType.TIMESTAMP)
    @RevisionTimestamp
    var timestamp: Date? = null
    @JvmField
    var auditor: String? = null

    @JvmField
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "REVCHANGES", joinColumns = [JoinColumn(name = "REV")])
    @Column(name = "ENTITYNAME")
    @Fetch(
        FetchMode.JOIN
    )
    @ModifiedEntityNames
    var modifiedEntityNames: Set<String>? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is CustomRevisionEntity) {
            return false
        }
        val that = o
        return id == that.id && timestamp == that.timestamp && auditor == that.auditor && modifiedEntityNames == that.modifiedEntityNames
    }

    override fun hashCode(): Int {
        return Objects.hash(id, timestamp, auditor, modifiedEntityNames)
    }

    override fun toString(): String {
        return ("CustomRevisionEntity{"
                + "id=" + id
                + ", timestamp=" + timestamp
                + ", auditor='" + auditor + '\''
                + ", modifiedEntityNames=" + modifiedEntityNames
                + '}')
    }

    companion object {
        private const val serialVersionUID = 8530213963961662300L
    }
}
