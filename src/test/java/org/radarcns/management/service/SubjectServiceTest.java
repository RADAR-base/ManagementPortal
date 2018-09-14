package org.radarcns.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.radarcns.management.service.dto.ProjectDTO.PRIVACY_POLICY_URL;
import static org.radarcns.management.service.dto.SubjectDTO.SubjectStatus.ACTIVATED;
import static org.radarcns.management.web.rest.TestUtil.commitTransactionAndStartNew;

import java.net.URL;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalTestApp;
import org.radarcns.management.domain.Subject;
import org.radarcns.management.domain.User;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.repository.UserRepository;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

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

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private UserRepository userRepository;


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

    @Test
    public void testFindNotActivatedSubjectsByCreationDateBefore() {
        User expiredUser = UserServiceIntTest.addExpiredUser(userRepository);
        Subject expiredSubject = new Subject();
        expiredSubject.setUser(expiredUser);
        subjectRepository.save(expiredSubject);
        commitTransactionAndStartNew();

        AuditReader auditReader = ((AuditReader) ReflectionTestUtils
                .getField(revisionService, "auditReader"));
        Object[] firstRevision = (Object[]) auditReader.createQuery()
                .forRevisionsOfEntity(expiredUser.getClass(), false, true)
                .add(AuditEntity.id().eq(expiredUser.getId()))
                .add(AuditEntity.revisionNumber().minimize()
                        .computeAggregationInInstanceContext())
                .getSingleResult();
        CustomRevisionEntity first = (CustomRevisionEntity) firstRevision[1];
        // Update the timestamp of the revision so it appears to have been created 5 days ago
        ZonedDateTime expDateTime = ZonedDateTime.now().minus(Period.ofDays(5));
        first.setTimestamp(Date.from(expDateTime.toInstant()));
        EntityManager entityManager = ((EntityManager) ReflectionTestUtils
                .getField(revisionService, "entityManager"));
        entityManager.persist(first);

        // make sure when we reload the expired user we have the new created date
        assertThat(revisionService.getAuditInfo(expiredUser).getCreatedAt()).isEqualTo(expDateTime);

        // Now we know we have an 'old' user in the database, we can test our deletion method
        int numSubjects = subjectRepository.findAll().size();
        subjectService.removeNotActivatedSubjects();
        List<Subject> subjects = subjectRepository.findAll();
        // make sure have actually deleted some users, otherwise this test is pointless
        assertThat(numSubjects - subjects.size()).isEqualTo(1);
        // remaining users should be either activated or have a created date less then 3 days ago
        ZonedDateTime cutoff = ZonedDateTime.now().minus(Period.ofDays(3));
        subjects.forEach(s -> assertThat(s.getUser().getActivated() || revisionService
                .getAuditInfo(s).getCreatedAt().isAfter(cutoff)).isTrue());
        commitTransactionAndStartNew();
    }
}
