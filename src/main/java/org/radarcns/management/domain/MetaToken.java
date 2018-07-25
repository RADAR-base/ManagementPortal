package org.radarcns.management.domain;

import java.time.ZonedDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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


    //https://math.stackexchange.com/questions/889538/probability-of-collision-with-randomly
    // -generated-id
    // Current length of tokenName is 12. If we think there might be collision we can increase
    // the length.
    public static final int SHORT_ID_LENGTH = 12;

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
    private ZonedDateTime expiryDate = null;

    public MetaToken () {
        if (this.tokenName == null) {
            this.tokenName = RandomStringUtils.randomAlphanumeric(SHORT_ID_LENGTH);
        }
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

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public MetaToken tokenName(String tokenName) {
        this.tokenName = tokenName;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MetaToken token(String token) {
        this.token = token;
        return this;
    }

    public boolean isFetched() {
        return fetched;
    }

    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }

    public MetaToken isFetched(boolean fetched) {
        this.fetched = fetched;
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
        MetaToken token1 = (MetaToken) o;
        return Objects.equals(id, token1.id) && Objects.equals(tokenName, token1.tokenName)
            && Objects.equals(fetched, token1.fetched);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, tokenName, token, fetched);
    }

    @Override
    public String toString() {
        return "MetaToken{"
            + "id=" + id + ","
            + " tokenName='" + tokenName + '\''
            + ", token='" + token + '\''
            + ", fetched=" + fetched + '}';
    }
}
