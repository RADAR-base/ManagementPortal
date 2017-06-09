package org.radarcns.management.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CascadeType;

/**
 * A Patient.
 */
@Entity
@Table(name = "patient")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "external_id")
    private String externalId;

    @NotNull
    @Column(name = "removed" , nullable = false)
    private Boolean removed = false;

    @OneToOne
    @JoinColumn(unique = true, name = "user_id")
    @Cascade(CascadeType.ALL)
    private User user;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "patient_devices",
               joinColumns = @JoinColumn(name="patients_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="devices_id", referencedColumnName="id"))
    private Set<Device> devices = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public Patient externalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getExternalId() {
        return externalId;
    }

    public Patient externalId(String enternalId) {
        this.externalId = enternalId;
        return this;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Boolean isRemoved() {
        return removed;
    }

    public Patient removed(Boolean removed) {
        this.removed = removed;
        return this;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public User getUser() {
        return user;
    }

    public Patient user(User usr) {
        this.user = usr;
        return this;
    }

    public void setUser(User usr) {
        this.user = usr;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public Patient devices(Set<Device> devices) {
        this.devices = devices;
        return this;
    }

    public Patient addDevices(Device device) {
        this.devices.add(device);
        device.getPatients().add(this);
        return this;
    }

    public Patient removeDevices(Device device) {
        this.devices.remove(device);
        device.getPatients().remove(this);
        return this;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Patient patient = (Patient) o;
        if (patient.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
            "id=" + id +
            ", externalLink='" + externalLink + "'" +
            ", enternalId='" + externalId + "'" +
            ", removed='" + removed + "'" +
            '}';
    }
}
