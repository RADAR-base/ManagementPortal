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
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional()
    public TokenDTO fetchToken(String tokenName) throws CustomNotFoundException,
        MalformedURLException {
        log.debug("Request to get Token : {}", tokenName);
        Optional<MetaToken> fetchedToken = metaTokenRepository.findOneByTokenName(tokenName);

        if(fetchedToken.isPresent()) {
            // get token from database
            MetaToken metaToken = fetchedToken.get();
            // create response
            TokenDTO result = new TokenDTO(metaToken.getToken() ,
                new URL(managementPortalProperties.getMail().getBaseUrl()));
            // change fetched status to true. This will be cleared later on.
            metaTokenRepository.save(metaToken.isFetched(true));
            // return result
            return result;
        }
        else {
            throw new CustomNotFoundException(
                ErrorConstants.ERR_TOKEN_NOT_FOUND,
                Collections.singletonMap("tokenName", tokenName));
        }

    }


}
