package org.radarcns.management.service;

import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.repository.MetaTokenRepository;
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
}
