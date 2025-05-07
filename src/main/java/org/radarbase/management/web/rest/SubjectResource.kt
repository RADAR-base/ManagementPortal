package org.radarbase.management.web.rest


import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.domain.Project
import org.radarbase.management.domain.Source
import org.radarbase.management.domain.Subject
import org.radarbase.management.domain.User
import org.radarbase.management.domain.enumeration.DataGroupingType
import org.radarbase.management.repository.ConnectDataLogRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.RoleRepository
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.security.SecurityUtils
import org.radarbase.management.service.*
import org.radarbase.management.service.dto.DataLogDTO
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.RevisionDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.criteria.SubjectAuthority
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.radarbase.management.web.rest.errors.*
import org.radarbase.management.web.rest.util.HeaderUtil
import org.radarbase.management.web.rest.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.jhipster.web.util.ResponseUtil
import java.io.Serializable
import java.net.URISyntaxException
import java.util.*
import java.util.stream.Stream
import javax.validation.Valid
import org.hibernate.Hibernate
/**
 * REST controller for managing Subject.
 */
@RestController
@RequestMapping("/api")
class SubjectResource(
    @Autowired private val subjectService: SubjectService,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val sourceTypeService: SourceTypeService,
    @Autowired private val eventRepository: AuditEventRepository,
    @Autowired private val revisionService: RevisionService,
    @Autowired private val sourceService: SourceService,
    @Autowired private val authService: AuthService,
    @Autowired private val connectDataLogRepository: ConnectDataLogRepository,
    @Autowired private val roleRepository: RoleRepository
) {

    /**
     * POST  /subjects : Create a new subject.
     *
     * @param subjectDto the subjectDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new subjectDto, or
     * with status 400 (Bad Request) if the subject has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/subjects")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createSubject(@RequestBody subjectDto: SubjectDTO): ResponseEntity<SubjectDTO> {
        log.debug("REST request to save Subject : {}", subjectDto)
        val projectName = getProjectName(subjectDto)
        authService.checkPermission(Permission.SUBJECT_CREATE, { e: EntityDetails -> e.project(projectName) })
        if (subjectDto.id != null) {
            throw BadRequestException(
                "A new subject cannot already have an ID",
                EntityName.SUBJECT, "idexists"
            )
        }
        if (!subjectDto.externalId.isNullOrEmpty()
            && subjectRepository.findOneByProjectNameAndExternalId(
                projectName, subjectDto.externalId
            ) != null
        ) {
            throw BadRequestException(
                "A subject with given project-id and"
                        + "external-id already exists", EntityName.SUBJECT, "subjectExists"
            )
        }
        val result = subjectService.createSubject(subjectDto)
        return ResponseEntity.created(ResourceUriService.getUri(subjectDto))
            .headers(HeaderUtil.createEntityCreationAlert(EntityName.SUBJECT, result?.login))
            .body(result)
    }


    private fun getProjectName(subjectDto: SubjectDTO): String {
        // not ideal, because only name is needed. however, id is checked to verify the project is in the database
        // this does prevent calls to the database?
        if (subjectDto.project == null || subjectDto.project!!.id == null || subjectDto.project!!.projectName == null) {
            throw BadRequestException(
                "A subject should be assigned to a project", EntityName.SUBJECT,
                "projectrequired"
            )
        }
        return subjectDto.project!!.projectName!!
    }

    /**
     * PUT  /subjects : Updates an existing subject.
     *
     * @param subjectDto the subjectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDto, or with
     * status 400 (Bad Request) if the subjectDto is not valid, or with status 500 (Internal
     * Server Error) if the subjectDto couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/subjects")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateSubject(@RequestBody subjectDto: SubjectDTO): ResponseEntity<SubjectDTO> {
        log.debug("REST request to update Subject : {}", subjectDto)
        if (subjectDto.id == null) {
            return createSubject(subjectDto)
        }
        val projectName = getProjectName(subjectDto)
        authService.checkPermission(Permission.SUBJECT_UPDATE, { e: EntityDetails ->
            e
                .project(projectName)
                .subject(subjectDto.login)
        })
        val result = subjectService.updateSubject(subjectDto)
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(EntityName.SUBJECT, subjectDto.login))
            .body(result)
    }

    /**
     * PUT  /subjects/discontinue : Discontinue a subject. A discontinued subject is not allowed to
     * send data to the system anymore.
     *
     * @param subjectDto the subjectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated subjectDto, or with
     * status 400 (Bad Request) if the subjectDto is not valid, or with status 500 (Internal
     * Server Error) if the subjectDto couldn't be updated
     */
    @PutMapping("/subjects/discontinue")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun discontinueSubject(@RequestBody subjectDto: SubjectDTO): ResponseEntity<SubjectDTO> {
        log.debug("REST request to update Subject : {}", subjectDto)
        if (subjectDto.id == null) {
            throw BadRequestException("No subject found", EntityName.SUBJECT, "subjectNotAvailable")
        }
        val projectName = getProjectName(subjectDto)
        authService.checkPermission(Permission.SUBJECT_UPDATE, { e: EntityDetails ->
            e
                .project(projectName)
                .subject(subjectDto.login)
        })

        // In principle this is already captured by the PostUpdate event listener, adding this
        // event just makes it more clear a subject was discontinued.
        eventRepository.add(
            AuditEvent(
                SecurityUtils.currentUserLogin,
                "SUBJECT_DISCONTINUE", "subject_login=" + subjectDto.login
            )
        )
        val result = subjectService.discontinueSubject(subjectDto)
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(EntityName.SUBJECT, subjectDto.login))
            .body(result)
    }

    /**
     * GET  /subjects : get all the subjects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of subjects in body
     */
    @GetMapping("/subjects")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getAllSubjects(
        @Valid subjectCriteria: SubjectCriteria?
    ): ResponseEntity<List<SubjectDTO?>>? {
        val projectName = subjectCriteria!!.projectName
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails -> e.project(projectName) })
        val externalId = subjectCriteria.externalId
        log.debug("ProjectName {} and external {}", projectName, externalId)
        // if not specified do not include inactive patients
        val authoritiesToInclude = subjectCriteria.authority
            .map { obj: SubjectAuthority -> obj.name }
            .toList()
        return if (projectName != null && externalId != null) {
            val subject = Optional.ofNullable(subjectRepository
                .findOneByProjectNameAndExternalIdAndAuthoritiesIn(
                    projectName, externalId, authoritiesToInclude
                )
                ?.let { s: Subject? ->
                    listOf(
                        subjectMapper.subjectToSubjectReducedProjectDTO(s)
                    )
                })
            ResponseUtil.wrapOrNotFound(subject)
        } else if (projectName == null && externalId != null) {
            val page = subjectService.findAll(subjectCriteria)
                .map { s: Subject -> subjectMapper.subjectToSubjectWithoutProjectDTO(s) }
            val headers = PaginationUtil.generateSubjectPaginationHttpHeaders(
                page, "/api/subjects", subjectCriteria
            )
            ResponseEntity(page.content, headers, HttpStatus.OK)
        } else {
            val page = subjectService.findAll(subjectCriteria)
                .map { subject: Subject -> subjectMapper.subjectToSubjectWithoutProjectDTO(subject) }
            val headers = PaginationUtil.generateSubjectPaginationHttpHeaders(
                page, "/api/subjects", subjectCriteria
            )
            ResponseEntity(page.content, headers, HttpStatus.OK)
        }
    }

    /**
     * GET  /subjects/:login : get the "login" subject.
     *
     * @param login the login of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSubject(@PathVariable login: String?): ResponseEntity<SubjectDTO> {
        log.debug("REST request to get Subject : {}", login)
        authService.checkScope(Permission.SUBJECT_READ)
        val subject = subjectService.findOneByLogin(login)
        val project: Project? = subject.activeProject
            ?.let { p -> p.id?.let { projectRepository.findOneWithEagerRelationships(it) } }
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            if (project != null) {
                e.project(project.projectName)
            }
            e.subject(subject.user?.login)
        })
        val subjectDto = subjectMapper.subjectToSubjectDTO(subject)
        return ResponseEntity.ok(subjectDto)
    }

    /**
     * GET  /subjects/:login/revisions : get all revisions for the "login" subject.
     *
     * @param login the login of the subjectDTO for which to retrieve the revisions
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/revisions")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSubjectRevisions(
        @Parameter pageable: Pageable?,
        @PathVariable login: String
    ): ResponseEntity<List<RevisionDTO>> {
        authService.checkScope(Permission.SUBJECT_READ)
        log.debug("REST request to get revisions for Subject : {}", login)
        val subject = subjectService.findOneByLogin(login)
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            e.project(subject.associatedProject?.projectName)
            e.subject(login)
        })
        val page = pageable?.let { revisionService.getRevisionsForEntity(it, subject) }
        return ResponseEntity.ok()
            .headers(
                PaginationUtil.generatePaginationHttpHeaders(
                    page,
                    HeaderUtil.buildPath("subjects", login, "revisions")
                )
            )
            .body(page?.content)
    }

    /**
     * GET  /subjects/:login/revisions/:revisionNb : get the "login" subject at revisionNb
     * 'revisionNb'.
     *
     * @param login the login of the subjectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the subjectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping(
        "/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}"
                + "/revisions/{revisionNb:^[0-9]*$}"
    )
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getSubjectRevision(
        @PathVariable login: String,
        @PathVariable revisionNb: Int?
    ): ResponseEntity<SubjectDTO> {
        authService.checkScope(Permission.SUBJECT_READ)
        log.debug("REST request to get Subject : {}, for revisionNb: {}", login, revisionNb)
        val subjectDto = subjectService.findRevision(login, revisionNb)
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            e.project(subjectDto.project?.projectName)
                .subject(subjectDto.login)
        })
        return ResponseEntity.ok(subjectDto)
    }

    /**
     * DELETE  /subjects/:login : delete the "login" subject.
     *
     * @param login the login of the subjectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteSubject(@PathVariable login: String?): ResponseEntity<Void> {
        log.debug("REST request to delete Subject : {}", login)
        authService.checkScope(Permission.SUBJECT_DELETE)
        val subject = subjectService.findOneByLogin(login)
        authService.checkPermission(Permission.SUBJECT_DELETE, { e: EntityDetails ->
            e.project(subject.associatedProject?.projectName)
            e.subject(login)
        })
        subjectService.deleteSubject(login)
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityDeletionAlert(EntityName.SUBJECT, login)).build()
    }

    /**
     * POST  /subjects/:login/sources: Assign a source to the specified user.
     *
     *
     * The request body is a [MinimalSourceDetailsDTO]. At minimum, the source should
     * define it's source type by either supplying the sourceTypeId, or the combination of
     * (sourceTypeProducer, sourceTypeModel, sourceTypeCatalogVersion) fields. A source ID will be
     * automatically generated. The source ID will be a new random UUID, and the source name, if not
     * provided, will be the device model, appended with a dash and the first eight characters of
     * the UUID. The sources will be created and assigned to the specified user.
     *
     *
     * If you need to assign existing sources, simply specify either of id, sourceId, or
     * sourceName fields.
     *
     * @param sourceDto The [MinimalSourceDetailsDTO] specification
     * @return The [MinimalSourceDetailsDTO] completed with all identifying fields.
     */
    @PostMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "An existing source was assigned"), ApiResponse(
            responseCode = "201", description = "A new source was created and"
                    + " assigned"
        ), ApiResponse(
            responseCode = "400", description = "You must supply either a"
                    + " Source Type ID, or the combination of (sourceTypeProducer, sourceTypeModel,"
                    + " catalogVersion)"
        ), ApiResponse(
            responseCode = "404", description = "Either the subject or the source type"
                    + " was not found."
        )
    )
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun assignSources(
        @PathVariable login: String?,
        @RequestBody sourceDto: MinimalSourceDetailsDTO
    ): ResponseEntity<MinimalSourceDetailsDTO> {
        authService.checkScope(Permission.SUBJECT_UPDATE)

        // find out source type id of supplied source
        var sourceTypeId = sourceDto.sourceTypeId
        if (sourceTypeId == null) {
            // check if combination (producer, model, version) is present
            if (sourceDto.sourceTypeProducer == null || sourceDto.sourceTypeModel == null || sourceDto.sourceTypeCatalogVersion == null) {
                throw BadRequestException(
                    "Producer or model or version value for the "
                            + "source-type is null", EntityName.SOURCE_TYPE, ErrorConstants.ERR_VALIDATION
                )
            }
            sourceTypeId = sourceTypeService
                .findByProducerAndModelAndVersion(
                    sourceDto.sourceTypeProducer!!,
                    sourceDto.sourceTypeModel!!,
                    sourceDto.sourceTypeCatalogVersion!!
                ).id
            // also update the sourceDto, since we pass it on to SubjectService later
            sourceDto.sourceTypeId = sourceTypeId
        }

        // check the subject id
        val sub = subjectService.findOneByLogin(login)

        // find the actively assigned project for this subject
        val currentProject: Project = projectRepository.findByIdWithOrganization(sub.activeProject?.id)
            ?: throw InvalidRequestException(
                "Requested subject does not have an active project",
                EntityName.SUBJECT, ErrorConstants.ERR_ACTIVE_PARTICIPANT_PROJECT_NOT_FOUND
            )

        authService.checkPermission(Permission.SUBJECT_UPDATE, { e: EntityDetails ->
            e
                .project(currentProject.projectName)
                .subject(sub.user!!.login)
        })

        // find whether the relevant source-type is available in the subject's project
        val sourceType = projectRepository
            .findSourceTypeByProjectIdAndSourceTypeId(currentProject.id, sourceTypeId)
            ?: throw BadRequestException(
                    "No valid source-type found for project."
                            + " You must provide either valid source-type id or producer, model,"
                            + " version of a source-type that is assigned to project",
                    EntityName.SUBJECT, ErrorConstants.ERR_SOURCE_TYPE_NOT_PROVIDED
                )

        // check if any of id, sourceID, sourceName were non-null
        val existing = Stream.of(
            sourceDto.id, sourceDto.sourceName,
            sourceDto.sourceId
        )
            .anyMatch { obj: Serializable? -> Objects.nonNull(obj) }

        // handle the source registration
        val sourceRegistered = subjectService
            .assignOrUpdateSource(sub, sourceType, currentProject, sourceDto)

        // Return the correct response type, either created if a new source was created, or ok if
        // an existing source was provided. If an existing source was given but not found, the
        // assignOrUpdateSource would throw an error, and we would not reach this point.
        return if (!existing) {
            ResponseEntity.created(ResourceUriService.getUri(sourceRegistered))
                .headers(
                    HeaderUtil.createEntityCreationAlert(
                        EntityName.SOURCE,
                        sourceRegistered.sourceName
                    )
                )
                .body(sourceRegistered)
        } else {
            ResponseEntity.ok()
                .headers(
                    HeaderUtil.createEntityUpdateAlert(
                        EntityName.SOURCE,
                        sourceRegistered.sourceName
                    )
                )
                .body(sourceRegistered)
        }
    }

    /**
     * Get sources assigned to a subject.
     *
     * @param login the subject login
     * @return the sources
     */
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSubjectSources(
        @PathVariable login: String?,
        @RequestParam(value = "withInactiveSources", required = false) withInactiveSourcesParam: Boolean?
    ): ResponseEntity<List<MinimalSourceDetailsDTO>> {
        authService.checkScope(Permission.SUBJECT_READ)
        val withInactiveSources = withInactiveSourcesParam != null && withInactiveSourcesParam
        // check the subject id
        val subject = subjectRepository.findOneWithEagerBySubjectLogin(login)
            ?: throw NoSuchElementException()
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            e
                .project(subject.associatedProject?.projectName)
            e.subject(login)
        })
        return if (withInactiveSources) {
            ResponseEntity.ok(subjectService.findSubjectSourcesFromRevisions(subject))
        } else {
            log.debug("REST request to get sources of Subject : {}", login)
            ResponseEntity.ok(subjectService.getSources(subject))
        }
    }

    /**
     * POST  /subjects/:login/sources/:sourceName Update source attributes and source-name.
     *
     *
     * The request body is a [Map] of strings. This request allows
     * update of attributes only. Attributes will be merged and if a new value is
     * provided for an existing key, the new value will be updated. The request will be validated
     * for SUBJECT.UPDATE permission. SUBJECT.UPDATE is expected to keep the permissions aligned
     * with permissions from dynamic source registration and update instead of checking for
     * SOURCE_UPDATE.
     *
     *
     * @param attributes The [Map] specification
     * @return The [MinimalSourceDetailsDTO] completed with all identifying fields.
     * @throws NotFoundException if the subject or the source not found using given ids.
     */
    @PostMapping(
        "/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/sources/{sourceName:"
                + Constants.ENTITY_ID_REGEX + "}"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "An existing source was updated"),
        ApiResponse(responseCode = "400", description = "You must supply existing sourceId)"),
        ApiResponse(
            responseCode = "404", description = "Either the subject or the source was"
                    + " not found."
        )
    )
    @Timed
    @Throws(NotFoundException::class, NotAuthorizedException::class)
    fun updateSubjectSource(
        @PathVariable login: String,
        @PathVariable sourceName: String, @RequestBody attributes: Map<String, String>?
    ): ResponseEntity<MinimalSourceDetailsDTO> {
        authService.checkScope(Permission.SUBJECT_UPDATE)

        // check the subject id
        val subject = subjectRepository.findOneWithEagerBySubjectLogin(login)
            ?: throw NotFoundException(
                "Subject ID not found",
                EntityName.SUBJECT, ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                Collections.singletonMap("subjectLogin", login)
            )
        authService.checkPermission(Permission.SUBJECT_UPDATE, { e: EntityDetails ->
            e
                .project(subject.associatedProject?.projectName)
            e.subject(login)
        })

        // find source under subject
        val source = subject.sources.stream()
            .filter { s: Source -> s.sourceName == sourceName }
            .findAny()
            .orElseThrow {
                val errorParams: MutableMap<String, String> = HashMap()
                errorParams["subjectLogin"] = login
                errorParams["sourceName"] = sourceName
                NotFoundException(
                    "Source not found under assigned sources of "
                            + "subject", EntityName.SUBJECT, ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                    errorParams
                )
            }

        // there should be only one source under a source-name.
        return ResponseEntity.ok(sourceService.safeUpdateOfAttributes(source, attributes))
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubjectResource::class.java)
    }
    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/datalogs")
    @Timed
    @Throws (
        NotAuthorizedException::class
    )
    fun getSubjectDataLog(@PathVariable login: String) : ResponseEntity<List<DataLogDTO>> {

        authService.checkScope(Permission.SUBJECT_READ)
        val dataLogDTOList = ArrayList<DataLogDTO>();

        for(groupingType in DataGroupingType.values()) {

            val connectDataLog = connectDataLogRepository.findDataLogsByUserIdAndDataGroupingType(login, groupingType.toString()).orElse(null);


            if(connectDataLog != null) {
                val dataLogDTO = DataLogDTO();

                dataLogDTO.time = connectDataLog.time;
                dataLogDTO.groupingType = connectDataLog.dataGroupingType;

                dataLogDTOList.add(dataLogDTO);

            }
        }
        return ResponseEntity.ok(dataLogDTOList);
    }

    @PostMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/reportready")
    @Timed
    @Throws (
        NotAuthorizedException::class
    )
    fun makeReportReady(@PathVariable login: String) : ResponseEntity<List<DataLogDTO>> {

        authService.checkScope(Permission.SUBJECT_READ)

        val subject = subjectService.findOneByLogin(login);

        val roles  =  roleRepository.findAllRolesByProjectName(subject.activeProject!!.projectName!!);
        log.info("after getting roles")
        var usersToEmail : List<User> = listOf();

        for (role in roles) {
            log.info("role ${role.authority}")

            if(role.authority.toString() == RoleAuthority.PROJECT_ADMIN.authority) {
                Hibernate.initialize(role.users) // Forces initialization
                for(user in role.users) {
                    usersToEmail += user
                }
            }
        }

        log.info("users in list ${usersToEmail}")

        return ResponseEntity.ok(null);
    }

//    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/datasummary")
//    @Timed
//    @Throws (
//        NotAuthorizedException::class
//    )
//    fun getDataSummary(@PathVariable login: String) : ResponseEntity<Map<String, List<Double>>> {
//        authService.checkScope(Permission.SUBJECT_READ)
//        val monthlyStatistics :  Map<String, List<Double>> = mapOf()
//        val subject = subjectService.findOneByLogin(login);
//
//        if(subject.activeProject != null) {
//            val awsService =   AWSService();
//            val folderPrefix = "output/" + subject.activeProject?.projectName + "/" + login + "/Data_summary.pdf";
//
//            val keyName = subject.activeProject?.projectName + "/" + login + "/Data_summary.pdf"
//
//            val pressignedUrl = awsService.createPresignedGetUrl("output", keyName)
//            log.info("pressigned url is ${pressignedUrl}")
//
//
//     //       val data = awsService.useHttpUrlConnectionToGetDataAsInputStream(pressignedUrl);
//
//     //       log.info("we have data $data")
//            val monthlyStatistics =   awsService.readLocalFile();
//        }
//
//  //      log.info("[AWS-S3] REST request to url  : {}", url)
//
////        val bytes = awsService.useHttpUrlConnectionToGet(url);
////        log.info("[AWS-S3] got the bytes")
////        val downloadedFile: MutableMap<String, String> = HashMap()
////        downloadedFile["fileName"] = "PDF file"
////        downloadedFile["fileBytes"] = Base64.getEncoder().encodeToString(bytes);
//        return ResponseEntity.ok(monthlyStatistics);
//    }


    @GetMapping("/subjects/{login:" + Constants.ENTITY_ID_REGEX + "}/datasummary")
    @Timed
    @Throws (
        NotAuthorizedException::class
    )
    fun getDataSummary(@PathVariable login: String) : ResponseEntity<DataSummaryResult> {
        authService.checkScope(Permission.SUBJECT_READ)
        val awsService =   AWSService();

        val subject = subjectRepository.findOneWithEagerBySubjectLogin(login);
        val project = subject!!.activeProject!!.projectName!!;
        
        val monthlyStatistics =   awsService.startProcessing(project, login, DataSource.S3)
        return ResponseEntity.ok(monthlyStatistics);
    }


    @GetMapping("/subjects/externalId")    @Timed
    @Throws (
        NotAuthorizedException::class
    )
    fun getAllExternalIds( subjectCriteria: SubjectCriteria) :  ResponseEntity<List<String?>>  {
        authService.checkScope(Permission.SUBJECT_READ)

        val allExternalIds = subjectRepository.findAllExternalIds();

        return ResponseEntity.ok(allExternalIds);
    }
}
