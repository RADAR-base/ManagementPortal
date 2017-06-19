package org.radarcns.management.service;

import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
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

    @Autowired
    private SubjectRepository subjectRepository;

//    public SourceService(SourceRepository sourceRepository, SourceMapper sourceMapper) {
//        this.sourceRepository = sourceRepository;
//        this.sourceMapper = sourceMapper;
//    }

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
        SourceDTO result = sourceMapper.sourceToSourceDTO(source);
        return result;
    }

    /**
     *  Get all the Sources.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SourceDTO> findAll() {
        log.debug("Request to get all Sources");
        List<SourceDTO> result = sourceRepository.findAll().stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));

        return result;
    }

    public List<MinimalSourceDetailsDTO> findAllUnassignedSources() {
        log.debug("Request to get all unassigned sources");
        List<MinimalSourceDetailsDTO> result = sourceRepository.findAllSourcesByAssigned(false).stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
        return result;
    }

    public List<MinimalSourceDetailsDTO> findAllUnassignedSourcesAndOfSubject(Long id) {
        log.debug("Request to get all unassigned sources and assigned sources of a subject");
        List<Source> subjectSources = subjectRepository.findSourcesBySubjectId(id);
        List<Source> sources = sourceRepository.findAllSourcesByAssigned(false);
        sources.addAll(subjectSources);
        List<MinimalSourceDetailsDTO> result = sources.stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
        return result;
    }
    /**
     *  Get one device by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public SourceDTO findOne(Long id) {
        log.debug("Request to get Source : {}", id);
        Source source = sourceRepository.findOne(id);
        SourceDTO sourceDTO = sourceMapper.sourceToSourceDTO(source);
        return sourceDTO;
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
}
