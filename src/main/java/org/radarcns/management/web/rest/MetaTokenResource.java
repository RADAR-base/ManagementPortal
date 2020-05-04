package org.radarcns.management.web.rest;


import java.net.MalformedURLException;
import java.time.Duration;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.config.Constants;
import org.radarcns.management.service.MetaTokenService;
import org.radarcns.management.service.dto.ClientPairInfoDTO;
import org.radarcns.management.service.dto.TokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetaTokenResource {

    private final Logger log = LoggerFactory.getLogger(OAuthClientsResource.class);

    public static final Duration DEFAULT_META_TOKEN_TIMEOUT = Duration.ofHours(1);
    public static final Duration DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT = Duration.ofDays(365);

    @Autowired
    private MetaTokenService metaTokenService;

    /**
     * GET /api/meta-token/:tokenName.
     *
     * <p>Get refresh-token available under this tokenName.</p>
     *
     * @param tokenName the tokenName given after pairing the subject with client
     * @return the client as a {@link ClientPairInfoDTO}
     */
    @GetMapping("/meta-token/{tokenName:" + Constants.TOKEN_NAME_REGEX + "}")
    @Timed
    public ResponseEntity<TokenDTO> getTokenByTokenName(@PathVariable("tokenName") String tokenName)
            throws MalformedURLException {
        log.info("Requesting token with tokenName {}", tokenName);
        return ResponseEntity.ok().body(metaTokenService.fetchToken(tokenName));
    }
}
