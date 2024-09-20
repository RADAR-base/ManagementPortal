package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.mapper.decorator.ClientDetailsMapperDecorator
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.client.BaseClientDetails

/**
 * Created by dverbeec on 7/09/2017.
 */
@Mapper(componentModel = "spring", uses = [BaseClientDetails::class])
@DecoratedWith(ClientDetailsMapperDecorator::class)
interface ClientDetailsMapper {
    @Mapping(target = "clientSecret", ignore = true)
    @Mapping(target = "autoApproveScopes", ignore = true)
    fun clientDetailsToClientDetailsDTO(details: ClientDetails): ClientDetailsDTO

    fun clientDetailsToClientDetailsDTO(detailsList: List<ClientDetails>): List<ClientDetailsDTO>

    fun clientDetailsDTOToClientDetails(detailsDto: ClientDetailsDTO): BaseClientDetails

    fun clientDetailsDTOToClientDetails(detailsDtoList: List<ClientDetailsDTO>): List<ClientDetails>

    /**
     * Map a set of authorities represented as strings to a collection of {@link GrantedAuthority}s.
     *
     * @param authorities the set of authorities to be mapped
     * @return a collection of {@link GrantedAuthority}s
     */
    fun map(authorities: Set<String>): Collection<GrantedAuthority> = authorities.map { SimpleGrantedAuthority(it) }

    /**
     * Map a collection of authorities represented as [GrantedAuthority]s to a set of +strings.
     *
     * @param authorities the collection of [GrantedAuthority]s to be mapped
     * @return the set of strings
     */
    fun map(authorities: Collection<GrantedAuthority>): Set<String> = authorities.map { it.authority }.toSet()

    /**
     * Transforms the values in the input map to strings so the result is a
     * [Map].
     *
     * @param additionalInformation a [Map] to be transformed
     * @return a new map with the same keys as the input map, but the values are strings
     */
    fun map(additionalInformation: Map<String, Any>): Map<String, String> =
        additionalInformation.map { (key, value) -> key to value.toString() }.toMap()
}
