package org.radarbase.management.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.radarbase.management.security.Constants;
import org.radarbase.management.domain.support.AbstractEntityListener;

@Entity
@Audited
@Table(name = "radar_meta_token")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners({AbstractEntityListener.class})
public class MetaToken extends AbstractEntity {
    private static final char[] ID_CHARS = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
        'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
        'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '-', '.'
    };

    //https://math.stackexchange.com/questions/889538/
    // probability-of-collision-with-randomly-generated-id
    // Current length of tokenName is 8 for short-lived tokens, and double that
    // for persistent tokens.
    public static final int SHORT_ID_LENGTH = 8;
    public static final int LONG_ID_LENGTH = 16;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000,
            sequenceName = "hibernate_sequence")
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.TOKEN_NAME_REGEX)
    @Column(name = "token_name", nullable = false, unique = true)
    private String tokenName;

    @Column(name = "fetched", nullable = false)
    private Boolean fetched;

    @Column(name = "expiry_date")
    private Instant expiryDate = null;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Subject subject;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "persistent", nullable = false)
    private Boolean persistent;

    /**
     * Generates a alphanumeric with '.' and '-' token name. Suggested token name lengths are
     * {@link #SHORT_ID_LENGTH} for short-living tokens and {@link #LONG_ID_LENGTH} for long-living
     * meta tokens.
     *
     * @param length token length
     * @return this
     */
    public MetaToken generateName(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] tokenChars = new char[length];
        for (int i = 0; i < length; i++) {
            tokenChars[i] = ID_CHARS[random.nextInt(ID_CHARS.length)];
        }
        tokenName = String.valueOf(tokenChars);
        return this;
    }

    @Override
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

    public String getClientId() {
        return clientId;
    }

    public MetaToken clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public boolean isValid() {
        return (persistent || !fetched) && Instant.now().isBefore(expiryDate);
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
                && Objects.equals(fetched, metaToken.fetched)
                && Objects.equals(expiryDate, metaToken.expiryDate)
                && Objects.equals(clientId, metaToken.clientId)
                && Objects.equals(subject, metaToken.subject)
                && Objects.equals(persistent, metaToken.persistent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tokenName, fetched, expiryDate, subject, clientId, persistent);
    }

    @Override
    public String toString() {
        return "MetaToken{" + "id=" + id
                + ", tokenName='" + tokenName
                + ", fetched=" + fetched
                + ", expiryDate=" + expiryDate
                + ", subject=" + subject
                + ", clientId=" + clientId
                + ", persistent=" + persistent
                + '}';
    }

    public boolean isPersistent() {
        return persistent;
    }

    public MetaToken persistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }
}
