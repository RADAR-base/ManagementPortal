package org.radarbase.management.service.mapper.decorator;

import org.radarbase.management.domain.User;
import org.radarbase.management.domain.audit.EntityAuditInfo;
import org.radarbase.management.service.RevisionService;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class UserMapperDecorator implements UserMapper {

    @Autowired
    @Qualifier("delegate")
    private UserMapper delegate;

    @Autowired
    private RevisionService revisionService;

    @Override
    public UserDTO userToUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = delegate.userToUserDTO(user);

        EntityAuditInfo auditInfo = revisionService.getAuditInfo(user);
        dto.setCreatedDate(auditInfo.getCreatedAt());
        dto.setCreatedBy(auditInfo.getCreatedBy());
        dto.setLastModifiedDate(auditInfo.getLastModifiedAt());
        dto.setLastModifiedBy(auditInfo.getLastModifiedBy());

        return dto;
    }
}
