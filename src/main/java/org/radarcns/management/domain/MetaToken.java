package org.radarcns.management.domain;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.domain.support.AbstractEntityListener;

@Entity
@Audited
@Table(name = "radar_meta_token")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class MetaToken extends AbstractEntity {


    //https://math.stackexchange.com/questions/889538/
    // probability-of-collision-with-randomly-generated-id
    // Current length of tokenName is 12. If we think there might be collision we can increase
    // the length.
    private static final int SHORT_ID_LENGTH = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.TOKEN_NAME_REGEX)
    @Column(name = "token_name", nullable = false, unique = true)
    private String tokenName;

    @Column(name = "token", length = 2000)
    private String token;

    @Column(name = "fetched", nullable = false)
    private Boolean fetched;

    @Column(name = "expiry_date")
    private Instant expiryDate = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Subject subject;

    /**
     * Meta token constructor.
     * Must generate a random string as the tokenName.
     */
    public MetaToken() {
        this.tokenName = RandomStringUtils.randomAlphanumeric(SHORT_ID_LENGTH);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenName() {
        return tokenName;
    }

    public MetaToken tokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public String getToken() {
        return token;
    }

    public MetaToken token(String token) {
        this.token = token;
        return this;
    }

    public boolean isFetched() {
        return fetched;
    }

    public MetaToken fetched(boolean fetched) {
        this.fetched = fetched;
        return this;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public MetaToken expiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public Subject getSubject() {
        return subject;
    }

    public MetaToken subject(Subject subject) {
        this.subject = subject;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetaToken metaToken = (MetaToken) o;
        return Objects.equals(id, metaToken.id)
            && Objects.equals(tokenName, metaToken.tokenName)
            && Objects.equals(token, metaToken.token)
            && Objects.equals(fetched, metaToken.fetched)
            && Objects.equals(expiryDate, metaToken.expiryDate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, tokenName, token, fetched, expiryDate);
    }

    @Override
    public String toString() {
        return "MetaToken{" + "id=" + id
                + ", tokenName='" + tokenName
                + ", token='" + token
                + ", fetched=" + fetched
                + ", expiryDate=" + expiryDate
                + ", subject=" + subject + '}';
    }
}
