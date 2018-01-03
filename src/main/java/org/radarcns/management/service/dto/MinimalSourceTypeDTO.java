package org.radarcns.management.service.dto;

/**
 * Created by nivethika on 22-6-17.
 */
public class MinimalSourceTypeDTO {

    private long id;

    private String model;

    private String producer;

    private String catalogVersion;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }
}
