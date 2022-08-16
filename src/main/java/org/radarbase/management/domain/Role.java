/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.FetchType;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.domain.support.AbstractEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Role.
 */
@Entity
@Audited
@Table(name = "radar_role")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class Role extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000,
            sequenceName = "hibernate_sequence")
    private Long id;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<User> users = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Organization organization;

    @JsonProperty()
    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinColumn(name = "authority_name", referencedColumnName = "name")
    private Authority authority;

    public Role() {
        // constructor for reflection
    }

    public Role(Authority authority, Project project) {
        this.authority = authority;
        this.project = project;
    }

    public Role(Authority authority) {
        this.authority = authority;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Role users(Set<User> users) {
        this.users = users;
        return this;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Project getProject() {
        return project;
    }

    public Role project(Project project) {
        this.project = project;
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Authority getAuthority() {
        return authority;
    }

    public RoleAuthority getRole() {
        return RoleAuthority.valueOfAuthorityOrNull(authority.getName());
    }

    public Role authority(Authority authority) {
        this.authority = authority;
        return this;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(authority, role.authority)
                && Objects.equals(project, role.project)
                && Objects.equals(organization, role.organization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority, project, organization);
    }

    @Override
    public String toString() {
        return "Role{"
               + "id=" + id + ", "
                + "organization='" + (organization == null ? "null" : organization.getName())
                + "', "
                + "project='" + (project == null ? "null" : project.getProjectName()) + "', "
               + "authority='" + getAuthority().getName() + "', "
               + "}";
    }

}
