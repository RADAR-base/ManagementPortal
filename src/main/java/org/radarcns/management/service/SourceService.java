package org.radarcns.management.service;

import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Source.
 */
@Service
@Transactional
public class SourceService {

    private Logger log = LoggerFactory.getLogger(SourceService.class);

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    /**
     * Save a Source.
     *
     * @param sourceDTO the entity to save
     * @return the persisted entity
     */
    public SourceDTO save(SourceDTO sourceDTO) {
        log.debug("Request to save Source : {}", sourceDTO);
        Source source = sourceMapper.sourceDTOToSource(sourceDTO);
        source = sourceRepository.save(source);
        return sourceMapper.sourceToSourceDTO(source);
    }

    /**
     *  Get all the Sources.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> findAll() {
        return sourceRepository.findAll()
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get all the sourceData with pagination.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SourceDTO> findAll(Pageable pageable) {
        log.debug("Request to get SourceData with pagination");
        return  sourceRepository.findAll(pageable)
            .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     *  Get one source by name
     *
     *  @param sourceName the name of the source
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SourceDTO> findOneByName(String sourceName) {
        log.debug("Request to get Source : {}", sourceName);
        return sourceRepository.findOneBySourceName(sourceName)
            .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     *  Delete the  device by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Source : {}", id);
        sourceRepository.delete(id);
    }

    /**
     * Returns all sources by project in {@link SourceDTO} format
     * @param projectId
     * @return list of sources
     */
    public List<SourceDTO> findAllByProjectId(Long projectId) {
        return sourceRepository.findAllSourcesByProjectId(projectId)
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Returns all sources by project in {@link MinimalSourceDetailsDTO} format
     * @param projectId
     * @return list of sources
     */
    public List<MinimalSourceDetailsDTO> findAllMinimalSourceDetailsByProject(Long projectId) {
        return sourceRepository.findAllSourcesByProjectId(projectId)
            .stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Returns list of not-assigned sources by project id
     * @param projectId
     * @param assigned
     * @return
     */
    public List<SourceDTO> findAllByProjectAndAssigned(Long projectId, boolean assigned) {
        return sourceMapper.sourcesToSourceDTOs(
            sourceRepository.findAllSourcesByProjectIdAndAssigned(projectId , assigned));
    }

    /**
     * Returns list of not-assigned sources by project id
     * @param projectId
     * @param assigned
     * @return
     */
    public List<MinimalSourceDetailsDTO> findAllMinimalSourceDetailsByProjectAndAssigned(
            Long projectId, boolean assigned) {
        return sourceRepository.findAllSourcesByProjectIdAndAssigned(projectId , assigned).stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toList());
    }
}
