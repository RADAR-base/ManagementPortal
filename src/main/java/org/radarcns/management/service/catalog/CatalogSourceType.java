package org.radarcns.management.service.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hibernate.validator.constraints.NotEmpty;

public class CatalogSourceType {

    @JsonProperty("assessment_type")
    private String assessmentType;

    @JsonProperty("app_provider")
    private String appProvider;

    @JsonProperty
    private String vendor;

    @JsonProperty
    private String model;

    @JsonProperty
    private String version;

    @JsonProperty
    private String name;

    @JsonProperty
    private String doc;

    @JsonProperty
    private String scope;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private List<String> labels;

    @JsonProperty
    @NotEmpty
    private List<CatalogSourceData> data;

    public String getName() {
        return name;
    }

    public String getDoc() {
        return doc;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public String getVendor() {
        return vendor;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public String getScope() {
        return scope;
    }

    public List<CatalogSourceData> getData() {
        return data;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CatalogSourceType that = (CatalogSourceType) o;
        return Objects.equals(assessmentType, that.assessmentType)
                && Objects.equals(appProvider, that.appProvider)
                && Objects.equals(vendor, that.vendor)
                && Objects.equals(model, that.model)
                && Objects.equals(version, that.version)
                && Objects.equals(name, that.name)
                && Objects.equals(doc, that.doc)
                && Objects.equals(scope, that.scope)
                && Objects.equals(properties, that.properties)
                && Objects.equals(labels, that.labels)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        return Objects
            .hash(assessmentType, appProvider, vendor, model, version, name, doc, scope, properties,
                labels, data);
    }
}
