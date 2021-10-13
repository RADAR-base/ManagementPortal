/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.repository;

import org.radarbase.management.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@RepositoryDefinition(domainClass = Group.class, idClass = Long.class)
public interface GroupRepository extends JpaRepository<Group, Long>,
        RevisionRepository<Group, Long, Integer> {
    @Query("SELECT group FROM Group group "
            + "WHERE group.project.id = :project_id "
            + "AND group.name = :group_name")
    Optional<Group> findByProjectIdAndName(
            @Param("project_id") Long id,
            @Param("group_name") String groupName);

    @Query("SELECT group FROM Group group "
            + "LEFT JOIN Project project on group.project = project "
            + "WHERE group.project.projectName = :project_name "
            + "AND group.name = :group_name")
    Optional<Group> findByProjectNameAndName(
            @Param("project_name") String projectName,
            @Param("group_name") String groupName);
}
