package org.radarcns.management.service;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

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
    public TokenDTO fetchToken(String tokenName) throws CustomNotFoundException,
            MalformedURLException {
        log.debug("Request to get Token : {}", tokenName);
        MetaToken fetchedToken = getToken(tokenName);

        if (!fetchedToken.isFetched()) {
            // create response
            TokenDTO result = new TokenDTO(fetchedToken.getToken(),
                new URL(managementPortalProperties.getMail().getBaseUrl()));

            fetchedToken.fetched(true);
            save(fetchedToken);
            return result;
        } else {
            throw new CustomParameterizedException("invalidRequest", "token is already fetched. "
                + "Invalid request");
        }


    }

    @Transactional(readOnly = true)
    public MetaToken getToken(String tokenName) {
        Optional<MetaToken> fetchedToken = metaTokenRepository.findOneByTokenName(tokenName);

        if (fetchedToken.isPresent()) {
            return  fetchedToken.get();
        } else {
            throw  new CustomNotFoundException(
                ErrorConstants.ERR_TOKEN_NOT_FOUND,
                Collections.singletonMap("tokenName", tokenName));
        }

    }


}
