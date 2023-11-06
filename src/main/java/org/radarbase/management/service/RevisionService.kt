package org.radarbase.management.service

import org.hibernate.envers.AuditReader
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.RevisionType
import org.hibernate.envers.exception.AuditException
import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.criteria.AuditCriterion
import org.mapstruct.Mapper
import org.radarbase.management.domain.AbstractEntity
import org.radarbase.management.domain.audit.CustomRevisionEntity
import org.radarbase.management.domain.audit.CustomRevisionMetadata
import org.radarbase.management.domain.audit.EntityAuditInfo
import org.radarbase.management.repository.CustomRevisionEntityRepository
import org.radarbase.management.service.dto.RevisionDTO
import org.radarbase.management.service.dto.RevisionInfoDTO
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.InvalidStateException
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.history.Revision
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Stream
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.NonUniqueResultException
import javax.persistence.PersistenceContext
import javax.validation.constraints.NotNull

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
open class RevisionService(@param:Autowired private val revisionEntityRepository: CustomRevisionEntityRepository) :
    ApplicationContextAware {
    @PersistenceContext
    private val entityManager: EntityManager? = null
    private val dtoMapperMap: ConcurrentMap<Class<*>?, Function<Any, Any?>> = ConcurrentHashMap()

    /**
     * Find audit info for a given entity. The audit info includes created by, created at, last
     * modified by and last modified at.
     *
     * @param entity the entity to look up
     * @return the audit information. The fields in this object can be null if that information
     * was not available.
     */
    fun getAuditInfo(entity: AbstractEntity): EntityAuditInfo {
        val auditReader = auditReader
        return try {
            // find first revision of the entity
            val firstRevision = auditReader.createQuery()
                .forRevisionsOfEntity(entity.javaClass, false, true)
                .add(AuditEntity.id().eq(entity.id))
                .add(
                    AuditEntity.revisionNumber().minimize()
                        .computeAggregationInInstanceContext()
                )
                .singleResult as Array<*>
            val first = firstRevision[1] as CustomRevisionEntity

            // find last revision of the entity
            val lastRevision = auditReader.createQuery()
                .forRevisionsOfEntity(entity.javaClass, false, true)
                .add(AuditEntity.id().eq(entity.id))
                .add(
                    AuditEntity.revisionNumber().maximize()
                        .computeAggregationInInstanceContext()
                )
                .singleResult as Array<*>
            val last = lastRevision[1] as CustomRevisionEntity

            // now populate the result object and return it
            EntityAuditInfo()
                .setCreatedAt(
                    ZonedDateTime.ofInstant(
                        first.timestamp!!.toInstant(),
                        ZoneId.systemDefault()
                    )
                )
                .setCreatedBy(first.auditor)
                .setLastModifiedAt(
                    ZonedDateTime.ofInstant(
                        last.timestamp!!.toInstant(),
                        ZoneId.systemDefault()
                    )
                )
                .setLastModifiedBy(last.auditor)
        } catch (ex: NonUniqueResultException) {
            // should not happen since we call 'minimize'
            throw IllegalStateException(
                "Query for revision returned a "
                        + "non-unique result. Please report this to the administrator together with "
                        + "the request issued.", ex
            )
        } catch (ex: NoResultException) {
            // we did not find any auditing info, so we just return an empty object
            EntityAuditInfo()
        }
    }

    /**
     * Find a specific revision of a specific entity. The repository methods seem not to be able
     * to find back a deleted entity with their findRevision method.
     *
     * @param revisionNb the revision number
     * @param id the entity id
     * @param clazz the entity class
     * @param <T> the entity class
     * @return the entity at the specified revision
    </T> */
    fun <T, R> findRevision(
        revisionNb: Int?,
        id: Long?,
        clazz: Class<T>?,
        dtoMapper: Function<T, R>
    ): R? {
        val value: T? = auditReader.createQuery()
            .forRevisionsOfEntity(clazz, true, true)
            .add(AuditEntity.id().eq(id))
            .add(AuditEntity.revisionNumber().eq(revisionNb))
            .singleResult as T
        return if (value != null) dtoMapper.apply(value) else null
    }

    /**
     * Get a page of revisions.
     *
     * @param pageable Page information
     * @return the page of revisions [RevisionInfoDTO]
     */
    fun getRevisions(pageable: Pageable): Page<RevisionInfoDTO> {
        return revisionEntityRepository.findAll(pageable)
            .map { rev -> RevisionInfoDTO.from(rev!!, getChangesForRevision(rev.id)) }
    }

    /**
     * Get a page of revisions for a given entity.
     *
     * @param pageable the page information
     * @param entity the entity for which to get the revisions
     * @return the requested page of revisions for the given entity
     */
    fun getRevisionsForEntity(pageable: Pageable, entity: AbstractEntity): Page<RevisionDTO> {
        val auditReader = auditReader
        val count = auditReader.createQuery()
            .forRevisionsOfEntity(entity.javaClass, false, true)
            .add(AuditEntity.id().eq(entity.id))
            .addProjection(AuditEntity.revisionNumber().count())
            .singleResult as Number

        // find all revisions of the entity class that have the correct id
        val query = auditReader.createQuery()
            .forRevisionsOfEntity(entity.javaClass, false, true)
            .add(AuditEntity.id().eq(entity.id))

        // add the page sorting information to the query
        pageable.sort
            .forEach(Consumer { order: Sort.Order ->
                query.addOrder(
                    if (order.direction.isAscending) AuditEntity.property(
                        order.property
                    ).asc() else AuditEntity.property(order.property).desc()
                )
            })

        // add the page constraints (offset and amount of results)
        query.setFirstResult(Math.toIntExact(pageable.offset))
            .setMaxResults(Math.toIntExact(pageable.pageSize.toLong()))
        val dtoMapper = getDtoMapper(entity.javaClass)
        val resultList = query.resultList as List<Array<*>?>
        val revisionDtos = resultList
            .map { objArray: Array<*>? ->
                RevisionDTO(
                    Revision.of(
                        CustomRevisionMetadata((objArray!![1] as CustomRevisionEntity)),
                        objArray[0]
                    ),
                    objArray[2] as RevisionType,
                    objArray[0]?.let { dtoMapper.apply(it) }
                )
            }
        return PageImpl(revisionDtos, pageable, count.toLong())
    }

    /**
     * Get a single revision.
     *
     * @param revision the revision number
     * @return the revision
     * @throws NotFoundException if the revision number does not exist
     */
    @Throws(NotFoundException::class)
    fun getRevision(revision: Int): RevisionInfoDTO {
        val revisionEntity = revisionEntityRepository.findById(revision)
            .orElse(null)
            ?: throw NotFoundException(
                "Revision not found with revision id", EntityName.REVISION,
                ErrorConstants.ERR_REVISIONS_NOT_FOUND,
                Collections.singletonMap("revision-id", revision.toString())
            )
        return RevisionInfoDTO.from(revisionEntity, getChangesForRevision(revision))
    }

    /**
     * Get changes for a specific revision number, ordered by revision type.
     *
     * @param revision the revision number
     * @return A map with as keys the revision types, and as values the list of changed objects
     * of that type
     */
    fun getChangesForRevision(revision: Int): Map<RevisionType, MutableList<Any>> {
        // Custom implementation not using crosstyperevisionchangesreader.
        // It seems we need to clear the entitymanager before using the
        // crosstyperevisionchangesreader, or we get incorrect results: deleted entities do not
        // show up in revisions where they were still around. However, clearing for every request
        // causes the revisions api to be quite slow, so we retrieve the changes manually using
        // the AuditReader.
        val revisionEntity = revisionEntityRepository.findById(revision)
            .orElse(null)
            ?: throw NotFoundException(
                "The requested revision could not be found.", EntityName.REVISION,
                ErrorConstants.ERR_REVISIONS_NOT_FOUND,
                Collections.singletonMap("revision-id", revision.toString())
            )
        val auditReader = auditReader
        val result: MutableMap<RevisionType, MutableList<Any>> = HashMap(5)
        for (revisionType in RevisionType.values()) {
            result[revisionType] = ArrayList()
        }
        for (entityName in revisionEntity.modifiedEntityNames!!) {
            val cleanedEntityName = entityName.replace("org.radarcns.", "org.radarbase.")
            val entityClass = classForEntityName(cleanedEntityName)
            val dtoMapper = getDtoMapper(entityClass)
            for (revisionType in RevisionType.values()) {
                result[revisionType]
                    ?.addAll(
                        (listOf(auditReader.createQuery()
                            .forEntitiesModifiedAtRevision(entityClass, revision)
                            .add(AuditEntity.revisionType().eq(revisionType))
                            .resultList
                            .let { toDto(it) } as Collection<*>)
                    ))
            }
        }
        return result
    }

    /**
     * Dynamically find the Mapstruct mapper that can map the entity to it's DTO counterpart,
     * then do the mapping and return the DTO.
     *
     * @param entity the entity to map to it's DTO form
     * @return the DTO form of the given entity
     */
    private fun toDto(entity: Any?): Any? {
        return if (entity != null) getDtoMapper(entity.javaClass).apply(entity) else null
    }

    private fun getDtoMapper(@NotNull entity: Class<*>?): Function<Any, Any?> {
        return dtoMapperMap.computeIfAbsent(entity) { clazz: Class<*>? -> addMapperForClass(clazz) }
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }

    /**
     *
     * Find the latest revision of an entity of a given class, that matches given criteria. This
     * is useful for finding deleted entities by properties other than their primary key. The
     * criteria should be defined such that zero or one entities match.
     *
     *
     * Example: `getLatestRevisionForEntity(Subject.class, Arrays.asList(AuditEntity
     * .property("user.login").eq(requestedLogin)))`.
     * @param clazz The entity class
     * @param criteria The list of criteria that will be added to the audit query
     * @return The DTO version of the latest revision of the requested entity, or an empty
     * optional if no entity was found matching the given criteria.
     * @throws AuditException if the entity is not audited
     * @throws NonUniqueResultException if multiple enities match the criteria
     */
    @Throws(AuditException::class, NonUniqueResultException::class)
    fun getLatestRevisionForEntity(
        clazz: Class<*>,
        criteria: List<AuditCriterion?>
    ): Optional<Any> {
        val query = auditReader.createQuery()
            .forRevisionsOfEntity(clazz, true, true)
            .add(
                AuditEntity.revisionNumber().maximize()
                    .computeAggregationInInstanceContext()
            )
        criteria.forEach(Consumer { criterion: AuditCriterion? -> query.add(criterion) })
        return try {
            Optional.ofNullable(toDto(query.singleResult))
        } catch (ex: NoResultException) {
            log.debug(
                "No entity of type " + clazz.getName() + " found in the revision history "
                        + "with the given criteria", ex
            )
            Optional.empty()
        }
    }

    private fun addMapperForClass(clazz: Class<*>?): Function<Any, Any?> {
        // get a list of @Mapper annotated components
        val scanner = ClassPathScanningCandidateComponentProvider(true)
        scanner.addIncludeFilter(AnnotationTypeFilter(Mapper::class.java))
        // look only in the mapper package
        return scanner.findCandidateComponents("org.radarbase.management.service.mapper").stream()
            .flatMap(Function<BeanDefinition, Stream<out Function<Any, Any?>>> { bd: BeanDefinition ->
                val mapper = beanFromDefinition(bd)
                Arrays.stream(mapper.javaClass.getMethods()) // look for methods that return our entity's DTO, and take exactly one
                    // argument of the same type as our entity
                    .filter { m: Method ->
                        m.genericReturnType.typeName.endsWith(
                            clazz!!.getSimpleName() + "DTO"
                        ) && m.genericParameterTypes.size == 1 && m.genericParameterTypes[0].typeName == clazz.getTypeName()
                    }
                    .map(Function { method: Method ->
                        Function { obj: Any? ->
                            if (obj == null) {
                                return@Function null
                            }
                            try {
                                return@Function method.invoke(mapper, obj)
                            } catch (ex: IllegalAccessException) {
                                log.error(ex.message, ex)
                                return@Function null
                            } catch (ex: InvocationTargetException) {
                                log.error(ex.message, ex)
                                return@Function null
                            }
                        }
                    })
            })
            .findAny()
            .orElse(Function { null })
    }

    private fun beanFromDefinition(beanDefinition: BeanDefinition): Any {
        val className = beanDefinition.beanClassName
        // get the bean for the given class
        return try {
            applicationContext!!.getBean(Class.forName(className))
        } catch (ex: ClassNotFoundException) {
            // should not happen, we got the classname from the bean definition
            throw InvalidStateException(
                ex.message, EntityName.REVISION, "error.classNotFound"
            )
        }
    }

    private fun classForEntityName(entityName: String): Class<*> {
        return try {
            Class.forName(entityName)
        } catch (ex: ClassNotFoundException) {
            // this should not happen
            log.error("Unable to load class for modified entity", ex)
            throw InvalidStateException(ex.message, EntityName.REVISION, "error.classNotFound")
        }
    }

    private val auditReader: AuditReader
        get() = AuditReaderFactory.get(entityManager)

    companion object {
        private val log = LoggerFactory.getLogger(RevisionService::class.java)

        @Volatile
        private var applicationContext: ApplicationContext? = null
    }
}
