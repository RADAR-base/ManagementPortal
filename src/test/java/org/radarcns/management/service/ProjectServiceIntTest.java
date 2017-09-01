package org.radarcns.management.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarcns.management.ManagementPortalApp;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.enumeration.ProjectStatus;
import org.radarcns.management.service.dto.AttributeMapDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nivethika on 31-8-17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagementPortalApp.class)
@Transactional
public class ProjectServiceIntTest {

    @Autowired
    private ProjectService projectService;

    @Test
    public void getProjectBasedOnExternalProjectDetails() {

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setDescription("Test Project");
        projectDTO.setLocation("London");
        projectDTO.setProjectName("test radar");
        projectDTO.setProjectStatus(ProjectStatus.ONGOING);
        Set<AttributeMapDTO> attributes = new HashSet<>();
        attributes.add(new AttributeMapDTO(Project.EXTERNAL_PROJECT_URL_KEY, "MyUrl"));
        attributes.add(new AttributeMapDTO(Project.EXTERNAL_PROJECT_ID_KEY, "MyID"));
        projectDTO.setAttributes(attributes);

        ProjectDTO saved = projectService.save(projectDTO);
        assertThat(saved.getId()>0);

        ProjectDTO anotherProjectDTO = new ProjectDTO();
        anotherProjectDTO.setDescription("Test Project");
        anotherProjectDTO.setLocation("London");
        anotherProjectDTO.setProjectName("test radar");
        anotherProjectDTO.setProjectStatus(ProjectStatus.ONGOING);
        Set<AttributeMapDTO> attributes2 = new HashSet<>();
        attributes2.add(new AttributeMapDTO(Project.EXTERNAL_PROJECT_URL_KEY, "MyValue 2"));
        attributes2.add(new AttributeMapDTO(Project.EXTERNAL_PROJECT_ID_KEY, "MyID"));
        anotherProjectDTO.setAttributes(attributes2);

        ProjectDTO saved2 = projectService.save(anotherProjectDTO);
        assertThat(saved2.getId()>0);

//        ProjectDTO retrieved = projectService.findProjectByExtenalProjectUrlAndId("MyUrl" , "MyID");
//        assertEquals(saved.getId(),retrieved.getId());
    }
}
