package org.radarbase.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.radarbase.auth.config.Constants;
import org.radarbase.management.domain.support.AbstractEntityListener;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A Source.
 */
@Entity
@Audited
@Table(name = "radar_source")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class Source extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @NotNull
    @Column(name = "source_id", nullable = false, unique = true)
    private UUID sourceId;

    @NotNull
    @Pattern(regexp = Constants.ENTITY_ID_REGEX)
    @Column(name = "source_name", nullable = false, unique = true)
    private String sourceName;

    @Column(name = "expected_source_name")
    private String expectedSourceName;

    @NotNull
    @Column(name = "assigned", nullable = false)
    private Boolean assigned;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    private SourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "source_metadata", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> attributes = new HashMap<>();

    /**
     * Default constructor. Needed for other JPA operations.
     */
    public Source() {
        // default constructor
    }

    /**
     * Constructor with SourceType. This will assign sourceType and assign default values for
     * sourceId and sourceName.
     * @param sourceType sourceType of the source.
     */
    public Source(SourceType sourceType) {
        this.sourceType = sourceType;
        this.generateUuid();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public Source sourceId(UUID devicePhysicalId) {
        this.sourceId = devicePhysicalId;
        return this;
    }

    public void setSourceId(UUID sourceId) {
        // pass
        this.sourceId = sourceId;
    }

    /**
     * Add default values for sourceId and sourceName if they are not provided before persisting
     * this object. The default for sourceId is to generate a new UUID. The default for
     * sourceName is to take to model name, and append a dash followed by the first 8 characters
     * of the string representation of the UUID.
     */
    @PrePersist
    public final void generateUuid() {
        if (this.sourceId == null) {
            this.sourceId = UUID.randomUUID();
        }
        if (this.sourceName == null) {
            this.sourceName = String.join("-", this.getSourceType().getModel(),
                    this.sourceId.toString().substring(0, 8));
        }
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

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public Source sourceType(SourceType sourceType) {
        this.sourceType = sourceType;
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

    public Subject getSubject() {
        return subject;
    }

    public Source subject(Subject subject) {
        this.subject = subject;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Source sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public void setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Source source = (Source) o;
        if (source.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, source.id)
                && Objects.equals(sourceId, source.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id , sourceId);
    }

    @Override
    public String toString() {
        return "Source{"
                + "id=" + id
                + ", sourceId='" + sourceId + '\''
                + ", sourceName='" + sourceName + '\''
                + ", assigned=" + assigned
                + ", sourceType=" + sourceType
                + ", project=" + project
                + '}';
    }
}
