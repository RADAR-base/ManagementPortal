package org.radarbase.management.service.mapper.decorator;


import org.radarbase.management.service.dto.ClientDetailsDTO;
import org.radarbase.management.service.mapper.ClientDetailsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Decorator for ClientDetailsMapper. The ClientDetails interface does not expose a method to get
 * all auto-approve scopes, instead it only has a method to check if a given scope is auto-approve.
 * This decorator adds the subset of scopes for which isAutoApprove returns true to the DTO.
 */
public abstract class ClientDetailsMapperDecorator implements ClientDetailsMapper {

    @Autowired
    @Qualifier("delegate")
    private ClientDetailsMapper delegate;

    @Override
    public ClientDetailsDTO clientDetailsToClientDetailsDTO(ClientDetails details) {
        ClientDetailsDTO clientDetailsDto = delegate.clientDetailsToClientDetailsDTO(details);
        // collect the scopes that are auto-approve and set them in our DTO
        clientDetailsDto.setAutoApproveScopes(details.getScope().stream()
                .filter(details::isAutoApprove)
                .collect(Collectors.toSet()));
        return clientDetailsDto;
    }

    @Override
    public List<ClientDetailsDTO> clientDetailsToClientDetailsDTO(List<ClientDetails> details) {
        if (Objects.isNull(details)) {
            return null;
        }
        return details.stream()
                .map(this::clientDetailsToClientDetailsDTO)
                .toList();
    }
}
