package org.radarcns.management.service.dto;

import java.util.stream.Collectors;
import org.hibernate.envers.RevisionType;
import org.radarcns.management.domain.audit.CustomRevisionEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for representing comprehensive information about a revision.
 */
public class RevisionInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private Date timestamp;

    private String author;

    // Groups the changes by revision type and class name
    private Map<RevisionType, Map<String, List<Object>>> changes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
        result.setChanges(changes.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(obj -> obj.getClass()
                                .getSimpleName()
                                .replaceAll("DTO$","")
                                .toLowerCase())))));
        return result;
    }
}
