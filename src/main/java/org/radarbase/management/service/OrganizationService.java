package org.radarbase.management.service;

import org.radarbase.management.repository.OrganizationRepository;
import org.radarbase.management.service.dto.OrganizationDTO;
import org.radarbase.management.service.mapper.OrganizationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Organization.
 */
@Service
@Transactional
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMapper organizationMapper;

    /**
     * Save an organization.
     *
     * @param organizationDto the entity to save
     * @return the persisted entity
     */
    public OrganizationDTO save(OrganizationDTO organizationDto) {
        log.debug("Request to save Organization : {}", organizationDto);
        var org = organizationMapper.organizationDTOToOrganization(organizationDto);
        org = organizationRepository.save(org);
        return organizationMapper.organizationToOrganizationDTO(org);
    }

    /**
     * Get all the organizations.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<OrganizationDTO> findAll() {
        return organizationRepository.findAll().stream()
            .map(organizationMapper::organizationToOrganizationDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get one organization by name.
     *
     * @param name the name of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<OrganizationDTO> findByName(String name) {
        log.debug("Request to get Organization by name: {}", name);
        return organizationRepository.findOneByName(name)
                .map(organizationMapper::organizationToOrganizationDTO);
    }
}
