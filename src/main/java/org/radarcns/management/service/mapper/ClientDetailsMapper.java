package org.radarcns.management.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 7/09/2017.
 */
@Mapper(componentModel = "spring", uses = {BaseClientDetails.class})
public interface ClientDetailsMapper {

    @Mapping(target = "clientSecret", ignore = true)
    ClientDetailsDTO clientDetailsToClientDetailsDTO(ClientDetails details);
    BaseClientDetails clientDetailsDTOToClientDetails(ClientDetailsDTO detailsDTO);
    List<ClientDetailsDTO> clientDetailsToClientDetailsDTO(List<ClientDetails> detailsList);
    List<ClientDetails> clientDetailsDTOToClientDetails(List<ClientDetailsDTO> detailsDTOList);

    default Collection<GrantedAuthority> map(Set<String> authorities) {
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    default Set<String> map(Collection<GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    default Map<String, String> map(Map<String, ?> additionalInformation) {
        return additionalInformation.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
    }
}
