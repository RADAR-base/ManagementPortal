/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.management.service.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.radarbase.management.domain.Group;
import org.radarbase.management.service.dto.GroupDTO;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GroupMapper {
    @Named("groupToGroupDTO")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "name", source = "name")
    GroupDTO groupToGroupDTO(Group group);

    @Named("groupToGroupDTOFull")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(target = "name", source = "name")
    GroupDTO groupToGroupDTOFull(Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project.id", ignore = true)
    @Mapping(target = "name", source = "name")
    Group groupDTOToGroup(GroupDTO groupDto);

    @IterableMapping(qualifiedByName = "groupToGroupDTOFull")
    List<GroupDTO> groupToGroupDTOs(Collection<Group> groups);
}
