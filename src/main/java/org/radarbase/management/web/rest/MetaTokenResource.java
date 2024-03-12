package org.radarbase.management.web.rest;


import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.MetaToken;
import org.radarbase.management.domain.Subject;
import org.radarbase.management.service.MetaTokenService;
import org.radarbase.management.service.dto.ClientPairInfoDTO;
import org.radarbase.management.service.dto.TokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.time.Duration;

import static org.radarbase.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnSubject;

@RestController
@RequestMapping("/api")
public class MetaTokenResource {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientsResource.class);

    public static final Duration DEFAULT_META_TOKEN_TIMEOUT = Duration.ofHours(1);
    public static final Duration DEFAULT_PERSISTENT_META_TOKEN_TIMEOUT = Duration.ofDays(31);

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

    /**
     * DELETE /api/meta-token/:tokenName.
     *
     * <p>Delete refresh-token available under this tokenName.</p>
     *
     * @param tokenName the tokenName given after pairing the subject with client
     * @return the client as a {@link ClientPairInfoDTO}
     */
    @DeleteMapping("/meta-token/{tokenName:" + Constants.TOKEN_NAME_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteTokenByTokenName(
            @PathVariable("tokenName") String tokenName, RadarToken token)
            throws NotAuthorizedException {
        log.info("Requesting token with tokenName {}", tokenName);
        MetaToken metaToken = metaTokenService.getToken(tokenName);
        Subject subject = metaToken.getSubject();
        String project = subject.getActiveProject()
                .orElseThrow(() -> new NotAuthorizedException(
                        "Cannot establish authority of subject without active project affiliation."
                ))
                .getProjectName();
        String user = subject.getUser().getLogin();
        checkPermissionOnSubject(token, SUBJECT_UPDATE, project, user);
        metaTokenService.delete(metaToken);
        return ResponseEntity.noContent().build();
    }
}
