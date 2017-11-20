package org.radarcns.management.service.dto;

/**
 * Created by nivethika on 22-6-17.
 */
public class MinimalSourceTypeDTO {

    private long id;

    private String deviceModel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
}
