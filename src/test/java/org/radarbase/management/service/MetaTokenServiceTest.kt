package org.radarbase.management.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.MetaToken
import org.radarbase.management.repository.MetaTokenRepository
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.errors.RadarWebApplicationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.net.MalformedURLException
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Test class for the MetaTokenService class.
 *
 * @see MetaTokenService
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
internal class MetaTokenServiceTest(
    @Autowired private val metaTokenService: MetaTokenService,
    @Autowired private val metaTokenRepository: MetaTokenRepository,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val oAuthClientService: OAuthClientService,
) {
    private lateinit var clientDetails: ClientDetails
    private lateinit var subjectDto: SubjectDTO

    @BeforeEach
    fun setUp() {
        subjectDto = SubjectServiceTest.createEntityDTO()
        subjectDto = subjectService.createSubject(subjectDto)!!
        clientDetails = oAuthClientService.createClientDetail(OAuthClientServiceTestUtil.createClient())
    }

    @Test
    @Throws(MalformedURLException::class)
    fun testSaveThenFetchMetaToken() {
        val metaToken = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(false)
            .persistent(false)
            .expiryDate(Instant.now().plus(Duration.ofHours(1)))
            .subject(subjectMapper.subjectDTOToSubject(subjectDto))
            .clientId(clientDetails.clientId)
        val saved = metaTokenService.save(metaToken)
        Assertions.assertNotNull(saved.id)
        Assertions.assertNotNull(saved.tokenName)
        Assertions.assertFalse(saved.isFetched())
        Assertions.assertTrue(saved.expiryDate!!.isAfter(Instant.now()))
        val tokenName = saved.tokenName
        val fetchedToken = metaTokenService.fetchToken(tokenName!!)
        Assertions.assertNotNull(fetchedToken)
        Assertions.assertNotNull(fetchedToken.refreshToken)
    }

    @Test
    @Throws(MalformedURLException::class)
    fun testGetAFetchedMetaToken() {
        val token = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(true)
            .persistent(false)
            .tokenName("something")
            .expiryDate(Instant.now().plus(Duration.ofHours(1)))
            .subject(subjectMapper.subjectDTOToSubject(subjectDto))
        val saved = metaTokenService.save(token)
        Assertions.assertNotNull(saved.id)
        Assertions.assertNotNull(saved.tokenName)
        Assertions.assertTrue(saved.isFetched())
        Assertions.assertTrue(saved.expiryDate!!.isAfter(Instant.now()))
        val tokenName = saved.tokenName
        Assertions.assertThrows(
            RadarWebApplicationException::class.java
        ) { metaTokenService.fetchToken(tokenName!!) }
    }

    @Test
    @Throws(MalformedURLException::class)
    fun testGetAnExpiredMetaToken() {
        val token = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(false)
            .persistent(false)
            .tokenName("somethingelse")
            .expiryDate(Instant.now().minus(Duration.ofHours(1)))
            .subject(subjectMapper.subjectDTOToSubject(subjectDto))
        val saved = metaTokenService.save(token)
        Assertions.assertNotNull(saved.id)
        Assertions.assertNotNull(saved.tokenName)
        Assertions.assertFalse(saved.isFetched())
        Assertions.assertTrue(saved.expiryDate!!.isBefore(Instant.now()))
        val tokenName = saved.tokenName
        Assertions.assertThrows(
            RadarWebApplicationException::class.java
        ) { metaTokenService.fetchToken(tokenName!!) }
    }

    @Test
    fun testRemovingExpiredMetaToken() {
        val tokenFetched = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(true)
            .persistent(false)
            .tokenName("something")
            .expiryDate(Instant.now().plus(Duration.ofHours(1)))
        val tokenExpired = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(false)
            .persistent(false)
            .tokenName("somethingelse")
            .expiryDate(Instant.now().minus(Duration.ofHours(1)))
        val tokenNew = MetaToken()
            .generateName(MetaToken.SHORT_ID_LENGTH)
            .fetched(false)
            .persistent(false)
            .tokenName("somethingelseandelse")
            .expiryDate(Instant.now().plus(Duration.ofHours(1)))
        metaTokenRepository.saveAll(Arrays.asList(tokenFetched, tokenExpired, tokenNew))
        metaTokenService.removeStaleTokens()
        val availableTokens = metaTokenRepository.findAll()
        Assertions.assertEquals(1, availableTokens.size)
    }
}
