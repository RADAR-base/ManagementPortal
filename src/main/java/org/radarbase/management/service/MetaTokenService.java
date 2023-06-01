package org.radarbase.management.service;

import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.domain.MetaToken;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.repository.MetaTokenRepository;
import org.radarbase.management.security.NotAuthorizedException;
import org.radarbase.management.service.dto.ClientPairInfoDTO;
import org.radarbase.management.service.dto.TokenDTO;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.errors.InvalidStateException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.errors.RequestGoneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;

import static org.radarbase.management.domain.MetaToken.LONG_ID_LENGTH;
import static org.radarbase.management.domain.MetaToken.SHORT_ID_LENGTH;
import static org.radarbase.management.web.rest.MetaTokenResource.DEFAULT_META_TOKEN_TIMEOUT;
import static org.radarbase.management.web.rest.MetaTokenResource.DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT;
import static org.radarbase.management.web.rest.errors.EntityName.META_TOKEN;
import static org.radarbase.management.web.rest.errors.EntityName.OAUTH_CLIENT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PERSISTENT_TOKEN_DISABLED;

/**
 * Created by nivethika.
 *
 * <p>Service to delegate MetaToken handling.</p>
 *
 */
@Service
@Transactional
public class MetaTokenService {

    private static final Logger log = LoggerFactory.getLogger(MetaTokenService.class);

    @Autowired
    private MetaTokenRepository metaTokenRepository;

    @Autowired
    private OAuthClientService oAuthClientService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private SubjectService subjectService;

    /**
     * Save a metaToken.
     *
     * @param metaToken the entity to save
     * @return the persisted entity
     */
    public MetaToken save(MetaToken metaToken) {
        log.debug("Request to save MetaToken : {}", metaToken);
        return metaTokenRepository.save(metaToken);
    }

    /**
     * Get one project by id.
     *
     * @param tokenName the id of the entity
     * @return the entity
     */
    public TokenDTO fetchToken(String tokenName) throws MalformedURLException {
        log.debug("Request to get Token : {}", tokenName);
        MetaToken metaToken = getToken(tokenName);
        // process the response if the token is not fetched or not expired
        if (metaToken.isValid()) {
            String refreshToken = oAuthClientService.createAccessToken(
                    metaToken.getSubject().getUser(),
                    metaToken.getClientId())
                    .getRefreshToken()
                    .getValue();

            // create response
            TokenDTO result = new TokenDTO(refreshToken,
                    new URL(managementPortalProperties.getCommon().getBaseUrl()),
                    subjectService.getPrivacyPolicyUrl(metaToken.getSubject()));

            // change fetched status to true.
            if (!metaToken.isFetched()) {
                metaToken.fetched(true);
                save(metaToken);
            }
            return result;
        } else {
            throw new RequestGoneException("Token already fetched or expired. ",
                META_TOKEN, "error.TokenCannotBeSent");
        }
    }

    /**
     * Gets a token from databased using the tokenName.
     *
     * @param tokenName tokenName.
     * @return fetched token as {@link MetaToken}.
     */
    @Transactional(readOnly = true)
    public MetaToken getToken(String tokenName) {
        return metaTokenRepository.findOneByTokenName(tokenName)
                .orElseThrow(() -> new NotFoundException("Meta token not found with tokenName",
                        META_TOKEN,
                        ErrorConstants.ERR_TOKEN_NOT_FOUND,
                        Collections.singletonMap("tokenName", tokenName)));
    }

    /**
     * Saves a unique meta-token instance, by checking for token-name collision.
     * If a collision is detection, we try to save the token with a new tokenName
     * @return an unique token
     */
    public MetaToken saveUniqueToken(Subject subject, String clientId, Boolean
            fetched, Instant expiryTime, boolean persistent) {
        MetaToken metaToken = new MetaToken()
                .generateName(persistent ? LONG_ID_LENGTH : SHORT_ID_LENGTH)
                .fetched(fetched)
                .expiryDate(expiryTime)
                .subject(subject)
                .clientId(clientId)
                .persistent(persistent);

        try {
            return metaTokenRepository.save(metaToken);
        } catch (ConstraintViolationException e) {
            log.warn("Unique constraint violation catched... Trying to save with new tokenName");
            return saveUniqueToken(subject, clientId, fetched, expiryTime, persistent);
        }
    }

    /**
     * Creates meta token for oauth-subject pair.
     * @param subject to create token for
     * @param clientId using which client id
     * @param persistent whether to persist the token after it is has been fetched
     * @return {@link ClientPairInfoDTO} to return.
     * @throws URISyntaxException when token URI cannot be formed properly.
     * @throws MalformedURLException when token URL cannot be formed properly.
     */
    public ClientPairInfoDTO createMetaToken(Subject subject, String clientId, boolean persistent)
            throws URISyntaxException, MalformedURLException, NotAuthorizedException {
        Duration timeout = getMetaTokenTimeout(persistent, subject.getActiveProject()
                .orElseThrow(() -> new NotAuthorizedException(
                        "Cannot calculate meta-token duration without configured project")));

        // tokenName should be generated
        MetaToken metaToken = saveUniqueToken(subject, clientId, false,
                        Instant.now().plus(timeout), persistent);

        if (metaToken.getId() != null && metaToken.getTokenName() != null) {
            // get base url from settings
            String baseUrl = managementPortalProperties.getCommon().getManagementPortalBaseUrl();
            // create complete uri string
            String tokenUrl = baseUrl + ResourceUriService.getUri(metaToken).getPath();
            // create response
            return new ClientPairInfoDTO(new URL(baseUrl), metaToken.getTokenName(),
                    new URL(tokenUrl), timeout);
        } else {
            throw new InvalidStateException("Could not create a valid token", OAUTH_CLIENT,
                    "error.couldNotCreateToken");
        }
    }

    /**
     * Gets the meta-token timeout from config file. If the config is not mentioned or in wrong
     * format, it will return default value.
     *
     * @return meta-token timeout duration.
     * @throws BadRequestException if a persistent token is requested but it is not configured.
     */
    public Duration getMetaTokenTimeout(boolean persistent, Project project) {
        String timeoutConfig;
        Duration defaultTimeout;

        if (persistent) {
            timeoutConfig = managementPortalProperties.getOauth().getPersistentMetaTokenTimeout();
            if (timeoutConfig == null || timeoutConfig.isEmpty()) {
                throw new BadRequestException(
                        "Cannot create persistent token: not supported in configuration.",
                        META_TOKEN, ERR_PERSISTENT_TOKEN_DISABLED);
            }
            defaultTimeout = DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT;
        } else {
            timeoutConfig = managementPortalProperties.getOauth().getMetaTokenTimeout();
            defaultTimeout = DEFAULT_META_TOKEN_TIMEOUT;
            if (timeoutConfig == null || timeoutConfig.isEmpty()) {
                return defaultTimeout;
            }
        }

        try {
            return Duration.parse(timeoutConfig);
        } catch (DateTimeParseException e) {
            // if the token timeout cannot be read, log the error and use the default value.
            log.warn("Cannot parse meta-token timeout config. Using default value {}",
                    defaultTimeout, e);
            return defaultTimeout;
        }
    }

    /**
     * Expired and fetched tokens are deleted after 1 month.
     * <p> This is scheduled to get triggered first day of the month. </p>
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void removeStaleTokens() {
        log.info("Scheduled scan for expired and fetched meta-tokens starting now");

        metaTokenRepository.findAllByFetchedOrExpired(Instant.now())
                .forEach(metaToken -> {
                    log.info("Deleting deleting expired or fetched token {}",
                            metaToken.getTokenName());
                    metaTokenRepository.delete(metaToken);
                });
    }

    public void delete(MetaToken token) {
        metaTokenRepository.delete(token);
    }
}
