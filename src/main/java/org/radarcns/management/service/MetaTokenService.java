package org.radarcns.management.service;


import static org.radarcns.management.domain.MetaToken.SHORT_ID_LENGTH;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.radarcns.management.config.ManagementPortalProperties;
import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.repository.MetaTokenRepository;
import org.radarcns.management.service.dto.TokenDTO;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomParameterizedException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
     * @throws CustomNotFoundException if there is no project with the given id
     */
    public TokenDTO fetchToken(String tokenName) throws
            MalformedURLException, CustomNotFoundException {
        log.debug("Request to get Token : {}", tokenName);
        MetaToken fetchedToken = getToken(tokenName);

        // process the response if the token is not fetched or not expired
        if (!fetchedToken.isFetched() && Instant.now().isBefore(fetchedToken.getExpiryDate())) {
            // create response
            TokenDTO result = new TokenDTO(fetchedToken.getToken(),
                    new URL(managementPortalProperties.getCommon().getBaseUrl()));
            // change fetched status to true.
            fetchedToken.fetched(true);
            save(fetchedToken);
            return result;
        } else {
            throw new CustomParameterizedException("invalidRequest", "token is already fetched "
                + "or has expired. Invalid request");
        }


    }

    /**
     * Gets a token from databased using the tokenName.
     *
     * @param tokenName tokenName.
     * @throws CustomNotFoundException if the token not found.
     * @return fetched token as {@link MetaToken}.
     */
    @Transactional(readOnly = true)
    public MetaToken getToken(String tokenName) throws CustomNotFoundException {
        Optional<MetaToken> fetchedToken = metaTokenRepository.findOneByTokenName(tokenName);

        if (fetchedToken.isPresent()) {
            return  fetchedToken.get();
        } else {
            throw  new CustomNotFoundException(
                ErrorConstants.ERR_TOKEN_NOT_FOUND,
                Collections.singletonMap("tokenName", tokenName));
        }
    }

    /**
     * Builds a unique meta-token instance, by checking for token-name collision.
     * @return an unique token
     */
    public MetaToken buildUniqueToken() {
        MetaToken token = new MetaToken();

        while (metaTokenRepository.findOneByTokenName(token.getTokenName()).isPresent()) {
            token.tokenName(RandomStringUtils.randomAlphanumeric(SHORT_ID_LENGTH));
        }

        return token;
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
