package org.radarcns.management.service;


import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;
import static org.radarcns.management.web.rest.errors.EntityName.SOURCE;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.ProjectRepository;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.radarcns.management.service.mapper.SourceTypeMapper;
import org.radarcns.management.web.rest.errors.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SourceTypeMapper sourceTypeMapper;

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
        return sourceRepository
                .findAll()
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
        return sourceRepository
                .findAll(pageable)
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
     * Get one source by id.
     *
     * @param id the id of the source
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<SourceDTO> findOneById(Long id) {
        log.debug("Request to get Source by id: {}", id);
        return Optional.ofNullable(sourceRepository.findOne(id))
                .map(sourceMapper::sourceToSourceDTO);
    }

    /**
     * Delete the  device by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    public void delete(Long id) {
        log.info("Request to delete Source : {}", id);
        Revisions<Integer, Source> sourceHistory = sourceRepository.findRevisions(id);
        List<Source> sources = sourceHistory.getContent().stream().map(Revision::getEntity)
                .filter(Source::isAssigned).collect(Collectors.toList());
        if (sources.isEmpty()) {
            sourceRepository.delete(id);
        } else {
            Map<String, String> errorParams = new HashMap<>();
            errorParams.put("message", "Cannot delete source with sourceId ");
            errorParams.put("id", Long.toString(id));
            throw new InvalidRequestException("Cannot delete a source that was once assigned.",
                    SOURCE, "error.usedSourceDeletion", errorParams);
        }
    }

    /**
     * Returns all sources by project in {@link SourceDTO} format.
     *
     * @return list of sources
     */
    public Page<SourceDTO> findAllByProjectId(Long projectId, Pageable pageable) {
        return sourceRepository.findAllSourcesByProjectId(pageable, projectId)
                .map(sourceMapper::sourceToSourceWithoutProjectDTO);
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
        return sourceRepository
                .findAllSourcesByProjectIdAndAssigned(projectId, assigned)
                .stream()
                .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * This method does a safe update of source assigned to a subject. It will allow updates of
     * attributes only.
     *
     * @param sourceToUpdate source fetched from database
     * @param attributes     value to update
     * @return Updated {@link MinimalSourceDetailsDTO} of source
     */
    public MinimalSourceDetailsDTO safeUpdateOfAttributes(Source sourceToUpdate,
            Map<String, String> attributes) {

        // update source attributes
        Map<String, String> updatedAttributes = new HashMap<>();
        updatedAttributes.putAll(sourceToUpdate.getAttributes());
        updatedAttributes.putAll(attributes);

        sourceToUpdate.setAttributes(updatedAttributes);
        // rest of the properties should not be updated from this request.
        return sourceMapper.sourceToMinimalSourceDetailsDTO(sourceRepository.save(sourceToUpdate));
    }

    /**
     * Updates a source.
     * Does not allow to transfer a source, if it is currently assigned.
     * Does not allow to transfer if new project does not have valid source-type.
     *
     * @param sourceDto source details to update.
     * @return updated source.
     */
    @Transactional
    public SourceDTO updateSource(SourceDTO sourceDto) {
        Source existingSource = sourceRepository.findOne(sourceDto.getId());
        // if the source is being transferred to another project.
        if (!existingSource.getProject().getId().equals(sourceDto.getProject().getId())) {
            if (existingSource.isAssigned()) {
                throw new InvalidRequestException("Cannot transfer an assigned source", SOURCE,
                        "error.sourceIsAssigned");
            }
            // check whether source-type of the device is assigned to the new project
            // to be transferred.
            Optional<SourceType> sourceType = projectRepository
                    .findSourceTypeByProjectIdAndSourceTypeId(sourceDto.getProject().getId(),
                            existingSource.getSourceType().getId());

            if (!sourceType.isPresent()) {
                throw new InvalidRequestException(
                        "Cannot transfer a source to a project which doesn't have compatible "
                                + "source-type", ENTITY_NAME, "error.invalidTransfer");
            }
            // set old source-type, ensures compatibility
            sourceDto.setSourceType(
                    sourceTypeMapper.sourceTypeToSourceTypeDTO(existingSource.getSourceType()));

        }

        return save(sourceDto);

    }
}
