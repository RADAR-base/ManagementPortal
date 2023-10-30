package org.radarbase.management.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.dto.SubjectDTO.SubjectStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Test class for the SubjectService class.
 *
 * @see SubjectService
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
open class SubjectServiceTest(
    @Autowired private val subjectService: SubjectService,
    @Autowired private val projectService: ProjectService
) {

    @Test
    @Transactional
    open fun testGetPrivacyPolicyUrl() {
        projectService.save(createEntityDTO().project!!)
        val created = subjectService.createSubject(createEntityDTO())
        Assertions.assertNotNull(created!!.id)
        val subject = subjectService.findOneByLogin(created.login)
        Assertions.assertNotNull(subject)
        val privacyPolicyUrl = subjectService.getPrivacyPolicyUrl(subject)
        Assertions.assertNotNull(privacyPolicyUrl)
        Assertions.assertEquals(DEFAULT_PROJECT_PRIVACY_POLICY_URL, privacyPolicyUrl.toExternalForm())
    }

    companion object {
        const val DEFAULT_EXTERNAL_LINK = "AAAAAAAAAA"
        const val UPDATED_EXTERNAL_LINK = "BBBBBBBBBB"
        const val DEFAULT_ENTERNAL_ID = "AAAAAAAAAA"
        const val UPDATED_ENTERNAL_ID = "BBBBBBBBBB"
        const val DEFAULT_REMOVED = false
        const val UPDATED_REMOVED = true
        val DEFAULT_STATUS = SubjectStatus.ACTIVATED
        const val MODEL = "App"
        const val PRODUCER = "THINC-IT"
        const val DEFAULT_PROJECT_PRIVACY_POLICY_URL = "http://info.thehyve.nl/radar-cns-privacy-policy"

        /**
         * Create an entity for this test.
         *
         *
         * This is a static method, as tests for other entities might also need it, if they test an
         * entity which requires the current entity.
         */
        fun createEntityDTO(): SubjectDTO {
            val subject = SubjectDTO()
            subject.externalLink = DEFAULT_EXTERNAL_LINK
            subject.externalId = DEFAULT_ENTERNAL_ID
            subject.status = SubjectStatus.ACTIVATED
            val projectDto = ProjectDTO()
            projectDto.id = 1L
            projectDto.projectName = "Radar"
            projectDto.location = "SOMEWHERE"
            projectDto.description = "test"
            projectDto.attributes = Collections.singletonMap(
                ProjectDTO.PRIVACY_POLICY_URL,
                DEFAULT_PROJECT_PRIVACY_POLICY_URL
            )
            subject.project = projectDto
            return subject
        }
    }
}
