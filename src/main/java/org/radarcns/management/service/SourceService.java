package org.radarcns.management.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.web.rest.errors.CustomConflictException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Source.
 */
@Service
@Transactional
public class SourceService {

    private final Logger log = LoggerFactory.getLogger(SourceService.class);

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SourceMapper sourceMapper;

    /**
     * Save a Source.
     *
     * @param sourceDto the entity to save
     * @return the persisted entity
     */
    public SourceDTO save(SourceDTO sourceDto) {
        log.debug("Request to save Source : {}", sourceDto);
        Source source = sourceMapper.sourceDTOToSource(sourceDto);
        source = sourceRepository.save(source);
        return sourceMapper.sourceToSourceDTO(source);
    }

    /**
     * Get all the Sources.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> findAll() {
        return sourceRepository.findAll()
                .stream()
                .map(sourceMapper::sourceToSourceDTO)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the sourceData with pagination.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<SourceDTO> findAll(Pageable pageable) {
        log.debug("Request to get SourceData with pagination");
        return sourceRepository.findAll(pageable)
                .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     * Get one source by name.
     *
     * @param sourceName the name of the source
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SourceDTO> findOneByName(String sourceName) {
        log.debug("Request to get Source : {}", sourceName);
        return sourceRepository.findOneBySourceName(sourceName)
                .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     * Delete the  device by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Source : {}", id);
        sourceRepository.delete(id);
    }

    /**
     * Returns all sources by project in {@link SourceDTO} format.
     *
     * @return list of sources
     */
    public Page<SourceDTO> findAllByProjectId(Long projectId, Pageable pageable) {
        return sourceRepository.findAllSourcesByProjectId(pageable, projectId)
                .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     * Returns all sources by project in {@link MinimalSourceDetailsDTO} format.
     *
     * @return list of sources
     */
    public Page<MinimalSourceDetailsDTO> findAllMinimalSourceDetailsByProject(Long projectId,
            Pageable pageable) {
        return sourceRepository.findAllSourcesByProjectId(pageable, projectId)
                .map(sourceMapper::sourceToMinimalSourceDetailsDTO);
    }

    /**
     * Returns list of not-assigned sources by project id.
     */
    public List<SourceDTO> findAllByProjectAndAssigned(Long projectId, boolean assigned) {
        return sourceMapper.sourcesToSourceDTOs(
                sourceRepository.findAllSourcesByProjectIdAndAssigned(projectId, assigned));
    }

    /**
     * Returns list of not-assigned sources by project id.
     */
    public List<MinimalSourceDetailsDTO> findAllMinimalSourceDetailsByProjectAndAssigned(
            Long projectId, boolean assigned) {
        return sourceRepository.findAllSourcesByProjectIdAndAssigned(projectId, assigned).stream()
                .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * This method does a safe update of source assigned to a subject.
     * @param sourceToUpdate source fetched from database
     * @param sourceDto value to update
     * @return Updated {@link MinimalSourceDetailsDTO} of source
     * @throws CustomConflictException when source is requested to replace with an existing
     *      source-name.
     */
    public MinimalSourceDetailsDTO safeUpdate(Source sourceToUpdate,
            MinimalSourceDetailsDTO sourceDto) throws URISyntaxException {
        // if a source-name update is expected, check whether any source available under the
        // newly requested source-name
        if (sourceDto.getSourceName() != null
                && !sourceToUpdate.getSourceName().equals(sourceDto.getSourceName())
                && sourceRepository.findOneBySourceName(sourceDto.getSourceName()).isPresent()) {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Source already exists with provided sourceName ");
            errorParams.put("sourceName", sourceDto.getSourceName());
            throw new CustomConflictException(ErrorConstants.ERR_SOURCE_NAME_EXISTS, errorParams,
                new URI(HeaderUtil.buildPath("api", "sources", sourceDto.getSourceName())));
        }

        // update source name
        sourceToUpdate.setSourceName(sourceDto.getSourceName());

        // update source attributes
        Map<String, String> mergedValues = Stream.of(sourceToUpdate.getAttributes(),
                sourceDto.getAttributes())
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        // such that if a value already exist
                        // for a given key, the value from the request is taken
                        (v1, v2) -> v2
                    ));

        sourceToUpdate.setAttributes(mergedValues);
        // update expected source-name
        sourceToUpdate.setExpectedSourceName(sourceToUpdate.getExpectedSourceName());
        // rest of the attributes should not be updated from this request.
        return sourceMapper.sourceToMinimalSourceDetailsDTO(sourceRepository.save(sourceToUpdate));
    }
}
