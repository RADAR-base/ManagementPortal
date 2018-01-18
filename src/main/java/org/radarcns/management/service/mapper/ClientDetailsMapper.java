package org.radarcns.management.service.mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.mapper.decorator.ClientDetailsMapperDecorator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

/**
 * Created by dverbeec on 7/09/2017.
 */
@Mapper(componentModel = "spring", uses = {BaseClientDetails.class})
@DecoratedWith(ClientDetailsMapperDecorator.class)
public interface ClientDetailsMapper {

    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "autoApproveScopes", ignore = true)
    ClientDetailsDTO clientDetailsToClientDetailsDTO(ClientDetails details);

    List<ClientDetailsDTO> clientDetailsToClientDetailsDTO(List<ClientDetails> detailsList);

    BaseClientDetails clientDetailsDTOToClientDetails(ClientDetailsDTO detailsDto);

    List<ClientDetails> clientDetailsDTOToClientDetails(List<ClientDetailsDTO> detailsDtoList);

    /**
     * Map a set of authorities represented as strings to a collection of {@link GrantedAuthority}s.
     * @param authorities the set of authorities to be mapped
     * @return a collection of {@link GrantedAuthority}s
     */
    default Collection<GrantedAuthority> map(Set<String> authorities) {
        if (Objects.isNull(authorities)) {
            return Collections.emptySet();
        }
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * Map a collection of authorities represented as {@link GrantedAuthority}s to a set of strings.
     * @param authorities the collection of {@link GrantedAuthority}s to be mapped
     * @return the set of strings
     */
    default Set<String> map(Collection<GrantedAuthority> authorities) {
        if (Objects.isNull(authorities)) {
            return Collections.emptySet();
        }
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    /**
     * Transforms the values in the input map to strings so the result is a
     * {@link Map}.
     * @param additionalInformation a {@link Map} to be transformed
     * @return a new map with the same keys as the input map, but the values are transformed to
     *     strings using their {@link Object#toString()} method
     */
    default Map<String, String> map(Map<String, ?> additionalInformation) {
        if (Objects.isNull(additionalInformation)) {
            return Collections.emptyMap();
        }
        return additionalInformation.entrySet().stream().collect(
                Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
    }
}
