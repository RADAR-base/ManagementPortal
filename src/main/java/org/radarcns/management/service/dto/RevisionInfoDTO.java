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

    private static final long serialVersionUID = 1L;

    private long id;

    private Date timestamp;

    private String author;

    // Groups the changes by revision type and class name
    private Map<RevisionType, Map<String, List<Object>>> changes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    /**
     * Create a RevisionInfoDTO from a {@link CustomRevisionEntity} and a set of changes grouped
     * by revision type.
     *
     * <p>This method is convenient when using a CustomRevisionEntity in combination with
     * {@link org.hibernate.envers.CrossTypeRevisionChangesReader}. The Map will be transformed
     * so changes are additionally grouped by class name.</p>
     * @param revisionEntity the revision entity
     * @param changes the changes
     * @return the RevisionInfoDTO object
     */
    public static RevisionInfoDTO from(CustomRevisionEntity revisionEntity, Map<RevisionType,
            List<Object>> changes) {
        RevisionInfoDTO result = new RevisionInfoDTO();
        result.setAuthor(revisionEntity.getAuditor());
        result.setTimestamp(revisionEntity.getTimestamp());
        result.setId(revisionEntity.getId());
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
