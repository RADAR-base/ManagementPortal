/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest;

import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.management.security.Constants;
import org.radarbase.management.service.AuthService;
import org.radarbase.management.service.GroupService;
import org.radarbase.management.service.dto.GroupDTO;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.vm.GroupPatchOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.PROJECT_READ;
import static org.radarbase.auth.authorization.Permission.PROJECT_UPDATE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarbase.management.web.rest.errors.EntityName.GROUP;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_VALIDATION;

@RestController
@RequestMapping("/api/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/groups")
public class GroupResource {
    @Autowired
    private GroupService groupService;

    @Autowired
    private AuthService authService;

    /**
     * Create group.
     * @param projectName project name
     * @param groupDto group specification
     * @return created response
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(
            @PathVariable String projectName,
            @Valid @RequestBody GroupDTO groupDto) throws NotAuthorizedException {
        authService.checkPermission(PROJECT_UPDATE, e -> e.project(projectName));
        GroupDTO groupDtoResult = groupService.createGroup(projectName, groupDto);
        URI location = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{groupName}")
                .buildAndExpand(projectName, groupDtoResult.getName())
                .toUri();
        return ResponseEntity.created(location)
                .body(groupDtoResult);
    }

    /**
     * List groups.
     * @param projectName project name
     * @return list of groups
     * @throws NotAuthorizedException if PROJECT_READ permissions are not present.
     */
    @GetMapping
    public List<GroupDTO> listGroups(
            @PathVariable String projectName) throws NotAuthorizedException {
        authService.checkPermission(PROJECT_READ, e -> e.project(projectName));
        return groupService.listGroups(projectName);
    }

    /**
     * Get a single group.
     * @param projectName project name
     * @param groupName group name
     * @return group
     * @throws NotAuthorizedException if PROJECT_READ permissions are not present.
     */
    @GetMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}")
    public GroupDTO getGroup(
            @PathVariable String projectName,
            @PathVariable String groupName) throws NotAuthorizedException {
        authService.checkPermission(PROJECT_READ, e -> e.project(projectName));
        return groupService.getGroup(projectName, groupName);
    }

    /**
     * Delete a single group.
     * @param projectName project name
     * @param groupName group name
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @DeleteMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}")
    public ResponseEntity<?> deleteGroup(
            @RequestParam(defaultValue = "false") Boolean unlinkSubjects,
            @PathVariable String projectName,
            @PathVariable String groupName) throws NotAuthorizedException {
        authService.checkPermission(PROJECT_UPDATE, e -> e.project(projectName));
        groupService.deleteGroup(projectName, groupName, unlinkSubjects);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add subjects to a single group.
     * @param projectName project name
     * @param groupName group name
     * @param patchOperations json-patch request body
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @PatchMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}/subjects")
    public ResponseEntity<?> changeGroupSubjects(
            @PathVariable String projectName,
            @PathVariable String groupName,
            @RequestBody List<GroupPatchOperation> patchOperations) throws NotAuthorizedException {
        // Technically, this request modifies subjects,
        // so it would make sense to check permissions per subject,
        // but I assume that only those who are authorized to perform project-wide actions
        // should be allowed to use this endpoint
        authService.checkPermission(SUBJECT_UPDATE, e -> e.project(projectName));

        var addedItems = new ArrayList<GroupPatchOperation.SubjectPatchValue>();
        var removedItems = new ArrayList<GroupPatchOperation.SubjectPatchValue>();
        for (GroupPatchOperation operation : patchOperations) {
            String opCode = operation.getOp();
            switch (opCode) {
                case "add":
                    addedItems.addAll(operation.getValue());
                    break;
                case "remove":
                    removedItems.addAll(operation.getValue());
                    break;
                default:
                    throw new BadRequestException(
                            "Group patch operation '" + opCode + "' is not supported",
                            GROUP, ERR_VALIDATION);
            }
        }

        groupService.updateGroupSubjects(projectName, groupName, addedItems, removedItems);
        return ResponseEntity.noContent().build();
    }
}
