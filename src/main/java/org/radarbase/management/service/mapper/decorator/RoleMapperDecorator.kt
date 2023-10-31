package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.domain.Role
import org.radarbase.management.repository.AuthorityRepository
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.mapper.RoleMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by nivethika on 03-8-18.
 */
abstract class RoleMapperDecorator() : RoleMapper {

//    constructor(roleMapper: RoleMapper, authorityRepository: AuthorityRepository?) : this(roleMapper)
    @Autowired @Qualifier("delegate") private val delegate: RoleMapper? = null
    private var authorityRepository: AuthorityRepository? = null;

    /**
     * Overrides standard RoleMapperImpl and loads authority from repository if not specified.
     * @param roleDto to convert to Role.
     * @return converted Role instance.
     */
    override fun roleDTOToRole(roleDto: RoleDTO?): Role? {
        if (roleDto == null) {
            return null
        }
        val role = delegate?.roleDTOToRole(roleDto)
        if (role!!.authority == null) {
            role.authority = roleDto.authorityName?.let { authorityRepository?.getById(it) }
        }
        return role
    }
}
