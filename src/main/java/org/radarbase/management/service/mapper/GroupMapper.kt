/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.service.mapper

import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.Named
import org.radarbase.management.domain.Group
import org.radarbase.management.service.dto.GroupDTO

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface GroupMapper {
    @Named("groupToGroupDTO")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "name", source = "name")
    fun groupToGroupDTO(group: Group): GroupDTO

    @Named("groupToGroupDTOFull")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(target = "name", source = "name")
    fun groupToGroupDTOFull(group: Group): GroupDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project.id", ignore = true)
    @Mapping(target = "name", source = "name")
    fun groupDTOToGroup(groupDto: GroupDTO): Group

    @IterableMapping(qualifiedByName = ["groupToGroupDTOFull"])
    fun groupToGroupDTOs(groups: Collection<Group>): List<GroupDTO>
}
