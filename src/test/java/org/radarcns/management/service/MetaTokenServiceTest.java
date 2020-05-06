package org.radarcns.management.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.MetaToken;
import org.radarcns.management.repository.MetaTokenRepository;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.service.dto.TokenDTO;
import org.radarcns.management.service.mapper.SubjectMapper;
import org.radarcns.management.web.rest.errors.RadarWebApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
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


    @Before
    public void setUp() {
        subjectDto = SubjectServiceTest.createEntityDTO();
        subjectDto = subjectService.createSubject(subjectDto);

        clientDetails = oAuthClientService
                .createClientDetail(OauthClientServiceTest.createClient());
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

    @Test(expected = RadarWebApplicationException.class)
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
        metaTokenService.fetchToken(tokenName);
    }

    @Test(expected = RadarWebApplicationException.class)
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

        metaTokenService.fetchToken(tokenName);
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

        metaTokenRepository.save(Arrays.asList(tokenFetched, tokenExpired, tokenNew));

        metaTokenService.removeStaleTokens();

        List<MetaToken> availableTokens = metaTokenRepository.findAll();

        assertEquals(availableTokens.size(), 1);
    }
}
