package org.radarcns.management.service.mapper;

import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class to map ClientDetails to ClientDetailsDTO and vice versa. We don't use an
 * autogenerated mapper like for the other DTO's here, as we don't want to include the client
 * secret from ClientDetails into the DTO, but we do want to include it the other way around.
 */
@Component
public class ClientDetailsMapperImpl implements ClientDetailsMapper {
    @Override
    public ClientDetailsDTO clientDetailsToClientDetailsDTO(ClientDetails details) {
        ClientDetailsDTO result = new ClientDetailsDTO();
        result.setClientId(details.getClientId());
        result.setClientSecret(details.getClientSecret());
        result.setScope(details.getScope());
        result.setResourceIds(details.getResourceIds());
        result.setAuthorizedGrantTypes(details.getAuthorizedGrantTypes());
        if (Objects.nonNull(details.getScope())) {
            result.setAutoApproveScopes(details.getScope().stream()
                    .filter(details::isAutoApprove)
                    .collect(Collectors.toSet())
            );
        }
        if (Objects.nonNull(details.getAccessTokenValiditySeconds())) {
            result.setAccessTokenValidity(details.getAccessTokenValiditySeconds().longValue());
        }
        if (Objects.nonNull(details.getRefreshTokenValiditySeconds())) {
            result.setRefreshTokenValidity(details.getRefreshTokenValiditySeconds().longValue());
        }
        if (Objects.nonNull(details.getAuthorities())) {
            Set<String> authorities = details.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            result.setAuthorities(authorities);
        }
        if (Objects.nonNull(details.getAdditionalInformation())) {
            result.setAdditionalInformation(details.getAdditionalInformation().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> Objects.nonNull(e.getValue())
                            ? e.getValue().toString() : null)));
        }
        return result;
    }

    @Override
    public ClientDetails clientDetailsDTOToClientDetails(ClientDetailsDTO detailsDTO) {
        BaseClientDetails result = new BaseClientDetails();
        result.setClientId(detailsDTO.getClientId());
        result.setClientSecret(detailsDTO.getClientSecret());
        result.setScope(detailsDTO.getScope());
        result.setResourceIds(detailsDTO.getResourceIds());
        result.setAuthorizedGrantTypes(detailsDTO.getAuthorizedGrantTypes());
        result.setAutoApproveScopes(detailsDTO.getAutoApproveScopes());
        if (Objects.nonNull(detailsDTO.getAccessTokenValidity())) {
            result.setAccessTokenValiditySeconds(detailsDTO.getAccessTokenValidity().intValue());
        }
        if (Objects.nonNull(detailsDTO.getRefreshTokenValidity())) {
            result.setRefreshTokenValiditySeconds(detailsDTO.getRefreshTokenValidity().intValue());
        }
        if (Objects.nonNull(detailsDTO.getAuthorities())) {
            Set<GrantedAuthority> authorities = detailsDTO.getAuthorities().stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
            result.setAuthorities(authorities);
        }
        result.setAdditionalInformation(detailsDTO.getAdditionalInformation());
        return result;
    }

    @Override
    public List<ClientDetailsDTO> clientDetailsToClientDetailsDTO(List<ClientDetails> detailsList) {
        if (Objects.isNull(detailsList)) {
            return Collections.emptyList();
        }
        return detailsList.stream().map(this::clientDetailsToClientDetailsDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClientDetails> clientDetailsDTOToClientDetails(List<ClientDetailsDTO> detailsDTOList) {
        if (Objects.isNull(detailsDTOList)) {
            return Collections.emptyList();
        }
        return detailsDTOList.stream().map(this::clientDetailsDTOToClientDetails)
                .collect(Collectors.toList());
    }
}
