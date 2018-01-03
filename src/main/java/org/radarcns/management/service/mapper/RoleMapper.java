package org.radarcns.management.service.mapper;


import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Role;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.service.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by nivethika on 23-5-17.
 */
@Mapper(componentModel = "spring", uses = {ProjectMapper.class})
public abstract class RoleMapper {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Mapping(source = "authority.name", target = "authorityName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.projectName", target = "projectName")
    public abstract RoleDTO roleToRoleDTO(Role role);

    @Mapping(source = "authorityName", target = "authority")
    @Mapping(source = "projectId", target = "project.id")
    @Mapping(target = "users", ignore = true)
    public abstract Role roleDTOToRole(RoleDTO roleDTO);

    public Authority authorityFromAuthorityName(String authorityName) {
        return authorityRepository.findByAuthorityName(authorityName);
    }

    public abstract Set<Role> roleDTOsToRoles(Set<RoleDTO> roleDTOs);

    public abstract Set<RoleDTO> rolesToRoleDTOs(Set<Role> roles);


}
