package org.radarcns.management.service.mapper;

import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.service.dto.UserDTO;
import org.mapstruct.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = "spring", uses = { ProjectMapper.class , RoleMapper.class})
public interface UserMapper {

    UserDTO userToUserDTO(User user);

    List<UserDTO> usersToUserDTOs(List<User> users);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User userDTOToUser(UserDTO userDTO);

    default Set<String> rolesToAuthorities(Set<Role> roles) {
        if(roles == null) {
            return null;
        }
        return roles.stream()
            .filter(Objects::nonNull)
            .map(role -> role.getAuthority().getName()).collect(Collectors.toSet());
    }

    List<User> userDTOsToUsers(List<UserDTO> userDTOs);

    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    default User userFromLogin(String login) {
        if (login == null) {
            return null;
        }
        User user = new User();
        user.setLogin(login);
        return user;
    }

    default Set<String> stringsFromAuthorities (Set<Authority> authorities) {
        if (authorities == null) {
            return null;
        }
        return authorities.stream().filter(Objects::nonNull).map(Authority::getName)
            .collect(Collectors.toSet());
    }

    default Set<Authority> authoritiesFromStrings(Set<String> strings) {
        if (strings == null) {
            return null;
        }
        return strings.stream().map(string -> {
            Authority auth = new Authority();
            auth.setName(string);
            return auth;
        }).collect(Collectors.toSet());
    }
}
