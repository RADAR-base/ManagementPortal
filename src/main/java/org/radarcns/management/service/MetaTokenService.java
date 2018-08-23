package org.radarcns.management.service;

import static org.radarcns.management.web.rest.errors.EntityName.META_TOKEN;
import static org.radarcns.management.web.rest.errors.EntityName.OAUTH_CLIENT;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.repository.MetaTokenRepository;
import org.radarcns.management.service.dto.TokenDTO;
import org.radarcns.management.web.rest.errors.RequestGoneException;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika.
 *
 * <p>Service to delegate MetaToken handling.</p>
 *
 */
@Service
@Transactional
public class MetaTokenService {

    private final Logger log = LoggerFactory.getLogger(MetaTokenService.class);

    @Autowired
    private MetaTokenRepository metaTokenRepository;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private OAuthClientService oAuthClientService;

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
    public TokenDTO fetchToken(String tokenName) throws
            MalformedURLException {
        log.debug("Request to get Token : {}", tokenName);
        MetaToken fetchedToken = getToken(tokenName);
        TokenDTO result = null;
        // process the response if the token is not fetched or not expired
        if (!fetchedToken.isFetched() && Instant.now().isBefore(fetchedToken.getExpiryDate())) {
            // create response

            ClientDetails clientDetails = oAuthClientService.findOneByClientId(fetchedToken
                    .getClientId());
            if (clientDetails != null) {
                result = new TokenDTO(fetchedToken.getToken(),
                        new URL(managementPortalProperties.getCommon().getBaseUrl()),
                        subjectService.getPrivacyPolicyUrl(fetchedToken.getSubject()),
                        clientDetails.getClientSecret());
            } else {
                throw new NotFoundException("Oauth client not found with client-id", OAUTH_CLIENT,
                        ErrorConstants.ERR_TOKEN_NOT_FOUND,
                        Collections.singletonMap("client-id", fetchedToken.getClientId()));
            }

            // change fetched status to true.
            fetchedToken.fetched(true);
            save(fetchedToken);
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
        Optional<MetaToken> fetchedToken = metaTokenRepository.findOneByTokenName(tokenName);

        if (fetchedToken.isPresent()) {
            return fetchedToken.get();
        } else {
            throw new NotFoundException("Meta token not found with tokenName", META_TOKEN,
                ErrorConstants.ERR_TOKEN_NOT_FOUND,
                Collections.singletonMap("tokenName", tokenName));
        }
    }

    /**
     * Saves a unique meta-token instance, by checking for token-name collision.
     * If a collision is detection, we try to save the token with a new tokenName
     * @return an unique token
     */
    public MetaToken saveUniqueToken(Subject subject, String clientId, String token, Boolean
            fetched, Instant expiryTime ) {
        MetaToken metaToken = new MetaToken()
                .token(token)
                .fetched(fetched)
                .expiryDate(expiryTime)
                .subject(subject)
                .clientId(clientId);

        try {
            return metaTokenRepository.save(metaToken);
        } catch (ConstraintViolationException e) {
            log.warn("Unique constraint violation catched... Trying to save with new tokenName");
            return saveUniqueToken(subject, clientId, token, fetched, expiryTime);
        }

    }

    /**
     * Expired and fetched tokens are deleted after 1 month.
     * <p> This is scheduled to get triggered first day of the month. </p>
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void removeStaleTokens() {
        log.info("Scheduled scan for expired and fetched meta-tokens starting now");

        metaTokenRepository.findAllByFetchedOrExpired(true, Instant.now())
                .forEach(metaToken -> {
                    log.info("Deleting deleting expired or fetched token {}",
                            metaToken.getTokenName());
                    metaTokenRepository.delete(metaToken);
                });
    }
}
