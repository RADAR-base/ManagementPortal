package org.radarcns.management.service.mapper;


import java.util.Set;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.Role;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.mapper.decorator.RoleMapperDecorator;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class})
@DecoratedWith(RoleMapperDecorator.class)
public interface RoleMapper {

    @Mapping(source = "authority.name", target = "authorityName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.projectName", target = "projectName")
    RoleDTO roleToRoleDTO(Role role);

    @Mapping(target = "authority", ignore = true)
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(target = "users", ignore = true)
    Role roleDTOToRole(RoleDTO roleDtp);

    Set<Role> roleDTOsToRoles(Set<RoleDTO> roleDtos);

    Set<RoleDTO> rolesToRoleDTOs(Set<Role> roles);

}
