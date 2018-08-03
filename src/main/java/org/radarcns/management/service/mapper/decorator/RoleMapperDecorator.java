package org.radarcns.management.service.mapper.decorator;

import org.radarcns.management.domain.Role;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nivethika on 03-8-18.
 */
public abstract class RoleMapperDecorator implements RoleMapper {

    @Autowired
    @Qualifier("delegate")
    private RoleMapper delegate;

    @Autowired
    private AuthorityRepository authorityRepository;

    /**
     * Overrides standard RoleMapperImpl and loads authority from repository if not specified.
     * @param roleDto to convert to Role.
     * @return converted Role instance.
     */
    @Override
    public Role roleDTOToRole(RoleDTO roleDto) {

        if (roleDto == null) {
            return null;
        }

        Role role = delegate.roleDTOToRole(roleDto);

        if (role.getAuthority() == null) {
            role.setAuthority(authorityRepository.getOne(roleDto.getAuthorityName()));
        }

        return role;
    }
}
