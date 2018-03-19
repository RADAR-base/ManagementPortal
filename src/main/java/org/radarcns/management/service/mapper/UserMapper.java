package org.radarcns.management.service.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.service.dto.UserDTO;

/**
 * Mapper for the entity User and its DTO UserDTO.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class, RoleMapper.class})
public interface UserMapper {

    UserDTO userToUserDTO(User user);

    List<UserDTO> usersToUserDTOs(List<User> users);

    @Mapping(target = "activationKey", ignore = true)
    @Mapping(target = "resetKey", ignore = true)
    @Mapping(target = "resetDate", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User userDTOToUser(UserDTO userDto);

    /**
     * Map a set of {@link Role}s to a set of strings that are the authorities.
     * @param roles the roles to map
     * @return the authorities as a set of strings if roles is not null, null otherwise
     */
    default Set<String> rolesToAuthorities(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(role -> role.getAuthority().getName())
                .collect(Collectors.toSet());
    }

    List<User> userDTOsToUsers(List<UserDTO> userDtos);

    /**
     * Create a {@link User} object with it's id field set to the given id.
     * @param id the id
     * @return the user if id is not null, null otherwise.
     */
    default User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    /**
     * Create a {@link User} object with the login set to the given login.
     * @param login the login
     * @return the user login is not null, null otherwise
     */
    default User userFromLogin(String login) {
        if (login == null) {
            return null;
        }
        User user = new User();
        user.setLogin(login);
        return user;
    }

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

    /**
     * Map a set of strings to a set of authorities.
     * @param strings the strings
     * @return the set of authorities of the set of strings is not null, null otherwise
     */
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
