package org.radarbase.management.domain;

import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.radarbase.management.domain.support.AbstractEntityListener;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A Subject.
 */
@Entity
@Audited
@Table(name = "subject")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class Subject extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @Column(name = "external_link")
    private String externalLink;

    @Column(name = "external_id")
    private String externalId;

    @NotNull
    @Column(name = "removed", nullable = false)
    private Boolean removed = false;

    @OneToOne
    @JoinColumn(unique = true, name = "user_id")
    @Cascade(CascadeType.ALL)
    private User user;

    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Cascade(CascadeType.SAVE_UPDATE)
    private Set<Source> sources = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "subject_metadata", joinColumns = @JoinColumn(name = "id"))
    @Cascade(CascadeType.ALL)
    private Map<String, String> attributes = new HashMap<>();

    @OneToMany(mappedBy = "subject", orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    private final Set<MetaToken> metaTokens = new HashSet<>();

    @Column(name = "group_name")
    private String group;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "enrollment_date")
    private ZonedDateTime enrollmentDate;

    @Column(name = "person_name")
    private String personName;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public Subject externalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getExternalId() {
        return externalId;
    }

    public Subject externalId(String enternalId) {
        this.externalId = enternalId;
        return this;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Boolean isRemoved() {
        return removed;
    }

    public Subject removed(Boolean removed) {
        this.removed = removed;
        return this;
    }

    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    public User getUser() {
        return user;
    }

    public Subject user(User usr) {
        this.user = usr;
        return this;
    }

    public void setUser(User usr) {
        this.user = usr;
    }

    public Set<Source> getSources() {
        return sources;
    }

    public Subject sources(Set<Source> sources) {
        this.sources = sources;
        return this;
    }

    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Set<MetaToken> getMetaTokens() {
        return metaTokens;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setEnrollmentDate(ZonedDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public ZonedDateTime getEnrollmentDate() {
        return this.enrollmentDate;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonName() {
        return this.personName;
    }

    /**
     * Gets the active project of subject.
     *
     * <p> There can be only one role with PARTICIPANT authority
     * and the project that is related to that role is the active role.</p>
     *
     * @return {@link Project} currently active project of subject.
     */
    public Optional<Project> getActiveProject() {
        return this.getUser().getRoles().stream()
                .filter(r -> r.getAuthority().getName().equals(PARTICIPANT))
                .findFirst()
                .map(Role::getProject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subject subject = (Subject) o;
        if (subject.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, subject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Subject{"
                + "id=" + id
                + ", externalLink='" + externalLink + '\''
                + ", externalId='" + externalId + '\''
                + ", removed=" + removed
                + ", user=" + user
                + ", sources=" + sources
                + ", attributes=" + attributes
                + ", group=" + group
                + "}";
    }
}
