/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.web.rest;

import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.management.service.GroupService;
import org.radarbase.management.service.dto.GroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.PROJECT_READ;
import static org.radarbase.auth.authorization.Permission.PROJECT_UPDATE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnSubject;
import static org.radarbase.management.security.SecurityUtils.getJWT;

@RestController
@RequestMapping("/api/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/groups")
public class GroupResource {
    @Autowired
    private GroupService groupService;

    @Autowired
    private ServletRequest servletRequest;

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
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_UPDATE, projectName);
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
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectName);
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
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectName);
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
            @PathVariable String projectName,
            @PathVariable String groupName) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_UPDATE, projectName);
        groupService.deleteGroup(projectName, groupName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add subjects to a single group.
     * @param projectName project name
     * @param groupName group name
     * @param subjects subject logins
     * @throws NotAuthorizedException if PROJECT_UPDATE permissions are not present.
     */
    @PostMapping("/{groupName:" + Constants.ENTITY_ID_REGEX + "}/subjects/new")
    public ResponseEntity<?> addSubjectsToGroup(
            @PathVariable String projectName,
            @PathVariable String groupName,
            @RequestBody List<String> subjects) throws NotAuthorizedException {
        for (String subjectLogin : subjects) {
            checkPermissionOnSubject(getJWT(servletRequest), SUBJECT_UPDATE,
                projectName, subjectLogin);
        }
        groupService.addSubjectsToGroup(projectName, groupName, subjects);
        return ResponseEntity.noContent().build();
    }
}
