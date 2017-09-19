package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A Source.
 */
@Entity
@Table(name = "radar_source")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Source implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "source_id", nullable = false , unique = true)
    private UUID sourceId;

    @NotNull
    @Column(name = "source_name", nullable = false, unique = true)
    private String sourceName;

    @Column(name = "device_category")
    private String deviceCategory;

    @NotNull
    @Column(name = "assigned", nullable = false)
    private Boolean assigned;

    @ManyToMany(mappedBy = "sources")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Subject> subjects = new HashSet<>();

    @ManyToOne
    private DeviceType deviceType;

    @ManyToOne
    private Project project;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="attribute_key")
    @Column(name="attribute_value")
    @CollectionTable(name="source_metadata" ,  joinColumns = @JoinColumn(name = "id"))
    Map<String, String> attributes = new HashMap<String, String>(); // maps from attribute name to value

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public Source devicePhysicalId(UUID devicePhysicalId) {
        this.sourceId = devicePhysicalId;
        return this;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public Source deviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
        return this;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public Boolean isAssigned() {
        return assigned;
    }

    public Source assigned(Boolean assigned) {
        this.assigned = assigned;
        return this;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Source deviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public Source project(Project project) {
        this.project = project;
        return this;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public Source subjects(Set<Subject> subjects) {
        this.subjects = subjects;
        return this;
    }

    public Source addSubject(Subject subject) {
        this.subjects.add(subject);
        subject.getSources().add(this);
        return this;
    }

    public Source removeSubject(Subject subject) {
        this.subjects.remove(subject);
        subject.getSources().remove(this);
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Source)) return false;

        Source source = (Source) o;

        if (!sourceId.equals(source.sourceId)) return false;
        return sourceName.equals(source.sourceName);
    }

    @Override
    public int hashCode() {
        int result = sourceId.hashCode();
        result = 31 * result + sourceName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Source{" +
            "id=" + id +
            ", sourceId='" + sourceId + '\'' +
            ", sourceName='" + sourceName + '\'' +
            ", deviceCategory='" + deviceCategory + '\'' +
            ", assigned=" + assigned +
            ", deviceType=" + deviceType +
            ", project=" + project +
            '}';
    }
}
