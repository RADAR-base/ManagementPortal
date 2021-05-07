package org.radarbase.management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.management.ManagementPortalTestApp;
import org.radarbase.management.domain.MetaToken;
import org.radarbase.management.repository.MetaTokenRepository;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.dto.TokenDTO;
import org.radarbase.management.service.mapper.SubjectMapper;
import org.radarbase.management.web.rest.errors.RadarWebApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for the MetaTokenService class.
 *
 * @see MetaTokenService
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class MetaTokenServiceTest {


    @Autowired
    private MetaTokenService metaTokenService;

    @Autowired
    private MetaTokenRepository metaTokenRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private OAuthClientService oAuthClientService;

    private ClientDetails clientDetails;

    private SubjectDTO subjectDto;

    @BeforeEach
    public void setUp() {
        subjectDto = SubjectServiceTest.createEntityDTO();
        subjectDto = subjectService.createSubject(subjectDto);

        clientDetails = oAuthClientService
                .createClientDetail(OAuthClientServiceTest.createClient());
    }

    @Test
    public void testSaveThenFetchMetaToken() throws MalformedURLException {

        MetaToken metaToken = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(false)
                .persistent(false)
                .expiryDate(Instant.now().plus(Duration.ofHours(1)))
                .subject(subjectMapper.subjectDTOToSubject(subjectDto))
                .clientId(clientDetails.getClientId());

        MetaToken saved = metaTokenService.save(metaToken);
        assertNotNull(saved.getId());
        assertNotNull(saved.getTokenName());
        assertFalse(saved.isFetched());
        assertTrue(saved.getExpiryDate().isAfter(Instant.now()));

        String tokenName = saved.getTokenName();
        TokenDTO fetchedToken = metaTokenService.fetchToken(tokenName);

        assertNotNull(fetchedToken);
        assertNotNull(fetchedToken.getRefreshToken());

    }

    @Test
    public void testGetAFetchedMetaToken() throws MalformedURLException {
        MetaToken token = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(true)
                .persistent(false)
                .tokenName("something")
                .expiryDate(Instant.now().plus(Duration.ofHours(1)))
                .subject(subjectMapper.subjectDTOToSubject(subjectDto));

        MetaToken saved = metaTokenService.save(token);
        assertNotNull(saved.getId());
        assertNotNull(saved.getTokenName());
        assertTrue(saved.isFetched());
        assertTrue(saved.getExpiryDate().isAfter(Instant.now()));

        String tokenName = saved.getTokenName();
        Assertions.assertThrows(RadarWebApplicationException.class,
                () -> metaTokenService.fetchToken(tokenName));
    }

    @Test
    public void testGetAnExpiredMetaToken() throws MalformedURLException {
        MetaToken token = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(false)
                .persistent(false)
                .tokenName("somethingelse")
                .expiryDate(Instant.now().minus(Duration.ofHours(1)))
                .subject(subjectMapper.subjectDTOToSubject(subjectDto));

        MetaToken saved = metaTokenService.save(token);

        assertNotNull(saved.getId());
        assertNotNull(saved.getTokenName());
        assertFalse(saved.isFetched());
        assertTrue(saved.getExpiryDate().isBefore(Instant.now()));

        String tokenName = saved.getTokenName();

        Assertions.assertThrows(RadarWebApplicationException.class,
                () -> metaTokenService.fetchToken(tokenName));
    }

    @Test
    public void testRemovingExpiredMetaToken() {

        MetaToken tokenFetched = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(true)
                .persistent(false)
                .tokenName("something")
                .expiryDate(Instant.now().plus(Duration.ofHours(1)));

        MetaToken tokenExpired = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(false)
                .persistent(false)
                .tokenName("somethingelse")
                .expiryDate(Instant.now().minus(Duration.ofHours(1)));

        MetaToken tokenNew = new MetaToken()
                .generateName(MetaToken.SHORT_ID_LENGTH)
                .fetched(false)
                .persistent(false)
                .tokenName("somethingelseandelse")
                .expiryDate(Instant.now().plus(Duration.ofHours(1)));

        metaTokenRepository.saveAll(Arrays.asList(tokenFetched, tokenExpired, tokenNew));

        metaTokenService.removeStaleTokens();

        List<MetaToken> availableTokens = metaTokenRepository.findAll();

        assertEquals(availableTokens.size(), 1);
    }
}
