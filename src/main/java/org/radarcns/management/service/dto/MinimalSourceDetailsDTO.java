package org.radarcns.management.service.dto;

/**
 * Created by nivethika on 13-6-17.
 */
public class MinimalSourceDetailsDTO {

    private Long id;

    private String deviceTypeAndSourceId;

    private boolean assigned;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceTypeAndSourceId() {
        return deviceTypeAndSourceId;
    }

    public void setDeviceTypeAndSourceId(String deviceTypeAndSourceId) {
        this.deviceTypeAndSourceId = deviceTypeAndSourceId;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
