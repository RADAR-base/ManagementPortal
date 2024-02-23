/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.repository

import org.radarbase.management.domain.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param

@RepositoryDefinition(domainClass = Group::class, idClass = Long::class)
interface GroupRepository : JpaRepository<Group?, Long?>, RevisionRepository<Group?, Long?, Int> {
    @Query(
        "SELECT group FROM Group group "
                + "WHERE group.project.id = :project_id "
                + "AND group.name = :group_name"
    )
    fun findByProjectIdAndName(
        @Param("project_id") id: Long?,
        @Param("group_name") groupName: String?
    ): Group?

    @Query(
        "SELECT group FROM Group group "
                + "LEFT JOIN Project project on group.project = project "
                + "WHERE group.project.projectName = :project_name "
                + "AND group.name = :group_name"
    )
    fun findByProjectNameAndName(
        @Param("project_name") projectName: String?,
        @Param("group_name") groupName: String?
    ): Group?
}
