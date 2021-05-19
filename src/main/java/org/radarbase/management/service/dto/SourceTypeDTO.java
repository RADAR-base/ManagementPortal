package org.radarbase.management.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the SourceType entity.
 */
public class SourceTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    private String producer;

    @NotNull
    private String model;

    @NotNull
    private String catalogVersion;

    @NotNull
    private String sourceTypeScope;

    @NotNull
    private Boolean canRegisterDynamically = false;

    private String name;

    private String description;

    private String assessmentType;

    private String appProvider;

    private Set<SourceDataDTO> sourceData = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getModel() {
        return model;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSourceTypeScope() {
        return sourceTypeScope;
    }

    public void setSourceTypeScope(String sourceTypeScope) {
        this.sourceTypeScope = sourceTypeScope;
    }

    public Set<SourceDataDTO> getSourceData() {
        return sourceData;
    }

    public void setSourceData(Set<SourceDataDTO> sourceData) {
        this.sourceData = sourceData;
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

        SourceTypeDTO sourceTypeDto = (SourceTypeDTO) o;

        if (id == null || sourceTypeDto.id == null) {
            return false;
        }

        return Objects.equals(id, sourceTypeDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SourceTypeDTO{"
                + "id=" + id
                + ", producer='" + producer + "'"
                + ", model='" + model + "'"
                + ", catalogVersion='" + catalogVersion + "'"
                + ", sourceTypeScope='" + sourceTypeScope + "'"
                + ", canRegisterDynamically='" + canRegisterDynamically + "'"
                + ", name='" + name + '\''
                + ", description=" + description
                + ", appProvider=" + appProvider
                + ", assessmentType=" + assessmentType
                + '}';
    }
}
