package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.radarcns.management.domain.enumeration.SourceTypeScope;

/**
 * A SourceType.
 */
@Entity
@Table(name = "source_type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SourceType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @Column(name = "producer")
    private String producer;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "assessment_type")
    private String assessmentType;

    @Column(name = "app_provider")
    private String appProvider;

    @NotNull
    @Column(name = "model", nullable = false )
    private String model;

    @NotNull
    @Column(name = "catalog_version", nullable = false)
    private String catalogVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type_scope", nullable = false)
    private SourceTypeScope sourceTypeScope;

    @NotNull
    @Column(name = "dynamic_registration" , nullable = false)
    private Boolean canRegisterDynamically = false;

    @OneToMany(mappedBy = "sourceType" , fetch = FetchType.EAGER)
    private Set<SourceData> sourceData;

    @ManyToMany(mappedBy = "sourceTypes")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Project> projects = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProducer() {
        return producer;
    }

    public SourceType producer(String producer) {
        this.producer = producer;
        return this;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getModel() {
        return model;
    }

    public SourceType model(String model) {
        this.model = model;
        return this;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public SourceType catalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
        return this;
    }

    public SourceTypeScope getSourceTypeScope() {
        return sourceTypeScope;
    }

    public SourceType sourceTypeScope(SourceTypeScope sourceTypeScope) {
        this.sourceTypeScope = sourceTypeScope;
        return this;
    }

    public void setSourceTypeScope(SourceTypeScope sourceTypeScope) {
        this.sourceTypeScope = sourceTypeScope;
    }

    public Set<SourceData> getSourceData() {
        return sourceData;
    }

    public SourceType sourceData(Set<SourceData> sourceData) {
        this.sourceData = sourceData;
        return this;
    }

    public void setSourceData(Set<SourceData> sourceData) {
        this.sourceData = sourceData;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public SourceType projects(Set<Project> projects) {
        this.projects = projects;
        return this;
    }

    public SourceType addProject(Project project) {
        this.projects.add(project);
        project.getSourceTypes().add(this);
        return this;
    }

    public SourceType removeProject(Project project) {
        this.projects.remove(project);
        project.getSourceTypes().remove(this);
        return this;
    }

    public Boolean getCanRegisterDynamically() {
        return canRegisterDynamically;
    }

    public void setCanRegisterDynamically(Boolean canRegisterDynamically) {
        this.canRegisterDynamically = canRegisterDynamically;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public void setAppProvider(String appProvider) {
        this.appProvider = appProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceType sourceType = (SourceType) o;
        if (sourceType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, sourceType.id)
            && Objects.equals(producer, sourceType.producer)
            && Objects.equals(model, sourceType.model)
            && Objects.equals(catalogVersion , sourceType.catalogVersion)
            && Objects.equals(canRegisterDynamically , sourceType.canRegisterDynamically)
            && Objects.equals(sourceTypeScope, sourceType.sourceTypeScope)
            && Objects.equals(name , sourceType.name)
            && Objects.equals(description , sourceType.description)
            && Objects.equals(appProvider , sourceType.appProvider)
            && Objects.equals(assessmentType , sourceType.assessmentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, model, producer, catalogVersion, canRegisterDynamically,
            sourceTypeScope, name, description, appProvider, assessmentType);
    }

    @Override
    public String toString() {
        return "SourceType{" +
            "id=" + id +
            ", producer='" + producer + '\'' +
            ", model='" + model + '\'' +
            ", catalogVersion='" + catalogVersion + '\'' +
            ", sourceTypeScope=" + sourceTypeScope +
            ", canRegisterDynamically=" + canRegisterDynamically +
            ", name='" + name + '\'' +
            ", description=" + description +
            ", appProvider=" + appProvider +
            ", assessmentType=" + assessmentType +
            '}';
    }
}
