package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.decorator.UserMapperDecorator

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = [ProjectMapper::class, RoleMapper::class])
@DecoratedWith(
    UserMapperDecorator::class
)
interface UserMapper {
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    fun userToUserDTO(user: User?): UserDTO?

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    fun userToUserDTONoProvenance(user: User?): UserDTO?

    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    fun userDTOToUser(userDto: UserDTO?): User?
}
