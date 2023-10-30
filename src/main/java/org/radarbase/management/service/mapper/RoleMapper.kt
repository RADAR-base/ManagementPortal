package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.domain.Role
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.mapper.decorator.RoleMapperDecorator

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = [ProjectMapper::class])
@DecoratedWith(RoleMapperDecorator::class)
interface RoleMapper {
    @Mapping(source = "authority.name", target = "authorityName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.projectName", target = "projectName")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "organization.name", target = "organizationName")
    fun roleToRoleDTO(role: Role): RoleDTO

    @Mapping(target = "authority", ignore = true)
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(target = "users", ignore = true)
    @Mapping(source = "organizationId", target = "organization.id")
    fun roleDTOToRole(roleDto: RoleDTO?): Role?
    fun roleDTOsToRoles(roleDtos: Set<RoleDTO>): Set<Role>
    fun rolesToRoleDTOs(roles: Set<Role>): Set<RoleDTO>
}
