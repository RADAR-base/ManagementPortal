package org.radarbase.management.service.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.radarbase.management.domain.Authority;
import org.radarbase.management.domain.User;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.decorator.UserMapperDecorator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ProjectMapper.class, RoleMapper.class})
@DecoratedWith(UserMapperDecorator.class)
public interface UserMapper {

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    UserDTO userToUserDTO(User user);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "accessToken", ignore = true)
    UserDTO userToUserDTONoProvenance(User user);

    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User userDTOToUser(UserDTO userDto);

    /**
     * Map a set of {@link Authority}s to a set of strings that are the authority names.
     * @param authorities the authorities to map
     * @return the set of strings if authorities is not null, null otherwise
     */
    default Set<String> stringsFromAuthorities(Set<Authority> authorities) {
        if (authorities == null) {
            return null;
        }
        return authorities.stream().map(Authority::getName)
                .collect(Collectors.toSet());
    }
}
