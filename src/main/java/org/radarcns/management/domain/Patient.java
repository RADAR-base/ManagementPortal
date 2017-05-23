package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Created by nivethika on 22-5-17.
 */
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 200)
    @Column(name = "external_link", length = 50)
    private String externalLink;

    @Size(max = 50)
    @Column(name = "external_id", length = 50)
    private String externalId;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "patient_device",
        joinColumns = @JoinColumn(name="patient_id", referencedColumnName="id"),
        inverseJoinColumns = @JoinColumn(name="device_id", referencedColumnName="id"))
    private Set<Device> devices = new HashSet<>();

    @NotNull
    @Column(nullable = false)
    private boolean removed;

    //    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public Patient devices(Set<Device> devices) {
        this.devices = devices;
        return this;
    }

    public Patient addDevice(Device device) {
        this.devices.add(device);
        device.getPatients().add(this);
        return this;
    }

    public Patient removeDevice(Device device) {
        this.devices.remove(device);
        device.getPatients().remove(this);
        return this;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
