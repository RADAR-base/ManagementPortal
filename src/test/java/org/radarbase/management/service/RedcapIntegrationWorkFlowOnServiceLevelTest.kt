package org.radarbase.management.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.management.ManagementPortalTestApp
import org.radarbase.management.domain.enumeration.ProjectStatus
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Created by nivethika on 31-8-17.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ManagementPortalTestApp::class])
@Transactional
internal class RedcapIntegrationWorkFlowOnServiceLevelTest {
    @Autowired
    private val projectService: ProjectService? = null

    @Autowired
    private val subjectService: SubjectService? = null
    @Test
    suspend fun testRedcapIntegrationWorkFlowOnServiceLevel() {
        val externalProjectUrl = "MyUrl"
        val externalProjectId = "MyId"
        val projectLocation = "London"
        val workPackage = "MDD"
        val phase = "1"
        val projectDto = ProjectDTO()
        projectDto.description = "Test Project"
        projectDto.location = projectLocation
        projectDto.projectName = "test radar"
        projectDto.projectStatus = ProjectStatus.PLANNING
        val attributes: MutableMap<String, String> = HashMap()
        attributes[ProjectDTO.EXTERNAL_PROJECT_URL_KEY] = externalProjectUrl
        attributes[ProjectDTO.EXTERNAL_PROJECT_ID_KEY] = externalProjectId
        attributes[ProjectDTO.PHASE_KEY] = phase
        attributes[ProjectDTO.WORK_PACKAGE_KEY] = workPackage
        projectDto.attributes = attributes

        // manually save
        val saved = projectService!!.save(projectDto)
        val storedProjectId = saved.id!!
        Assertions.assertTrue(storedProjectId > 0)

        // Use ROLE_EXTERNAL_ERF_INTEGRATOR authority in your oauth2 client config
        // GET api/projects/{storedProjectId}
        val retrievedById = projectService.findOne(storedProjectId)
        Assertions.assertEquals(retrievedById?.id, storedProjectId)

        // retrieve required details
        // location is part of project property
        val locationRetrieved = projectDto.location

        // work-package, phase are from meta-data
        var workPackageRetrieved = ""
        var phaseRetrieved = ""

        // redcap-id from trigger
        val redcapRecordId = "1"
        for ((key, value) in retrievedById!!.attributes!!) {
            when (key) {
                ProjectDTO.WORK_PACKAGE_KEY -> workPackageRetrieved = value
                ProjectDTO.PHASE_KEY -> phaseRetrieved = value
                else -> {}
            }
        }

        // assert retrieved data
        Assertions.assertEquals(workPackage, workPackageRetrieved)
        Assertions.assertEquals(phase, phaseRetrieved)
        Assertions.assertEquals(projectLocation, locationRetrieved)

        // create a new Subject
        val newSubject = SubjectDTO()
        newSubject.login = "53d8a54a" // will be removed
        newSubject.project = retrievedById // set retrieved project
        newSubject.externalId = redcapRecordId // set redcap-record-id

        // create human-readable-id
        val humanReadableId = java.lang.String.join(
            "-", workPackageRetrieved, phaseRetrieved,
            locationRetrieved, redcapRecordId
        )

        //set meta-data to subject
        newSubject.attributes = Collections.singletonMap(
            SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY,
            humanReadableId
        )

        // create/save a subject
        // PUT api/subjects/
        val savedSubject = subjectService!!.createSubject(newSubject)
        Assertions.assertTrue(savedSubject!!.id!! > 0)

        // asset human-readable-id
        for ((key, value) in savedSubject.attributes) {
            if (SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY == key) {
                Assertions.assertEquals(humanReadableId, value)
            }
        }
    }
}
