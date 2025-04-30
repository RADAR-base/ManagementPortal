package org.radarbase.management.service

import org.hibernate.envers.query.AuditEntity
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.domain.*
import org.radarbase.management.repository.*
import org.radarbase.management.repository.filters.SubjectSpecification
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.dto.UserDTO
import org.radarbase.management.service.mapper.ProjectMapper
import org.radarbase.management.service.mapper.SourceMapper
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.radarbase.management.web.rest.errors.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Page
import org.springframework.data.history.Revision
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.MalformedURLException
import java.net.URL
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import javax.annotation.Nonnull

/**
 * Created by nivethika on 26-5-17.
 */
@ConditionalOnProperty(prefix = "managementportal", name = ["legacyLogin"], havingValue = "true")
@Service
@Transactional
class SubjectServiceLegacyLogin(
    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val projectMapper: ProjectMapper,
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val sourceRepository: SourceRepository,
    @Autowired private val sourceMapper: SourceMapper,
    @Autowired private val roleRepository: RoleRepository,
    @Autowired private val groupRepository: GroupRepository,
    @Autowired private val revisionService: RevisionService,
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
    @Autowired private val passwordService: PasswordService,
    @Autowired private val authorityRepository: AuthorityRepository,
    @Autowired private val authService: AuthService
) : SubjectService {

    /**
     * Create a new subject.
     *
     * @param subjectDto the subject information
     * @return the newly created subject
     */
    @Transactional
    override suspend fun createSubject(subjectDto: SubjectDTO, activated: Boolean?): SubjectDTO? {
        val subject = subjectMapper.subjectDTOToSubject(subjectDto) ?: throw NullPointerException()
        //assign roles
        val user = subject.user
        val project = projectMapper.projectDTOToProject(subjectDto.project)
        val projectParticipantRole = getProjectParticipantRole(project, RoleAuthority.PARTICIPANT)
        val roles = user!!.roles
        roles?.add(projectParticipantRole)

        // Set group
        subject.group = getSubjectGroup(project, subjectDto.group)

        // set password and reset keys
        user.password = passwordService.generateEncodedPassword()
        user.resetKey = passwordService.generateResetKey()
        // setting default language key to "en", required to set email context, Find a workaround
        user.langKey = "en"
        user.resetDate = ZonedDateTime.now()
        // default subject is activated.
        user.activated = true
        //set if any devices are set as assigned
        if (subject.sources.isNotEmpty()) {
            subject.sources.forEach(Consumer { s: Source ->
                s.assigned = true
                s.subject(subject)
            })
        }
        if (subject.enrollmentDate == null) {
            subject.enrollmentDate = ZonedDateTime.now()
        }
        sourceRepository.saveAll(subject.sources)
        return subjectMapper.subjectToSubjectReducedProjectDTO(subjectRepository.save(subject))
    }

    override suspend fun createSubject(
        id: String,
        projectDto: ProjectDTO,
        externalId: String,
        attributes: Map<String, String>
    ): SubjectDTO? {
        TODO("Not yet implemented")
    }

    private fun getSubjectGroup(project: Project?, groupName: String?): Group? {
        return if (project == null || groupName == null) {
            null
        } else groupRepository.findByProjectIdAndName(project.id, groupName) ?: throw BadRequestException(
            "Group " + groupName + " does not exist in project " + project.projectName,
            EntityName.GROUP,
            ErrorConstants.ERR_GROUP_NOT_FOUND
        )
    }

    /**
     * Fetch Participant role of the project if available, otherwise create a new Role and assign.
     *
     * @param project project subject is assigned to
     * @return relevant Participant role
     * @throws java.util.NoSuchElementException if the authority name is not in the database
     */
    private fun getProjectParticipantRole(project: Project?, authority: RoleAuthority): Role {
        val ans: Role? = roleRepository.findOneByProjectIdAndAuthorityName(
            project?.id, authority.authority
        )
        return if (ans == null) {
            val subjectRole = Role()
            val auth: Authority = authorityRepository.findByAuthorityName(
                authority.authority
            ) ?: authorityRepository.save(Authority(authority))

            subjectRole.authority = auth
            subjectRole.project = project
            roleRepository.save(subjectRole)
            subjectRole
        } else ans
    }

    /**
     * Update a subject's information.
     *
     * @param newSubjectDto the new subject information
     * @return the updated subject
     */
    @Transactional
    override suspend fun updateSubject(newSubjectDto: SubjectDTO): SubjectDTO? {
        if (newSubjectDto.id == null) {
            return createSubject(newSubjectDto)
        }
        val subjectFromDb = ensureSubject(newSubjectDto)
        val sourcesToUpdate = subjectFromDb.sources
        //set only the devices assigned to a subject as assigned
        subjectMapper.safeUpdateSubjectFromDTO(newSubjectDto, subjectFromDb)
        sourcesToUpdate.addAll(subjectFromDb.sources)
        subjectFromDb.sources.forEach(Consumer { s: Source ->
            s.subject(subjectFromDb).assigned = true })
        sourceRepository.saveAll(sourcesToUpdate)
        // update participant role
        subjectFromDb.user!!.roles = updateParticipantRoles(subjectFromDb, newSubjectDto)
        // Set group
        subjectFromDb.group = getSubjectGroup(
            subjectFromDb.activeProject, newSubjectDto.group
        )
        return subjectMapper.subjectToSubjectReducedProjectDTO(
            subjectRepository.save(subjectFromDb)
        )
    }

    override fun activateSubject(login: String): SubjectDTO? {
        TODO("Not yet implemented")
    }

    private fun updateParticipantRoles(subject: Subject, subjectDto: SubjectDTO): MutableSet<Role> {
        if (subjectDto.project == null || subjectDto.project!!.projectName == null) {
            return subject.user!!.roles
        }
        val existingRoles = subject.user!!.roles.map {
            // make participant inactive in projects that do not match the new project
            if (it.authority!!.name == RoleAuthority.PARTICIPANT.authority && it.project!!.projectName != subjectDto.project!!.projectName) {
                return@map getProjectParticipantRole(it.project, RoleAuthority.INACTIVE_PARTICIPANT)
            } else {
                // do not modify other roles.
                return@map it
            }
        }.toMutableSet()

        // Ensure that given project is present
        val newProjectRole =
            getProjectParticipantRole(projectMapper.projectDTOToProject(subjectDto.project), RoleAuthority.PARTICIPANT)
        existingRoles.add(newProjectRole)

        return existingRoles

    }

    /**
     * Discontinue the given subject.
     *
     *
     * A discontinued subject is not deleted from the database, but will be prevented from
     * logging into the system, sending data, or otherwise interacting with the system.
     *
     * @param subjectDto the subject to discontinue
     * @return the discontinued subject
     */
    override fun discontinueSubject(subjectDto: SubjectDTO): SubjectDTO? {
        val subject = ensureSubject(subjectDto)
        // reset all the sources assigned to a subject to unassigned
        unassignAllSources(subject)

        // set the removed flag and deactivate the user to prevent them from refreshing their
        // access token
        subject.removed = true
        subject.user!!.activated = false
        return subjectMapper.subjectToSubjectReducedProjectDTO(subjectRepository.save(subject))
    }

    private fun ensureSubject(subjectDto: SubjectDTO): Subject {
        return try {
            subjectDto.id?.let { subjectRepository.findById(it).get() }
                ?: throw Exception("invalid subject ${subjectDto.login}: No ID")
        }
        catch(e: Throwable) {
            throw NotFoundException(
                "Subject with ID " + subjectDto.id + " not found.",
                EntityName.SUBJECT,
                ErrorConstants.ERR_SUBJECT_NOT_FOUND
            )
        }
    }

    /**
     * Unassign all sources from a subject. This method saves the unassigned sources, but does NOT
     * save the subject in question. This is the responsibility of the caller.
     *
     * @param subject The subject for which to unassign all sources
     */
    private fun unassignAllSources(subject: Subject) {
        subject.sources.forEach(Consumer { source: Source ->
            source.assigned = false
            source.subject = null
            source.deleted = true
            sourceRepository.save(source)
        })
        subject.sources.clear()
    }

    /**
     * Creates or updates a source for a subject. It creates and assigns a source of a for a
     * dynamicallyRegister-able sourceType. Currently, it is allowed to create only once source of a
     * dynamicallyRegistrable sourceType per subject. Otherwise finds the matching source and
     * updates meta-data.
     */
    @Transactional
    override suspend fun assignOrUpdateSource(
        subject: Subject, sourceType: SourceType, project: Project?, sourceRegistrationDto: MinimalSourceDetailsDTO
    ): MinimalSourceDetailsDTO {
        val assignedSource: Source
        if (sourceRegistrationDto.sourceId != null) {
            // update meta-data and source-name for existing sources
            assignedSource = updateSourceAssignedSubject(subject, sourceRegistrationDto)
        } else if (sourceType.canRegisterDynamically!!) {
            val sources = subjectRepository.findSubjectSourcesBySourceType(
                subject.user!!.login, sourceType.producer, sourceType.model, sourceType.catalogVersion
            )
            // create a source and register metadata
            // we allow only one source of a source-type per subject
            if (sources.isNullOrEmpty()) {
                var source = Source(sourceType).project(project).sourceType(sourceType).subject(subject)
                source.assigned = true
                source.attributes += sourceRegistrationDto.attributes
                // if source name is provided update source name
                if (sourceRegistrationDto.sourceName != null) {
                    // append the auto generated source-name to given source-name to avoid conflicts
                    source.sourceName = sourceRegistrationDto.sourceName + "_" + source.sourceName
                }
                // make sure there is no source available on the same name.
                if (sourceRepository.findOneBySourceName(source.sourceName!!) != null) {
                    throw ConflictException(
                        "SourceName already in use. Cannot create a " + "source with existing source-name ",
                        EntityName.SUBJECT,
                        ErrorConstants.ERR_SOURCE_NAME_EXISTS,
                        Collections.singletonMap("source-name", source.sourceName)
                    )
                }
                source = sourceRepository.save(source)
                assignedSource = source
                subject.sources.add(source)
            } else {
                throw ConflictException(
                    "A Source of SourceType with the specified producer, model and version" + " was already registered for subject login",
                    EntityName.SUBJECT,
                    ErrorConstants.ERR_SOURCE_TYPE_EXISTS,
                    sourceTypeAttributes(sourceType, subject)
                )
            }
        } else {
            // new source since sourceId == null, but canRegisterDynamically == false
            throw BadRequestException(
                "The source type is not eligible for dynamic " + "registration",
                EntityName.SOURCE_TYPE,
                "error.InvalidDynamicSourceRegistration",
                sourceTypeAttributes(sourceType, subject)
            )
        }
        subjectRepository.save(subject)
        return sourceMapper.sourceToMinimalSourceDetailsDTO(assignedSource)
    }

    /**
     * Updates source name and attributes of the source assigned to subject. Otherwise returns
     * [NotFoundException].
     * @param subject subject
     * @param sourceRegistrationDto details of source which need to be updated.
     * @return Updated [Source] instance.
     */
    private fun updateSourceAssignedSubject(
        subject: Subject, sourceRegistrationDto: MinimalSourceDetailsDTO
    ): Source {
        // for manually registered devices only add meta-data
        val source = subjectRepository.findSubjectSourcesBySourceId(
            subject.user?.login, sourceRegistrationDto.sourceId
        )
        if (source == null) {
            val errorParams: MutableMap<String, String?> = HashMap()
            errorParams["sourceId"] = sourceRegistrationDto.sourceId.toString()
            errorParams["subject-login"] = subject.user?.login
            throw NotFoundException(
                "No source with source-id to assigned to the subject with subject-login",
                EntityName.SUBJECT,
                ErrorConstants.ERR_SOURCE_NOT_FOUND,
                errorParams
            )
        }

        if (sourceRegistrationDto.sourceName != null) {
            source.sourceName = sourceRegistrationDto.sourceName
        }
        source.attributes += sourceRegistrationDto.attributes
        source.assigned = true
        source.subject = subject
        return sourceRepository.save(source)
    }

    /**
     * Gets all sources assigned to the subject identified by :login.
     *
     * @return list of sources
     */
    override fun getSources(subject: Subject): List<MinimalSourceDetailsDTO> {
        val sources = subjectRepository.findSourcesBySubjectLogin(subject.user?.login)
        if (sources.isEmpty()) throw NotFoundException(
            "Could not find sources for user ${subject.user}",
            EntityName.SUBJECT,
            ErrorConstants.ERR_SOURCE_NOT_FOUND,
            Collections.singletonMap("subjectLogin", subject.user?.login)
        )
        return sourceMapper.sourcesToMinimalSourceDetailsDTOs(sources)
    }

    /**
     * Delete the subject with the given login from the database.
     *
     * @param login the login
     */
    override fun deleteSubject(login: String?) {
        subjectRepository.findOneWithEagerBySubjectLogin(login)?.let { subject: Subject ->
            unassignAllSources(subject)
            subjectRepository.delete(subject)
            log.debug("Deleted Subject: {}", subject)
        } ?: throw NotFoundException(
            "subject not found for given login.",
            EntityName.SUBJECT,
            ErrorConstants.ERR_SUBJECT_NOT_FOUND,
            Collections.singletonMap("subjectLogin", login)
        )
    }

    /**
     * Finds all sources of subject including inactive sources.
     *
     * @param subject of whom the sources should be retrieved.
     * @return list of [MinimalSourceDetailsDTO] of sources.
     */
    override fun findSubjectSourcesFromRevisions(subject: Subject): List<MinimalSourceDetailsDTO>? {
        val revisions = subject.id?.let { subjectRepository.findRevisions(it) }
        // collect distinct sources in a set
        val sources: List<Source>? = revisions?.content?.flatMap { p: Revision<Int, Subject> -> p.entity.sources }
            ?.distinctBy { obj: Source -> obj.sourceId }

        return sources?.map { p: Source -> sourceMapper.sourceToMinimalSourceDetailsDTO(p) }?.toList()
    }

    /**
     * Get a specific revision for a given subject.
     *
     * @param login the login of the subject
     * @param revision the revision number
     * @return the subject at the given revision
     * @throws NotFoundException if there was no subject with the given login at the given
     * revision number
     */
    @Throws(NotFoundException::class, NotAuthorizedException::class)
    override fun findRevision(login: String?, revision: Int?): SubjectDTO {
        // first get latest known version of the subject, if it's deleted we can't load the entity
        // directly by e.g. findOneByLogin
        val latest = getLatestRevision(login)
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            e.project(latest.project?.projectName).subject(latest.login)
        })
        return revisionService.findRevision(
            revision, latest.id, Subject::class.java, subjectMapper::subjectToSubjectReducedProjectDTO
        ) ?: throw NotFoundException(
            "subject not found for given login and revision.",
            EntityName.SUBJECT,
            ErrorConstants.ERR_SUBJECT_NOT_FOUND,
            Collections.singletonMap("subjectLogin", login)
        )
    }

    /**
     * Get latest known revision of a subject with the given login.
     *
     * @param login the login of the subject
     * @return the latest revision for that subject
     * @throws NotFoundException if no subject was found with the given login
     */
    @Throws(NotFoundException::class)
    override fun getLatestRevision(login: String?): SubjectDTO {
        val user = revisionService.getLatestRevisionForEntity(
            User::class.java, listOf(AuditEntity.property("login").eq(login))
        ).orElseThrow {
            NotFoundException(
                "Subject latest revision not found " + "for login",
                EntityName.SUBJECT,
                ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                Collections.singletonMap("subjectLogin", login)
            )
        } as UserDTO
        return revisionService.getLatestRevisionForEntity(
            Subject::class.java, listOf(AuditEntity.property("user").eq(user))
        ).orElseThrow {
            NotFoundException(
                "Subject latest revision not found " + "for login",
                EntityName.SUBJECT,
                ErrorConstants.ERR_SUBJECT_NOT_FOUND,
                Collections.singletonMap("subjectLogin", login)
            )
        } as SubjectDTO
    }

    /**
     * Finds [Subject] from databased from login provided.
     * @param login of subject to look for.
     * @return [Subject] loaded.
     */
    @Nonnull
    override fun findOneByLogin(login: String?): Subject {
        val subject = subjectRepository.findOneWithEagerBySubjectLogin(login)
        return subject ?: throw NotFoundException(
            "Subject not found with login", EntityName.SUBJECT, ErrorConstants.ERR_SUBJECT_NOT_FOUND
        )
    }

    /**
     * Find all subjects matching given filter.
     * @param criteria filter and sort for subjects.
     * @return page of subjects matching filter.
     */
    override fun findAll(criteria: SubjectCriteria): Page<Subject> {
        // Pageable is required to set the page limit,
        // but the page should always be zero
        // since the lastLoadedId param defines the offset
        // within the query specification
        return subjectRepository.findAll(
            SubjectSpecification(criteria), criteria.pageable
        )
    }

    /**
     * Gets relevant privacy-policy-url for this subject.
     *
     *
     * If the active project of the subject has a valid privacy-policy-url returns that url.
     * Otherwise, it loads the default URL from ManagementPortal configurations that is
     * general.
     *
     * @param subject to get relevant policy url
     * @return URL of privacy policy for this token
     */
    override fun getPrivacyPolicyUrl(subject: Subject): URL {

        // load default url from config
        val policyUrl: String = subject.activeProject?.attributes?.get(ProjectDTO.PRIVACY_POLICY_URL)
            ?: managementPortalProperties.common.privacyPolicyUrl
        return try {
            URL(policyUrl)
        } catch (e: MalformedURLException) {
            val params: MutableMap<String, String?> = HashMap()
            params["url"] = policyUrl
            params["message"] = e.message
            throw InvalidStateException(
                "No valid privacy-policy Url configured. Please " + "verify your project's privacy-policy url and/or general url config",
                EntityName.OAUTH_CLIENT,
                ErrorConstants.ERR_NO_VALID_PRIVACY_POLICY_URL_CONFIGURED,
                params
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubjectService::class.java)
        private fun sourceTypeAttributes(
            sourceType: SourceType, subject: Subject
        ): Map<String, String?> {
            val errorParams: MutableMap<String, String?> = HashMap()
            errorParams["producer"] = sourceType.producer
            errorParams["model"] = sourceType.model
            errorParams["catalogVersion"] = sourceType.catalogVersion
            errorParams["userId"] = subject.user!!.login
            return errorParams
        }

        private fun <T> distinctByKey(keyExtractor: Function<in T, *>): Predicate<T> {
            val seen: MutableSet<Any> = HashSet()
            return Predicate { t: T -> seen.add(keyExtractor.apply(t)) }
        }
    }
}
