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
import org.radarbase.management.service.ProjectService;
import org.radarbase.management.service.dto.GroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.PROJECT_READ;
import static org.radarbase.auth.authorization.Permission.PROJECT_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.management.security.SecurityUtils.getJWT;

@RestController
@RequestMapping("/api/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/groups")
public class GroupResource {
    @Autowired
    private GroupService groupService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ServletRequest servletRequest;

    @PostMapping
    public Response createGroup(
            @PathParam("projectName") String projectName,
            GroupDTO groupDto) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_UPDATE, projectName);
        groupService.createGroup(projectName, groupDto);
        return Response.created(URI.create(groupDto.getName())).build();
    }

    @GetMapping
    public List<GroupDTO> listGroups(
            @PathParam("projectName") String projectName) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectName);
        return groupService.listGroups(projectName);
    }

    @GetMapping("{groupName:" + Constants.ENTITY_ID_REGEX + "}")
    public GroupDTO getGroup(
            @PathParam("projectName") String projectName,
            @PathParam("groupName") String groupName) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectName);
        return groupService.getGroup(projectName, groupName);
    }

    @DeleteMapping
    public void deleteGroup(
            @PathParam("projectName") String projectName,
            @PathParam("groupName") String groupName) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_UPDATE, projectName);
        groupService.deleteGroup(projectName, groupName);
    }
}
