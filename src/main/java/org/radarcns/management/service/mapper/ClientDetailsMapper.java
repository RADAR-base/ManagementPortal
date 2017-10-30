package org.radarcns.management.service.mapper;

import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.List;

/**
 * Created by dverbeec on 7/09/2017.
 */
public interface ClientDetailsMapper {

    public ClientDetailsDTO clientDetailsToClientDetailsDTO(ClientDetails details);
    public ClientDetails clientDetailsDTOToClientDetails(ClientDetailsDTO detailsDTO);
    public List<ClientDetailsDTO> clientDetailsToClientDetailsDTO(List<ClientDetails> detailsList);
    public List<ClientDetails> clientDetailsDTOToClientDetails(List<ClientDetailsDTO> detailsDTOList);

}
