package org.radarcns.management.service;

import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.User;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.security.AuthoritiesConstants;
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

    @Autowired
    private UserService userService;

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
        return sourceRepository.findAll()
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<Source> findAllUnassignedSources() {


        List<Source> sources = new LinkedList<>();
//        List result = new LinkedList();
        User currentUser = userService.getUserWithAuthorities();
        List<String> currentUserAuthorities = currentUser.getAuthorities().stream().map(Authority::getName).collect(
            Collectors.toList());
        if(currentUserAuthorities.contains(AuthoritiesConstants.SYS_ADMIN)) {
            log.debug("Request to get all unassigned sources");
            sources = sourceRepository.findAllSourcesByAssigned(false);/*.stream()
                .map(projectMapper::projectToProjectDTO)
                .collect(Collectors.toCollection(LinkedList::new));*/
        }
        else if(currentUserAuthorities.contains(AuthoritiesConstants.PROJECT_ADMIN)) {
            log.debug("Request to get Sources of admin's project ");
//            sources = sourceRepository.findAllSourcesByProjectIdAndAssigned(currentUser.getProject().getId(), false);
        }
        return sources;
    }
    public List<MinimalSourceDetailsDTO> findAllUnassignedSourcesMinimalDTO() {

        return findAllUnassignedSources().stream()
            .map(sourceMapper::sourceToMinimalSourceDetailsDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public List<MinimalSourceDetailsDTO> findAllUnassignedSourcesAndOfSubject(Long id) {
        log.debug("Request to get all unassigned sources and assigned sources of a subject");
        List<Source> subjectSources = subjectRepository.findSourcesBySubjectId(id);
        List<Source> sources = findAllUnassignedSources();
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

    public List<SourceDTO> findAllByProjectId(Long projectId) {
        return sourceRepository.findAllSourcesByProjectId(projectId)
            .stream()
            .map(sourceMapper::sourceToSourceDTO)
            .collect(Collectors.toCollection(LinkedList::new));
    }
}
