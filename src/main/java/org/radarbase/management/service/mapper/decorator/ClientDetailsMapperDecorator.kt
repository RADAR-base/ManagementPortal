package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.oauth2.provider.ClientDetails
import java.util.*
import java.util.stream.Collectors

/**
 * Decorator for ClientDetailsMapper. The ClientDetails interface does not expose a method to get
 * all auto-approve scopes, instead it only has a method to check if a given scope is auto-approve.
 * This decorator adds the subset of scopes for which isAutoApprove returns true to the DTO.
 */
abstract class ClientDetailsMapperDecorator : ClientDetailsMapper {
    @Autowired
    @Qualifier("delegate")
    private val delegate: ClientDetailsMapper? = null
    override fun clientDetailsToClientDetailsDTO(details: ClientDetails): ClientDetailsDTO {
        val clientDetailsDto = delegate!!.clientDetailsToClientDetailsDTO(details)
        // collect the scopes that are auto-approve and set them in our DTO
        clientDetailsDto.autoApproveScopes = details.scope.stream()
            .filter { scope: String? -> details.isAutoApprove(scope) }
            .collect(Collectors.toSet())
        return clientDetailsDto
    }

    override fun clientDetailsToClientDetailsDTO(detailsList: List<ClientDetails>): List<ClientDetailsDTO>? {
        return if (Objects.isNull(detailsList)) {
            null
        } else detailsList.stream()
            .map { details: ClientDetails -> this.clientDetailsToClientDetailsDTO(details) }
            .toList()
    }
}
