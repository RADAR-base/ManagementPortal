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
import org.radarbase.management.domain.Subject;
import org.radarbase.management.repository.GroupRepository;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.service.dto.GroupDTO;
import org.radarbase.management.service.mapper.GroupMapper;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ConflictException;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.vm.GroupPatchOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.radarbase.management.web.rest.errors.EntityName.GROUP;
import static org.radarbase.management.web.rest.errors.EntityName.PROJECT;
import static org.radarbase.management.web.rest.errors.EntityName.SUBJECT;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_GROUP_EXISTS;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_GROUP_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

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
    private SubjectRepository subjectRepository;

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

    /**
     * Add subjects to group.
     * @param projectName project name
     * @param groupName group name
     * @param subjectsToAdd patch items for subjects to be added
     * @param subjectsToRemove patch items for subjects to be removed
     * @throws NotFoundException if the project or group is not found.
     */
    @Transactional
    public void updateGroupSubjects(
            String projectName, String groupName,
            List<GroupPatchOperation.SubjectPatchValue> subjectsToAdd,
            List<GroupPatchOperation.SubjectPatchValue> subjectsToRemove
    ) {
        Group group = groupRepository
                .findByProjectNameAndName(projectName, groupName)
                .orElseThrow(() -> new NotFoundException(
                    "Group " + groupName + " not found in project " + projectName,
                    GROUP, ERR_GROUP_NOT_FOUND));

        List<Subject> entitiesToAdd = getSubjectEntities(projectName, subjectsToAdd);
        List<Subject> entitiesToRemove = getSubjectEntities(projectName, subjectsToRemove);

        if (!entitiesToAdd.isEmpty()) {
            List<Long> idsToAdd = entitiesToAdd.stream()
                    .map(Subject::getId).collect(Collectors.toList());
            subjectRepository.setGroupIdByIds(group.getId(), idsToAdd);
        }

        if (!entitiesToRemove.isEmpty()) {
            List<Long> idsToRemove = entitiesToRemove.stream()
                    .map(Subject::getId).collect(Collectors.toList());
            subjectRepository.unsetGroupIdByIds(idsToRemove);
        }
    }

    private List<Subject> getSubjectEntities(
            String projectName,
            List<GroupPatchOperation.SubjectPatchValue> subjectsToModify
    ) {
        List<String> logins = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        extractSubjectIdentities(subjectsToModify, logins, ids);

        List<Subject> subjectEntities = new ArrayList<>(subjectsToModify.size());
        if (!ids.isEmpty()) {
            subjectEntities.addAll(subjectRepository.findAllById(ids));
        }
        if (!logins.isEmpty()) {
            subjectEntities.addAll(subjectRepository.findAllBySubjectLogins(logins));
        }

        for (Subject s : subjectEntities) {
            String login = s.getUser().getLogin();
            if (s.getActiveProject().isEmpty()) {
                throw new BadRequestException(
                    "Subject " + login + " is not assigned to a project",
                    SUBJECT, ERR_VALIDATION);
            }
            if (!projectName.equals(s.getActiveProject().get().getProjectName())) {
                throw new BadRequestException(
                    "Subject " + login + " belongs to a different project",
                    SUBJECT, ERR_VALIDATION);
            }
        }

        return subjectEntities;
    }

    private void extractSubjectIdentities(
            List<GroupPatchOperation.SubjectPatchValue> subjectsToModify,
            List<String> logins,
            List<Long> ids
    ) {
        // Each item should specify either a login or an ID,
        // since having both will require an extra validation step
        // to reject e.g. {id: 1, login: "subject-id-42"}.
        // Whether the IDs and logins exist and belong to the project
        // should be checked later
        for (var item : subjectsToModify) {
            String login = item.getLogin();
            Long id = item.getId();
            if (id == null && login == null) {
                throw new BadRequestException(
                    "Subject identification must be specified",
                    GROUP, ERR_VALIDATION);
            }
            if (id != null && login != null) {
                throw new BadRequestException(
                    "Subject identification must be specify either ID or Login. "
                        + "Do not provide both values to avoid potential confusion.",
                    GROUP, ERR_VALIDATION);
            }

            if (id != null) {
                ids.add(id);
            }
            if (login != null) {
                logins.add(login);
            }
        }
    }
}
