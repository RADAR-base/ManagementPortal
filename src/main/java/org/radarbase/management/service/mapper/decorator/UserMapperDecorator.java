package org.radarbase.management.service.mapper.decorator;

import org.radarbase.management.domain.User;
import org.radarbase.management.domain.audit.EntityAuditInfo;
import org.radarbase.management.service.RevisionService;
import org.radarbase.management.service.dto.UserDTO;
import org.radarbase.management.service.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapperDecorator implements UserMapper {

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

    @Override
    public List<UserDTO> usersToUserDTOs(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream().map(this::userToUserDTO).collect(Collectors.toList());
    }

    @Override
    public User userDTOToUser(UserDTO userDto) {
        if (userDto == null) {
            return null;
        }

        return delegate.userDTOToUser(userDto);
    }

    @Override
    public List<User> userDTOsToUsers(List<UserDTO> userDtos) {
        if (userDtos == null) {
            return null;
        }

        return userDtos.stream().map(this::userDTOToUser).collect(Collectors.toList());
    }
}
