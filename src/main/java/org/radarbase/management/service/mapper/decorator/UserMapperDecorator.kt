package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.domain.User
import org.radarbase.management.service.RevisionService
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.UserMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

abstract class UserMapperDecorator : UserMapper {
    @Autowired
    @Qualifier("delegate")
    private val delegate: UserMapper? = null

    @Autowired
    private val revisionService: RevisionService? = null
    override fun userToUserDTO(user: User?): UserDTO? {
        if (user == null) {
            return null
        }
        val dto = delegate!!.userToUserDTO(user)
        val auditInfo = revisionService!!.getAuditInfo(user)
        dto?.createdDate = auditInfo.createdAt
        dto?.createdBy = auditInfo.createdBy
        dto?.lastModifiedDate = auditInfo.lastModifiedAt
        dto?.lastModifiedBy = auditInfo.lastModifiedBy
        return dto
    }
}
