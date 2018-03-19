package org.radarcns.management.service.dto;

import org.hibernate.envers.RevisionType;
import org.radarcns.management.domain.audit.CustomRevisionEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for representing comprehensive information about a revision.
 */
public class RevisionInfoDTO implements Serializable {

    private long revisionNumber;

    private Date timestamp;

    private String author;

    // Groups the changes by revision type and class name
    private Map<RevisionType, Map<String, List<Object>>> changes;

    public long getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(long revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Map<RevisionType, Map<String, List<Object>>> getChanges() {
        return changes;
    }

    public void setChanges(Map<RevisionType, Map<String, List<Object>>> changes) {
        this.changes = changes;
    }

    public static RevisionInfoDTO from(CustomRevisionEntity revisionEntity, Map<RevisionType,
            List<Object>> changes) {
        RevisionInfoDTO result = new RevisionInfoDTO();
        result.setAuthor(revisionEntity.getAuditor());
        result.setTimestamp(revisionEntity.getTimestamp());
        result.setRevisionNumber(revisionEntity.getId());
        result.setChanges(new HashMap<>());
        changes.forEach((type, objects) -> {
            result.changes.putIfAbsent(type, new HashMap<>());
            objects.stream().filter(Objects::nonNull).forEach(object -> {
                result.changes.get(type).putIfAbsent(object.getClass().getSimpleName(),
                        new LinkedList<>());
                result.changes.get(type).get(object.getClass().getSimpleName()).add(object);
            });
        });
        return result;
    }
}
