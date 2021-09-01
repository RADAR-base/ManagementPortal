package org.radarbase.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.domain.Source;
import org.radarbase.management.repository.SourceRepository;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.SourceService;
import org.radarbase.management.service.dto.MinimalProjectDetailsDTO;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.jhipster.web.util.ResponseUtil.wrapOrNotFound;
import static org.radarbase.auth.authorization.AuthoritiesConstants.SYS_ADMIN;
import static org.radarbase.auth.authorization.Permission.SOURCE_CREATE;
import static org.radarbase.auth.authorization.Permission.SOURCE_DELETE;
import static org.radarbase.auth.authorization.Permission.SOURCE_READ;
import static org.radarbase.auth.authorization.Permission.SOURCE_UPDATE;
import static org.radarbase.auth.authorization.Permission.SUBJECT_READ;
import static org.radarbase.auth.authorization.RadarAuthorization.checkAuthorityAndPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnSource;
import static org.radarbase.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Source.
 */
@RestController
@RequestMapping("/api")
public class SourceResource {

    private static final Logger log = LoggerFactory.getLogger(SourceResource.class);

    private static final String ENTITY_NAME = "source";

    @Autowired
    private SourceService sourceService;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /sources : Create a new source.
     *
     * @param sourceDto the sourceDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new sourceDto, or with
     *     status 400 (Bad Request) if the source has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> createSource(@Valid @RequestBody SourceDTO sourceDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Source : {}", sourceDto);
        MinimalProjectDetailsDTO project = sourceDto.getProject();
        String projectName = project != null ? project.getProjectName() : null;
        checkPermissionOnProject(getJWT(servletRequest), SOURCE_CREATE, projectName);
        if (sourceDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "A new source cannot already have an ID")).build();
        } else if (sourceDto.getSourceId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceIdExists", "A new source cannot already have a Source ID")).build();
        } else if (sourceRepository.findOneBySourceName(sourceDto.getSourceName()).isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceNameExists", "Source name already in use")).build();
        } else if (sourceDto.getAssigned() == null) {
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "sourceAssignedRequired",
                            "A new source must have the 'assigned' field specified")).body(null);
        } else {
            SourceDTO result = sourceService.save(sourceDto);
            return ResponseEntity.created(ResourceUriService.getUri(result))
                    .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME,
                            result.getSourceName()))
                    .body(result);
        }
    }

    /**
     * PUT  /sources : Updates an existing source.
     *
     * @param sourceDto the sourceDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated sourceDto, or with
     *     status 400 (Bad Request) if the sourceDto is not valid, or with status 500 (Internal
     *     Server Error) if the sourceDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sources")
    @Timed
    public ResponseEntity<SourceDTO> updateSource(@Valid @RequestBody SourceDTO sourceDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Source : {}", sourceDto);
        if (sourceDto.getId() == null) {
            return createSource(sourceDto);
        }
        RadarToken jwt = getJWT(servletRequest);
        MinimalProjectDetailsDTO project = sourceDto.getProject();
        String projectName = project != null ? project.getProjectName() : null;
        checkPermissionOnProject(jwt, SOURCE_UPDATE, projectName);
        Optional<SourceDTO> updatedSource = sourceService.updateSource(sourceDto, jwt);
        return wrapOrNotFound(updatedSource,
                HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, sourceDto.getSourceName()));
    }

    /**
     * GET  /sources : get all the sources.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/sources")
    @Timed
    public ResponseEntity<List<SourceDTO>> getAllSources(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable)
            throws NotAuthorizedException {
        checkAuthorityAndPermission(getJWT(servletRequest), SYS_ADMIN, SUBJECT_READ);
        log.debug("REST request to get all Sources");
        Page<SourceDTO> page = sourceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/sources");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /sources/:sourceName : get the source with this sourceName.
     *
     * @param sourceName the name of the sourceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the sourceDTO, or with status
     *     404 (Not Found)
     */
    @GetMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<SourceDTO> getSource(@PathVariable String sourceName)
            throws NotAuthorizedException {
        log.debug("REST request to get Source : {}", sourceName);
        RadarToken jwt = getJWT(servletRequest);
        checkPermission(jwt, SOURCE_READ);
        Optional<SourceDTO> sourceOpt = sourceService.findOneByName(sourceName);
        if (sourceOpt.isPresent()) {
            SourceDTO source = sourceOpt.get();
            String projectName = source.getProject() != null
                    ? source.getProject().getProjectName()
                    : null;
            checkPermissionOnSource(jwt, SOURCE_READ, projectName, source.getSubjectLogin(),
                    source.getSourceName());
        }
        return wrapOrNotFound(sourceService.findOneByName(sourceName));
    }

    /**
     * DELETE  /sources/:sourceName : delete the "id" source.
     *
     * @param sourceName the id of the sourceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sources/{sourceName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteSource(@PathVariable String sourceName)
            throws NotAuthorizedException {
        log.debug("REST request to delete Source : {}", sourceName);
        RadarToken jwt = getJWT(servletRequest);
        checkPermission(jwt, SOURCE_DELETE);
        Optional<SourceDTO> sourceDtoOpt = sourceService.findOneByName(sourceName);
        if (sourceDtoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SourceDTO sourceDto = sourceDtoOpt.get();
        String projectName = sourceDto.getProject() != null
                ? sourceDto.getProject().getProjectName()
                : null;
        checkPermissionOnSource(jwt, SOURCE_DELETE, projectName, sourceDto.getSubjectLogin(),
                sourceDto.getSourceName());

        if (sourceDto.getAssigned()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceIsAssigned", "Cannot delete an assigned source")).build();
        }
        Long sourceId = sourceDtoOpt.get().getId();
        Revisions<Integer, Source> sourceHistory = sourceRepository.findRevisions(sourceId);
        List<Source> sources = sourceHistory.getContent().stream().map(Revision::getEntity)
                .filter(Source::isAssigned).collect(Collectors.toList());
        if (!sources.isEmpty()) {
            HttpHeaders failureAlert = HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "sourceRevisionIsAssigned", "Cannot delete a previously assigned source");
            return ResponseEntity.status(HttpStatus.CONFLICT).headers(failureAlert).build();
        }
        sourceService.delete(sourceId);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME,
                sourceName)).build();
    }

}
