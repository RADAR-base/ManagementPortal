package org.radarbase.management.service.dto

import org.hibernate.envers.RevisionType
import org.radarbase.management.domain.audit.CustomRevisionEntity
import java.io.Serializable
import java.util.*

/**
 * Class for representing comprehensive information about a revision.
 */
class RevisionInfoDTO : Serializable {
    var id = 0
    var timestamp: Date? = null
    var author: String? = null

    // Groups the changes by revision type and class name
    var changes: Map<RevisionType, Map<String, List<Any>>>? = null

    companion object {
        private const val serialVersionUID = 1L

        /**
         * Create a RevisionInfoDTO from a [CustomRevisionEntity] and a set of changes grouped
         * by revision type.
         *
         *
         * This method is convenient when using a CustomRevisionEntity in combination with
         * [org.hibernate.envers.CrossTypeRevisionChangesReader]. The Map will be transformed
         * so changes are additionally grouped by class name.
         * @param revisionEntity the revision entity
         * @param changes the changes
         * @return the RevisionInfoDTO object
         */
        fun from(revisionEntity: CustomRevisionEntity, changes: Map<RevisionType, List<Any>>): RevisionInfoDTO {
            val result = RevisionInfoDTO()
            result.author = revisionEntity.auditor
            result.timestamp = revisionEntity.timestamp
            result.id = revisionEntity.id
            result.changes = changes.entries
                .associateBy (
                    { obj -> obj.key },
                    {
                        it.value.groupBy { obj ->
                            obj.javaClass.getSimpleName().replace("DTO$".toRegex(), "").lowercase()
                        }
                    }
                )

            return result
        }
    }
}
