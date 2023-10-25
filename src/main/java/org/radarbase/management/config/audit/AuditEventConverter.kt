package org.radarbase.management.config.audit

import org.radarbase.management.domain.PersistentAuditEvent
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class AuditEventConverter {
    /**
     * Convert a list of PersistentAuditEvent to a list of AuditEvent.
     *
     * @param persistentAuditEvents the list to convert
     * @return the converted list.
     */
    fun convertToAuditEvent(
        persistentAuditEvents: Iterable<PersistentAuditEvent>?
    ): List<AuditEvent> {
        if (persistentAuditEvents == null) {
            return emptyList()
        }
        val auditEvents: MutableList<AuditEvent> = ArrayList()
        for (persistentAuditEvent in persistentAuditEvents) {
            auditEvents.add(convertToAuditEvent(persistentAuditEvent))
        }
        return auditEvents
    }

    /**
     * Convert a PersistentAuditEvent to an AuditEvent.
     *
     * @param persistentAuditEvent the event to convert
     * @return the converted list.
     */
    fun convertToAuditEvent(persistentAuditEvent: PersistentAuditEvent): AuditEvent {
        val instant = persistentAuditEvent.auditEventDate.atZone(ZoneId.systemDefault())
            .toInstant()
        return AuditEvent(
            instant, persistentAuditEvent.principal,
            persistentAuditEvent.auditEventType,
            convertDataToObjects(persistentAuditEvent.data)
        )
    }

    /**
     * Internal conversion. This is needed to support the current SpringBoot actuator
     * AuditEventRepository interface
     *
     * @param data the data to convert
     * @return a map of String, Object
     */
    fun convertDataToObjects(data: Map<String, String>?): Map<String, Any> {
        val results: MutableMap<String, Any> = HashMap()
        if (data != null) {
            for ((key, value) in data) {
                results[key] = value
            }
        }
        return results
    }

    /**
     * Internal conversion. This method will allow to save additional data. By default, it will save
     * the object as string
     *
     * @param data the data to convert
     * @return a map of String, String
     */
    fun convertDataToStrings(data: Map<String, Any?>?): Map<String, String> {
        val results: MutableMap<String, String> = HashMap()
        if (data != null) {
            for ((key, value) in data) {

                // Extract the data that will be saved.
                if (value is WebAuthenticationDetails) {
                    results["sessionId"] = value.sessionId
                } else if (value != null) {
                    results[key] = value.toString()
                } else {
                    results[key] = "null"
                }
            }
        }
        return results
    }
}
