package org.radarbase.management.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.radarbase.management.domain.SourceData;
import org.radarbase.management.repository.SourceDataRepository;
import org.radarbase.management.service.dto.SourceDataDTO;
import org.radarbase.management.service.mapper.SourceDataMapper;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.radarbase.management.web.rest.errors.EntityName.SOURCE_DATA;

/**
 * Service Implementation for managing SourceData.
 */
@Service
@Transactional
public class SourceDataService {

    private static final Logger log = LoggerFactory.getLogger(SourceDataService.class);

    private final SourceDataRepository sourceDataRepository;

    private final SourceDataMapper sourceDataMapper;

    public SourceDataService(SourceDataRepository sourceDataRepository,
            SourceDataMapper sourceDataMapper) {
        this.sourceDataRepository = sourceDataRepository;
        this.sourceDataMapper = sourceDataMapper;
    }

    /**
     * Save a sourceData.
     *
     * @param sourceDataDto the entity to save
     * @return the persisted entity
     */
    public SourceDataDTO save(SourceDataDTO sourceDataDto) {
        log.debug("Request to save SourceData : {}", sourceDataDto);
        if (sourceDataDto.getSourceDataType() == null) {
            throw new BadRequestException(ErrorConstants.ERR_VALIDATION, SOURCE_DATA,
                    "Source Data must contain a type or a topic.");
        }
        SourceData sourceData = sourceDataMapper.sourceDataDTOToSourceData(sourceDataDto);
        sourceData = sourceDataRepository.save(sourceData);
        return sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
    }

    /**
     * Get all the sourceData.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDataDTO> findAll() {
        log.debug("Request to get all SourceData");

        return sourceDataRepository.findAll().stream()
                .map(sourceDataMapper::sourceDataToSourceDataDTO)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the sourceData with pagination.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SourceDataDTO> findAll(Pageable pageable) {
        log.debug("Request to get all SourceData");
        return sourceDataRepository.findAll(pageable)
                .map(sourceDataMapper::sourceDataToSourceDataDTO);
    }

    /**
     * Get one sourceData by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public SourceDataDTO findOne(Long id) {
        log.debug("Request to get SourceData : {}", id);
        SourceData sourceData = sourceDataRepository.findById(id).get();
        return sourceDataMapper.sourceDataToSourceDataDTO(sourceData);
    }

    /**
     * Get one sourceData by name.
     *
     * @param sourceDataName the sourceDataType of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SourceDataDTO> findOneBySourceDataName(String sourceDataName) {
        log.debug("Request to get SourceData : {}", sourceDataName);
        return sourceDataRepository.findOneBySourceDataName(sourceDataName)
                .map(sourceDataMapper::sourceDataToSourceDataDTO);
    }

    /**
     * Delete the  sourceData by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete SourceData : {}", id);
        sourceDataRepository.deleteById(id);
    }
}
