package org.radarcns.management.service.dto;

/**
 * Created by nivethika on 13-6-17.
 */
public class DescriptiveDeviceDTO {

    private Long id;

    private String deviceTypeAndPhysicalId;

    private boolean assigned;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceTypeAndPhysicalId() {
        return deviceTypeAndPhysicalId;
    }

    public void setDeviceTypeAndPhysicalId(String deviceTypeAndPhysicalId) {
        this.deviceTypeAndPhysicalId = deviceTypeAndPhysicalId;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
