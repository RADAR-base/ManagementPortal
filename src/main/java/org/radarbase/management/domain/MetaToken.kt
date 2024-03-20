package org.radarbase.management.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.time.Instant
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

@Entity
@Audited
@Table(name = "radar_meta_token")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class MetaToken : AbstractEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @Column(name = "token_name", nullable = false, unique = true)
    @NotNull @Pattern(regexp = Constants.TOKEN_NAME_REGEX) var tokenName: String? = null
        private set

    @Column(name = "fetched", nullable = false)
    private var fetched: Boolean? = null

    @Column(name = "expiry_date")
    var expiryDate: Instant? = null
        private set

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var subject: Subject? = null
        private set

    @Column(name = "client_id", nullable = false)
    var clientId: String? = null
        private set

    @Column(name = "persistent", nullable = false)
    private var persistent: Boolean? = null

    /**
     * Generates a alphanumeric with '.' and '-' token name. Suggested token name lengths are
     * [.SHORT_ID_LENGTH] for short-living tokens and [.LONG_ID_LENGTH] for long-living
     * meta tokens.
     *
     * @param length token length
     * @return this
     */
    fun generateName(length: Int): MetaToken {
        val random = ThreadLocalRandom.current()
        val tokenChars = CharArray(length)
        for (i in 0 until length) {
            tokenChars[i] = ID_CHARS[random.nextInt(ID_CHARS.size)]
        }
        tokenName = String(tokenChars)
        return this
    }

    fun tokenName(tokenName: String?): MetaToken {
        this.tokenName = tokenName
        return this
    }

    fun isFetched(): Boolean {
        return fetched!!
    }

    fun fetched(fetched: Boolean): MetaToken {
        this.fetched = fetched
        return this
    }

    fun expiryDate(expiryDate: Instant?): MetaToken {
        this.expiryDate = expiryDate
        return this
    }

    fun subject(subject: Subject?): MetaToken {
        this.subject = subject
        return this
    }

    fun clientId(clientId: String?): MetaToken {
        this.clientId = clientId
        return this
    }

    val isValid: Boolean
        get() = (persistent!! || !fetched!!) && Instant.now().isBefore(expiryDate)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val metaToken = other as MetaToken
        return id == metaToken.id && tokenName == metaToken.tokenName && fetched == metaToken.fetched && expiryDate == metaToken.expiryDate && clientId == metaToken.clientId && subject == metaToken.subject && persistent == metaToken.persistent
    }

    override fun hashCode(): Int {
        return Objects.hash(id, tokenName, fetched, expiryDate, subject, clientId, persistent)
    }

    override fun toString(): String {
        return ("MetaToken{" + "id=" + id
                + ", tokenName='" + tokenName
                + ", fetched=" + fetched
                + ", expiryDate=" + expiryDate
                + ", subject=" + subject
                + ", clientId=" + clientId
                + ", persistent=" + persistent
                + '}')
    }

    fun isPersistent(): Boolean {
        return persistent!!
    }

    fun persistent(persistent: Boolean): MetaToken {
        this.persistent = persistent
        return this
    }

    companion object {
        private val ID_CHARS = charArrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '-', '.'
        )

        //https://math.stackexchange.com/questions/889538/
        // probability-of-collision-with-randomly-generated-id
        // Current length of tokenName is 8 for short-lived tokens, and double that
        // for persistent tokens.
        const val SHORT_ID_LENGTH = 8
        const val LONG_ID_LENGTH = 16
    }
}
