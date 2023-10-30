/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.service

import org.radarbase.management.domain.Group
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.GroupRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.service.dto.GroupDTO
import org.radarbase.management.service.mapper.GroupMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.ConflictException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.vm.GroupPatchOperation.SubjectPatchValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

/**
 * Service to manage project groups.
 */
@Service
open class GroupService(
    @Autowired private val groupRepository: GroupRepository,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val groupMapper: GroupMapper
) {

    /**
     * Get the group by name.
     * @param projectName project name
     * @param groupName group name
     * @return group
     * @throws NotFoundException if the project or group is not found.
     */
    @Throws(NotFoundException::class)
    @Transactional
    open fun getGroup(projectName: String, groupName: String): GroupDTO {
        return groupMapper.groupToGroupDTOFull(
            groupRepository.findByProjectNameAndName(
                projectName, groupName
            ) ?: throw NotFoundException(
                "Group $groupName not found in project $projectName",
                EntityName.GROUP,
                ErrorConstants.ERR_GROUP_NOT_FOUND
            )
        )!!
    }

    /**
     * Delete the group by name.
     * @param projectName project name
     * @param groupName group name
     * @param unlinkSubjects unset group for each linked subject
     * @throws NotFoundException if the project or group is not found.
     */
    @Transactional
    open fun deleteGroup(projectName: String, groupName: String, unlinkSubjects: Boolean) {
        val group = groupRepository.findByProjectNameAndName(projectName, groupName) ?: throw NotFoundException(
            "Group $groupName not found in project $projectName", EntityName.GROUP, ErrorConstants.ERR_GROUP_NOT_FOUND
        )
        if (!unlinkSubjects) {
            val subjectCount = subjectRepository.countByGroupId(group.id)
            if (subjectCount > 0) {
                val msg =
                    ("Group " + groupName + " has subjects. " + "Add `unlinkSubjects=true` query param to confirm deletion")
                throw ConflictException(msg, EntityName.GROUP, ErrorConstants.ERR_VALIDATION)
            }
        }
        groupRepository.delete(group)
    }

    /**
     * Create the group.
     * @param projectName project name
     * @param groupDto group values
     * @throws NotFoundException if the project is not found.
     * @throws ConflictException if the group name already exists.
     */
    @Transactional
    open fun createGroup(projectName: String, groupDto: GroupDTO): GroupDTO? {
        val project = projectRepository.findOneWithGroupsByName(projectName) ?: throw NotFoundException(
            "Project with name $projectName not found", EntityName.PROJECT, ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND
        )
        if (project.groups.stream().anyMatch { g: Group -> g.name == groupDto.name }) {
            throw ConflictException(
                "Group " + groupDto.name + " already exists in project " + projectName,
                EntityName.GROUP,
                ErrorConstants.ERR_GROUP_EXISTS
            )
        }
        val group = groupMapper.groupDTOToGroup(groupDto)
        if (group != null) {
            group.project = project
            val groupDtoResult = groupMapper.groupToGroupDTOFull(groupRepository.save(group))
            project.groups.add(group)
            projectRepository.save(project)
            return groupDtoResult
        }
        else {
            throw NotFoundException(
                "Group ${groupDto.name} not found in project $projectName", EntityName.GROUP, ErrorConstants.ERR_GROUP_NOT_FOUND
            )
        }
    }

    /**
     * List all groups in a project.
     * @param projectName project name
     * @throws NotFoundException if the project is not found.
     */
    fun listGroups(projectName: String): List<GroupDTO> {
        val project = projectRepository.findOneWithGroupsByName(projectName) ?: throw NotFoundException(
            "Project with name $projectName not found", EntityName.PROJECT, ErrorConstants.ERR_PROJECT_NAME_NOT_FOUND
        )
        return groupMapper.groupToGroupDTOs(project.groups)
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
    open fun updateGroupSubjects(
        projectName: String,
        groupName: String,
        subjectsToAdd: List<SubjectPatchValue>,
        subjectsToRemove: List<SubjectPatchValue>
    ) {

        groupRepository ?: throw NullPointerException()

        val group = groupRepository.findByProjectNameAndName(projectName, groupName) ?: throw NotFoundException(
            "Group $groupName not found in project $projectName", EntityName.GROUP, ErrorConstants.ERR_GROUP_NOT_FOUND
        )

        val entitiesToAdd = getSubjectEntities(projectName, subjectsToAdd)
        val entitiesToRemove = getSubjectEntities(projectName, subjectsToRemove)
        if (entitiesToAdd.isNotEmpty()) {
            val idsToAdd = entitiesToAdd.mapNotNull(Subject::id).toList()
            subjectRepository.setGroupIdByIds(group.id!!, idsToAdd)
        }
        if (entitiesToRemove.isNotEmpty()) {
            val idsToRemove = entitiesToRemove.mapNotNull(Subject::id)
            subjectRepository.unsetGroupIdByIds(idsToRemove)
        }
    }

    private fun getSubjectEntities(
        projectName: String, subjectsToModify: List<SubjectPatchValue>
    ): List<Subject> {
        val logins: MutableList<String> = ArrayList()
        val ids: MutableList<Long> = ArrayList()
        extractSubjectIdentities(subjectsToModify, logins, ids)
        val subjectEntities: MutableList<Subject> = ArrayList(subjectsToModify.size)
        if (ids.isNotEmpty()) {
            subjectEntities.addAll(subjectRepository.findAllById(ids))
        }
        if (logins.isNotEmpty()) {
            subjectEntities.addAll(subjectRepository.findAllBySubjectLogins(logins))
        }
        for (s in subjectEntities) {
            val login = s.user!!.login
            s.activeProject ?: throw BadRequestException(
                "Subject $login is not assigned to a project", EntityName.SUBJECT, ErrorConstants.ERR_VALIDATION
            )
            if (projectName != s.activeProject!!.projectName) {
                throw BadRequestException(
                    "Subject $login belongs to a different project", EntityName.SUBJECT, ErrorConstants.ERR_VALIDATION
                )
            }
        }
        return subjectEntities
    }

    private fun extractSubjectIdentities(
        subjectsToModify: List<SubjectPatchValue>, logins: MutableList<String>, ids: MutableList<Long>
    ) {
        // Each item should specify either a login or an ID,
        // since having both will require an extra validation step
        // to reject e.g. {id: 1, login: "subject-id-42"}.
        // Whether the IDs and logins exist and belong to the project
        // should be checked later
        for (item in subjectsToModify) {
            val login = item.login
            val id = item.id
            if (id == null && login == null) {
                throw BadRequestException(
                    "Subject identification must be specified", EntityName.GROUP, ErrorConstants.ERR_VALIDATION
                )
            }
            if (id != null && login != null) {
                throw BadRequestException(
                    "Subject identification must be specify either ID or Login. " + "Do not provide both values to avoid potential confusion.",
                    EntityName.GROUP,
                    ErrorConstants.ERR_VALIDATION
                )
            }
            if (id != null) {
                ids.add(id)
            }
            if (login != null) {
                logins.add(login)
            }
        }
    }
}
