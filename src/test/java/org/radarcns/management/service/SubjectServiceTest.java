package org.radarcns.management.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.management.service.dto.ProjectDTO.PRIVACY_POLICY_URL;
import static org.radarcns.management.service.dto.SubjectDTO.SubjectStatus.ACTIVATED;

import java.net.URL;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalTestApp.class)
@Transactional
public class SubjectServiceTest {

    public static final String DEFAULT_EXTERNAL_LINK = "AAAAAAAAAA";
    public static final String UPDATED_EXTERNAL_LINK = "BBBBBBBBBB";

    public static final String DEFAULT_ENTERNAL_ID = "AAAAAAAAAA";
    public static final String UPDATED_ENTERNAL_ID = "BBBBBBBBBB";

    public static final Boolean DEFAULT_REMOVED = false;
    public static final Boolean UPDATED_REMOVED = true;

    public static final SubjectDTO.SubjectStatus DEFAULT_STATUS = ACTIVATED;

    public static final String MODEL = "App";
    public static final String PRODUCER = "THINC-IT";

    public static final String DEFAULT_PROJECT_PRIVACY_POLICY_URL =
            "http://info.thehyve.nl/radar-cns-privacy-policy";


    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ProjectService projectService;


    /**
     * Create an entity for this test.
     *
     * <p>This is a static method, as tests for other entities might also need it, if they test an
     * entity which requires the current entity.</p>
     */
    public static SubjectDTO createEntityDTO() {
        SubjectDTO subject = new SubjectDTO();
        subject.setExternalLink(DEFAULT_EXTERNAL_LINK);
        subject.setExternalId(DEFAULT_ENTERNAL_ID);
        subject.setStatus(ACTIVATED);
        ProjectDTO projectDto = new ProjectDTO();
        projectDto.setId(1L);
        projectDto.setProjectName("Radar");
        projectDto.setLocation("SOMEWHERE");
        projectDto.setDescription("test");
        projectDto.setAttributes(Collections.singletonMap(PRIVACY_POLICY_URL,
                DEFAULT_PROJECT_PRIVACY_POLICY_URL));
        subject.setProject(projectDto);

        return subject;
    }

    @Test
    @Transactional
    public void testGetPrivacyPolicyUrl() {

        projectService.save(createEntityDTO().getProject());
        SubjectDTO created = subjectService.createSubject(createEntityDTO());
        assertNotNull(created.getId());

        Subject subject = subjectService.findOneByLogin(created.getLogin());
        assertNotNull(subject);

        URL privacyPolicyUrl = subjectService.getPrivacyPolicyUrl(subject);
        assertNotNull(privacyPolicyUrl);
        assertEquals(privacyPolicyUrl.toExternalForm(), DEFAULT_PROJECT_PRIVACY_POLICY_URL);

    }
}
