package org.radarcns.management.service.mapper;


import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.service.dto.RoleDTO;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class, UserMapper.class})
public interface RoleMapper {

    @Mapping(source = "authority.name" , target = "authorityName")
    RoleDTO roleToRoleDTO (Role role);

    @Mapping(source = "authorityName" , target = "authority.name")
    Role roleDTOToRole(RoleDTO roleDTO);

    Set<Role> roleDTOsToRoles(Set<RoleDTO> roleDTOs);

    Set<RoleDTO> rolesToRoleDTOs(Set<Role> roles);

    String roleToAuthorityString(Role role);

    default Set<String> authorityStringsFromRoles (Set<Role> roles) {
        return roles.stream().map(r -> r.getAuthority().getName())
            .collect(Collectors.toSet());
    }

    default Role roleFromAuthorityName (String authorityName) {
        Role role = new Role();
        Authority authority = new Authority();
        authority.setName(authorityName);
        role.setAuthority(authority);
        return role;
    }

    default Set<Role> rolesFromAuthorityStrings(Set<String> authorities) {
        return authorities.stream().map(this::roleFromAuthorityName)
            .collect(Collectors.toSet());
    }



}
