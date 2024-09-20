/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.web.rest

import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.GroupService
import org.radarbase.management.service.dto.GroupDTO
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.vm.GroupPatchOperation
import org.radarbase.management.web.rest.vm.GroupPatchOperation.SubjectPatchValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import javax.validation.Valid

@RestController
@RequestMapping("/api/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/groups")
class GroupResource(
    @Autowired private val groupService: GroupService,
    @Autowired private val authService: AuthService,
) {
    /**
     * Create group.
     * @param projectName project name
     * @param groupDto group specification
     * @return created response
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @PostMapping
    @Throws(NotAuthorizedException::class)
    fun createGroup(
        @PathVariable projectName: String?,
        @RequestBody @Valid groupDto: GroupDTO?,
    ): ResponseEntity<GroupDTO> {
        authService.checkPermission(Permission.PROJECT_UPDATE, { e: EntityDetails -> e.project(projectName) })
        val groupDtoResult = groupService.createGroup(projectName!!, groupDto!!)
        val location =
            MvcUriComponentsBuilder
                .fromController(javaClass)
                .path("/{groupName}")
                .buildAndExpand(projectName, groupDtoResult.name)
                .toUri()
        return ResponseEntity
            .created(location)
            .body(groupDtoResult)
    }

    /**
     * List groups.
     * @param projectName project name
     * @return list of groups
     * @throws NotAuthorizedException if PROJECT_READ permissions are not present.
     */
    @GetMapping
    @Throws(NotAuthorizedException::class)
    fun listGroups(
        @PathVariable projectName: String?,
    ): List<GroupDTO> {
        authService.checkPermission(Permission.PROJECT_READ, { e: EntityDetails -> e.project(projectName) })
        return groupService.listGroups(projectName!!)
    }

    /**
     * Get a single group.
     * @param projectName project name
     * @param groupName group name
     * @return group
     * @throws NotAuthorizedException if PROJECT_READ permissions are not present.
     */
    @GetMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}")
    @Throws(
        NotAuthorizedException::class,
    )
    fun getGroup(
        @PathVariable projectName: String?,
        @PathVariable groupName: String?,
    ): GroupDTO {
        authService.checkPermission(Permission.PROJECT_READ, { e: EntityDetails -> e.project(projectName) })
        return groupService.getGroup(projectName!!, groupName!!)
    }

    /**
     * Delete a single group.
     * @param projectName project name
     * @param groupName group name
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @DeleteMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}")
    @Throws(
        NotAuthorizedException::class,
    )
    fun deleteGroup(
        @RequestParam(defaultValue = "false") unlinkSubjects: Boolean?,
        @PathVariable projectName: String?,
        @PathVariable groupName: String?,
    ): ResponseEntity<*> {
        authService.checkPermission(Permission.PROJECT_UPDATE, { e: EntityDetails -> e.project(projectName) })
        groupService.deleteGroup(projectName!!, groupName!!, unlinkSubjects!!)
        return ResponseEntity.noContent().build<Any>()
    }

    /**
     * Add subjects to a single group.
     * @param projectName project name
     * @param groupName group name
     * @param patchOperations json-patch request body
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @PatchMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}/subjects")
    @Throws(
        NotAuthorizedException::class,
    )
    fun changeGroupSubjects(
        @PathVariable projectName: String?,
        @PathVariable groupName: String?,
        @RequestBody patchOperations: List<GroupPatchOperation>,
    ): ResponseEntity<*> {
        // Technically, this request modifies subjects,
        // so it would make sense to check permissions per subject,
        // but I assume that only those who are authorized to perform project-wide actions
        // should be allowed to use this endpoint
        authService.checkPermission(Permission.SUBJECT_UPDATE, { e: EntityDetails -> e.project(projectName) })
        val addedItems = ArrayList<SubjectPatchValue?>()
        val removedItems = ArrayList<SubjectPatchValue?>()
        for (operation in patchOperations) {
            when (val opCode = operation.op) {
                "add" -> operation.value?.let { addedItems.addAll(it) }
                "remove" -> operation.value?.let { removedItems.addAll(it) }
                else -> throw BadRequestException(
                    "Group patch operation '$opCode' is not supported",
                    EntityName.Companion.GROUP,
                    ErrorConstants.ERR_VALIDATION,
                )
            }
        }
        groupService.updateGroupSubjects(
            projectName!!,
            groupName!!,
            addedItems.filterNotNull(),
            removedItems.filterNotNull(),
        )
        return ResponseEntity.noContent().build<Any>()
    }
}
