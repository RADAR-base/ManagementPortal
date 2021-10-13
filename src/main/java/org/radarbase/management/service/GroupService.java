/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.service;

import org.radarbase.management.domain.Group;
import org.radarbase.management.domain.Project;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.service.dto.GroupDTO;
import org.radarbase.management.service.mapper.GroupMapper;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static org.radarbase.management.web.rest.errors.EntityName.GROUP;
import static org.radarbase.management.web.rest.errors.EntityName.PROJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_GROUP_EXISTS;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_GROUP_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND;

/**
 * Service to manage project groups.
 */
@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupMapper groupMapper;

    /**
     * Get the group by name.
     * @param projectName project name
     * @param groupName group name
     * @return group
     * @throws NotFoundException if the project or group is not found.
     */
    @Transactional
    public GroupDTO getGroup(String projectName, String groupName) {
        return groupMapper.groupToGroupDTOFull(groupRepository.findByProjectNameAndName(projectName,
                        groupName)
                .orElseThrow(() -> new NotFoundException(
                        "Group " + groupName + " not found in project " + projectName,
                        GROUP, ERR_GROUP_NOT_FOUND)));
    }

    /**
     * Delete the group by name.
     * @param projectName project name
     * @param groupName group name
     * @throws NotFoundException if the project or group is not found.
     */
    @Transactional
    public void deleteGroup(String projectName, String groupName) {
        Group group = groupRepository.findByProjectNameAndName(projectName, groupName)
                .orElseThrow(() -> new NotFoundException(
                        "Group " + groupName + " not found in project " + projectName,
                        GROUP, ERR_GROUP_NOT_FOUND));

        groupRepository.delete(group);
    }

    /**
     * Create the group.
     * @param projectName project name
     * @param groupDto group values
     * @throws NotFoundException if the project is not found.
     * @throws ConflictException if the group name already exists.
     */
    @Transactional
    public GroupDTO createGroup(String projectName, GroupDTO groupDto) {
        Project project = projectRepository.findOneWithGroupsByName(projectName)
                .orElseThrow(() -> new NotFoundException(
                        "Project with name " + projectName + " not found",
                        PROJECT, ERR_PROJECT_NAME_NOT_FOUND));

        if (project.getGroups().stream()
                .anyMatch(g -> g.getName().equals(groupDto.getName()))) {
            throw new ConflictException(
                    "Group " + groupDto.getName() + " already exists in project " + projectName,
                    GROUP, ERR_GROUP_EXISTS);
        }
        Group group = groupMapper.groupDTOToGroup(groupDto);
        group.setProject(project);

        GroupDTO groupDtoResult = groupMapper.groupToGroupDTOFull(groupRepository.save(group));
        project.getGroups().add(group);
        projectRepository.save(project);
        return groupDtoResult;
    }

    /**
     * List all groups in a project.
     * @param projectName project name
     * @throws NotFoundException if the project is not found.
     */
    public List<GroupDTO> listGroups(String projectName) {
        Project project = projectRepository.findOneWithGroupsByName(projectName)
                .orElseThrow(() -> new NotFoundException(
                        "Project with name " + projectName + " not found",
                        PROJECT, ERR_PROJECT_NAME_NOT_FOUND));
        return groupMapper.groupToGroupDTOs(project.getGroups());
    }
}
