package org.radarcns.management.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * A Subject.
 */
@Entity
@Table(name = "subject")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Subject implements Serializable {

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
    @Column(name = "removed" , nullable = false)
    private Boolean removed = false;

    @OneToOne
    @JoinColumn(unique = true, name = "user_id")
    @Cascade(CascadeType.ALL)
    private User user;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "subject_sources",
               joinColumns = @JoinColumn(name="subjects_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="sources_id", referencedColumnName="id"))
    @Cascade(CascadeType.SAVE_UPDATE)
    private Set<Source> sources = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="attribute_key")
    @Column(name="attribute_value")
    @CollectionTable(name="subject_metadata" ,  joinColumns = @JoinColumn(name = "id"))
    Map<String, String> attributes = new HashMap<>();

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

    public Subject addSources(Source source) {
        this.sources.add(source);
        source.getSubjects().add(this);
        return this;
    }

    public Subject removeSources(Source source) {
        this.sources.remove(source);
        source.getSubjects().remove(this);
        return this;
    }

    public void setSources(Set<Source> sources) {
        this.sources = sources;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
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
        return "Subject{" +
            "id=" + id +
            ", externalLink='" + externalLink + "'" +
            ", enternalId='" + externalId + "'" +
            ", removed='" + removed + "'" +
            '}';
    }
}
