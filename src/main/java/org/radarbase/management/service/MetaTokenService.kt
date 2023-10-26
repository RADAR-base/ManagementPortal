package org.radarbase.management.service

import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.MetaToken
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.MetaTokenRepository
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.ClientPairInfoDTO
import org.radarbase.management.service.dto.TokenDTO
import org.radarbase.management.web.rest.MetaTokenResource
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.InvalidStateException
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.errors.RequestGoneException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.Consumer
import javax.validation.ConstraintViolationException

/**
 * Created by nivethika.
 *
 *
 * Service to delegate MetaToken handling.
 *
 */
@Service
@Transactional
open class MetaTokenService {
    @Autowired
    private val metaTokenRepository: MetaTokenRepository? = null

    @Autowired
    private val oAuthClientService: OAuthClientService? = null

    @Autowired
    private val managementPortalProperties: ManagementPortalProperties? = null

    @Autowired
    private val subjectService: SubjectService? = null

    /**
     * Save a metaToken.
     *
     * @param metaToken the entity to save
     * @return the persisted entity
     */
    fun save(metaToken: MetaToken): MetaToken {
        log.debug("Request to save MetaToken : {}", metaToken)
        return metaTokenRepository!!.save(metaToken)
    }

    /**
     * Get one project by id.
     *
     * @param tokenName the id of the entity
     * @return the entity
     */
    @Throws(MalformedURLException::class)
    fun fetchToken(tokenName: String): TokenDTO {
        log.debug("Request to get Token : {}", tokenName)
        val metaToken = getToken(tokenName)
        // process the response if the token is not fetched or not expired
        return if (metaToken.isValid) {
            val refreshToken = oAuthClientService!!.createAccessToken(
                metaToken.subject!!.user,
                metaToken.clientId
            )
                .refreshToken
                .value

            // create response
            val result = TokenDTO(
                refreshToken,
                URL(managementPortalProperties!!.common.baseUrl),
                subjectService!!.getPrivacyPolicyUrl(metaToken.subject!!)
            )

            // change fetched status to true.
            if (!metaToken.isFetched()) {
                metaToken.fetched(true)
                save(metaToken)
            }
            result
        } else {
            throw RequestGoneException(
                "Token $tokenName already fetched or expired. ",
                EntityName.META_TOKEN, "error.TokenCannotBeSent"
            )
        }
    }

    /**
     * Gets a token from databased using the tokenName.
     *
     * @param tokenName tokenName.
     * @return fetched token as [MetaToken].
     */
    @Transactional(readOnly = true)
    open fun getToken(tokenName: String): MetaToken {
        return metaTokenRepository!!.findOneByTokenName(tokenName)
            .orElseThrow {
                NotFoundException(
                    "Meta token not found with tokenName",
                    EntityName.META_TOKEN,
                    ErrorConstants.ERR_TOKEN_NOT_FOUND,
                    Collections.singletonMap("tokenName", tokenName)
                )
            }
    }

    /**
     * Saves a unique meta-token instance, by checking for token-name collision.
     * If a collision is detection, we try to save the token with a new tokenName
     * @return an unique token
     */
    fun saveUniqueToken(
        subject: Subject?,
        clientId: String?,
        fetched: Boolean?,
        expiryTime: Instant?,
        persistent: Boolean
    ): MetaToken {
        val metaToken = MetaToken()
            .generateName(if (persistent) MetaToken.LONG_ID_LENGTH else MetaToken.SHORT_ID_LENGTH)
            .fetched(fetched!!)
            .expiryDate(expiryTime)
            .subject(subject)
            .clientId(clientId)
            .persistent(persistent)
        return try {
            metaTokenRepository!!.save(metaToken)
        } catch (e: ConstraintViolationException) {
            log.warn("Unique constraint violation catched... Trying to save with new tokenName")
            saveUniqueToken(subject, clientId, fetched, expiryTime, persistent)
        }
    }

    /**
     * Creates meta token for oauth-subject pair.
     * @param subject to create token for
     * @param clientId using which client id
     * @param persistent whether to persist the token after it is has been fetched
     * @return [ClientPairInfoDTO] to return.
     * @throws URISyntaxException when token URI cannot be formed properly.
     * @throws MalformedURLException when token URL cannot be formed properly.
     */
    @Throws(URISyntaxException::class, MalformedURLException::class, NotAuthorizedException::class)
    fun createMetaToken(subject: Subject, clientId: String?, persistent: Boolean): ClientPairInfoDTO {
        val timeout = getMetaTokenTimeout(persistent, project = subject.activeProject
            ?:throw NotAuthorizedException("Cannot calculate meta-token duration without configured project")
        )

        // tokenName should be generated
        val metaToken = saveUniqueToken(
            subject, clientId, false,
            Instant.now().plus(timeout), persistent
        )
        return if (metaToken.id != null && metaToken.tokenName != null) {
            // get base url from settings
            val baseUrl = managementPortalProperties!!.common.managementPortalBaseUrl
            // create complete uri string
            val tokenUrl = baseUrl + ResourceUriService.getUri(metaToken).getPath()
            // create response
            ClientPairInfoDTO(
                URL(baseUrl), metaToken.tokenName,
                URL(tokenUrl), timeout
            )
        } else {
            throw InvalidStateException(
                "Could not create a valid token", EntityName.OAUTH_CLIENT,
                "error.couldNotCreateToken"
            )
        }
    }

    /**
     * Gets the meta-token timeout from config file. If the config is not mentioned or in wrong
     * format, it will return default value.
     *
     * @return meta-token timeout duration.
     * @throws BadRequestException if a persistent token is requested but it is not configured.
     */
    fun getMetaTokenTimeout(persistent: Boolean, project: Project?): Duration {
        val timeoutConfig: String?
        val defaultTimeout: Duration
        if (persistent) {
            timeoutConfig = managementPortalProperties!!.oauth.persistentMetaTokenTimeout
            if (timeoutConfig == null || timeoutConfig.isEmpty()) {
                throw BadRequestException(
                    "Cannot create persistent token: not supported in configuration.",
                    EntityName.META_TOKEN, ErrorConstants.ERR_PERSISTENT_TOKEN_DISABLED
                )
            }
            defaultTimeout = MetaTokenResource.DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT
        } else {
            timeoutConfig = managementPortalProperties!!.oauth.metaTokenTimeout
            defaultTimeout = MetaTokenResource.DEFAULT_META_TOKEN_TIMEOUT
            if (timeoutConfig == null || timeoutConfig.isEmpty()) {
                return defaultTimeout
            }
        }
        return try {
            Duration.parse(timeoutConfig)
        } catch (e: DateTimeParseException) {
            // if the token timeout cannot be read, log the error and use the default value.
            log.warn(
                "Cannot parse meta-token timeout config. Using default value {}",
                defaultTimeout, e
            )
            defaultTimeout
        }
    }

    /**
     * Expired and fetched tokens are deleted after 1 month.
     *
     *  This is scheduled to get triggered first day of the month.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    fun removeStaleTokens() {
        log.info("Scheduled scan for expired and fetched meta-tokens starting now")
        metaTokenRepository!!.findAllByFetchedOrExpired(Instant.now())
            .forEach(Consumer { metaToken: MetaToken ->
                log.info(
                    "Deleting deleting expired or fetched token {}",
                    metaToken.tokenName
                )
                metaTokenRepository.delete(metaToken)
            })
    }

    fun delete(token: MetaToken) {
        metaTokenRepository!!.delete(token)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MetaTokenService::class.java)
    }
}
