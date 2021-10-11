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
import org.radarbase.management.domain.Group;
import org.radarbase.management.service.dto.GroupDTO;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    GroupDTO groupToGroupDTO(Group group);

    @Mapping(source = "project.id", target = "projectId")
    GroupDTO groupToGroupDTOFull(Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project.id", ignore = true)
    Group groupDTOToGroup(GroupDTO groupDTO);

    @IterableMapping(qualifiedByName = "groupToGroupDTO")
    List<GroupDTO> groupToGroupDTOs(Collection<Group> groups);
}
