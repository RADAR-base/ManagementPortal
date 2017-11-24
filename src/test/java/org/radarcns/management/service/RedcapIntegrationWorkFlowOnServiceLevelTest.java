package org.radarcns.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.enumeration.ProjectStatus;
import org.radarcns.management.service.dto.AttributeMapDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika on 31-8-17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class RedcapIntegrationWorkFlowOnServiceLevelTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SubjectService subjectService;

    @Test
    public void testRedcapIntegrationWorkFlowOnServiceLevel() {
        String externalProjectUrl = "MyUrl";
        String externalProjectId = "MyId";
        String projectLocation = "London";
        String workPackage = "MDD";
        String phase = "1";


        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setDescription("Test Project");
        projectDTO.setLocation(projectLocation);
        projectDTO.setProjectName("test radar");
        projectDTO.setProjectStatus(ProjectStatus.PLANNING);
        Set<AttributeMapDTO> projectMetaData = new HashSet<>();
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.EXTERNAL_PROJECT_URL_KEY, externalProjectUrl));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.EXTERNAL_PROJECT_ID_KEY, externalProjectId));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.PHASE_KEY, phase));
        projectMetaData.add(new AttributeMapDTO(ProjectDTO.WORK_PACKAGE_KEY, workPackage));
        projectDTO.setAttributes(projectMetaData);

        // manually save
        ProjectDTO saved = projectService.save(projectDTO);
        Long storedProjectId = saved.getId();
        assertThat(storedProjectId>0);

        // Use ROLE_EXTERNAL_ERF_INTEGRATOR authority in your oauth2 client config
        // GET api/projects/{storedProjectId}
        ProjectDTO retrievedById = projectService.findOne(storedProjectId);
        assertEquals(retrievedById.getId(), storedProjectId);

        // retrieve required details
        // location is part of project property
        String locationRetrieved = projectDTO.getLocation();

        // work-package, phase are from meta-data
        String workPackageRetrieved ="";
        String phaseRetrieved ="";

        // redcap-id from trigger
        String redcapRecordId = "1";
        for(AttributeMapDTO attributeMapDTO : retrievedById.getAttributes()) {
            switch (attributeMapDTO.getKey()) {
                case ProjectDTO.WORK_PACKAGE_KEY :
                    workPackageRetrieved = attributeMapDTO.getValue();
                    break;
                case ProjectDTO.PHASE_KEY :
                    phaseRetrieved = attributeMapDTO.getValue();
                    break;
            }
        }

        // assert retrieved data
        assertEquals(workPackage ,workPackageRetrieved);
        assertEquals(phase, phaseRetrieved);
        assertEquals(projectLocation ,locationRetrieved);

        // create a new Subject
        SubjectDTO newSubject = new SubjectDTO();
        newSubject.setLogin("53d8a54a"); // will be removed
        newSubject.setProject(retrievedById); // set retrieved project
        newSubject.setExternalId(redcapRecordId); // set redcap-record-id

        // create human-readable-id
        String humanReadableId = workPackageRetrieved+'-'+phaseRetrieved+"-"+locationRetrieved+"-"+redcapRecordId;

        //set meta-data to subject
        newSubject.setAttributes(Collections.singletonMap(SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY, humanReadableId));

        // create/save a subject
        // PUT api/subjects/
        SubjectDTO savedSubject = subjectService.createSubject(newSubject);
        assertThat(savedSubject.getId()>0);

        // asset human-readable-id
        for (Map.Entry<String, String> attr : savedSubject.getAttributes().entrySet()) {
            switch (attr.getKey()) {
                case SubjectDTO.HUMAN_READABLE_IDENTIFIER_KEY :
                    assertEquals(humanReadableId, attr.getValue());
            }
        }
    }
}
