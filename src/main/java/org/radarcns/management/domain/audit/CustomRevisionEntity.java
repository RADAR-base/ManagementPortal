package org.radarcns.management.domain.audit;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.radarcns.management.config.audit.CustomRevisionListener;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Entity
@RevisionEntity(CustomRevisionListener.class)
@Table(name = "_revisions_info")
public class CustomRevisionEntity implements Serializable {
    private static final long serialVersionUID = 8530213963961662300L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "revisionGenerator")
    @SequenceGenerator(name = "revisionGenerator", initialValue = 2, allocationSize = 50,
            sequenceName = "sequence_revision")
    @RevisionNumber
    private int id;

    @RevisionTimestamp
    private Date timestamp;

    private String auditor;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "REVCHANGES", joinColumns = @JoinColumn(name = "REV"))
    @Column(name = "ENTITYNAME")
    @Fetch(FetchMode.JOIN)
    @ModifiedEntityNames
    private Set<String> modifiedEntityNames;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomRevisionEntity)) {
            return false;
        }
        CustomRevisionEntity that = (CustomRevisionEntity) o;
        return id == that.id && Objects.equals(timestamp, that.timestamp) && Objects
                .equals(auditor, that.auditor) && Objects
                .equals(modifiedEntityNames, that.modifiedEntityNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, auditor, modifiedEntityNames);
    }

    @Override
    public String toString() {
        return "CustomRevisionEntity{"
                + "id=" + id
                + ", timestamp=" + timestamp
                + ", auditor='" + auditor + '\''
                + ", modifiedEntityNames=" + modifiedEntityNames
                + '}';
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public Set<String> getModifiedEntityNames() {
        return modifiedEntityNames;
    }

    public void setModifiedEntityNames(Set<String> modifiedEntityNames) {
        this.modifiedEntityNames = modifiedEntityNames;
    }
}