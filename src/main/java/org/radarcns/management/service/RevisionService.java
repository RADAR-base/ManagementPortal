package org.radarcns.management.service;

import static org.radarcns.management.web.rest.errors.EntityName.REVISION;
import static org.radarcns.management.web.rest.errors.ErrorConstants.ERR_REVISIONS_NOT_FOUND;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.AbstractEntity;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.domain.audit.CustomRevisionMetadata;
import org.radarcns.management.domain.audit.EntityAuditInfo;
import org.radarcns.management.repository.CustomRevisionEntityRepository;
import org.radarcns.management.service.dto.RevisionDTO;
import org.radarcns.management.service.dto.RevisionInfoDTO;
import org.radarcns.management.web.rest.errors.InvalidStateException;
import org.radarcns.management.web.rest.errors.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashMap;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RevisionService implements ApplicationContextAware {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CustomRevisionEntityRepository revisionEntityRepository;

    private EntityManager entityManager;

    private AuditReader auditReader;

    private final Map<Class, Function<Object, Object>> dtoMapperMap = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(RevisionService.class);

    private static ApplicationContext applicationContext;

    /**
     * Initialize the auditReader and entitymanager.
     */
    @PostConstruct
    public void initAuditReader() {
        Map<String, Object> props = entityManagerFactory.getProperties();
        entityManager = entityManagerFactory.createEntityManager(props);
        auditReader = AuditReaderFactory.get(entityManager);
    }

    /**
     * Close the entity manager.
     */
    @PreDestroy
    public void closeEntityManager() {
        entityManager.close();
    }

    /**
     * Find audit info for a given entity. The audit info includes created by, created at, last
     * modified by and last modified at.
     *
     * @param entity the entity to look up
     * @return the audit information. The fields in this object can be null if that information
     *         was not available.
     */
    public EntityAuditInfo getAuditInfo(AbstractEntity entity) {
        // find first revision of the entity
        CustomRevisionEntity first;
        try {
            Object[] firstRevision = (Object[]) auditReader.createQuery()
                    .forRevisionsOfEntity(entity.getClass(), false, true)
                    .add(AuditEntity.id().eq(entity.getId()))
                    .add(AuditEntity.revisionNumber().minimize()
                            .computeAggregationInInstanceContext())
                    .getSingleResult();
            first = (CustomRevisionEntity) firstRevision[1];
        } catch (NonUniqueResultException ex) {
            // should not happen since we call 'minimize'
            throw new IllegalStateException("Query for first revision returned a "
                    + "non-unique result. Please report this to the administrator together with "
                    + "the request issued." , ex);
        } catch (NoResultException ex) {
            // we did not find any auditing info, so we just return an empty object
            return new EntityAuditInfo();
        }

        // find last revision of the entity
        CustomRevisionEntity last;
        try {
            Object[] lastRevision = (Object[]) auditReader.createQuery()
                    .forRevisionsOfEntity(entity.getClass(), false, true)
                    .add(AuditEntity.id().eq(entity.getId()))
                    .add(AuditEntity.revisionNumber().maximize()
                            .computeAggregationInInstanceContext())
                    .getSingleResult();
            last = (CustomRevisionEntity) lastRevision[1];
        } catch (NonUniqueResultException ex) {
            // should not happen since we call 'maximize'
            throw new IllegalStateException("Query for last revision returned a "
                            + "non-unique result. Please report this to the administrator together "
                            + "with the request issued.");
        } catch (NoResultException ex) {
            // we did not find any auditing info, so we just return an empty object
            return new EntityAuditInfo();
        }

        // now populate the result object and return it
        return new EntityAuditInfo()
                .setCreatedAt(ZonedDateTime.ofInstant(first.getTimestamp().toInstant(),
                        ZoneId.systemDefault()))
                .setCreatedBy(first.getAuditor())
                .setLastModifiedAt(ZonedDateTime.ofInstant(last.getTimestamp().toInstant(),
                        ZoneId.systemDefault()))
                .setLastModifiedBy(last.getAuditor());
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
     */
    public <T> T findRevision(Integer revisionNb, Long id, Class<T> clazz) {
        return (T) auditReader.createQuery().forRevisionsOfEntity(clazz, true, true)
                .add(AuditEntity.id().eq(id))
                .add(AuditEntity.revisionNumber().eq(revisionNb))
                .getSingleResult();
    }

    /**
     * Get a page of revisions.
     *
     * @param pageable Page information
     * @return the page of revisions {@link RevisionInfoDTO}
     */
    public Page<RevisionInfoDTO> getRevisions(Pageable pageable) {
        return revisionEntityRepository.findAll(pageable).map(rev ->
                RevisionInfoDTO.from(rev, getChangesForRevision(rev.getId())));
    }

    public Page<RevisionInfoDTO> findByDates(Date fromDate, Date toDate, Pageable pageable) {
        return revisionEntityRepository.findAllByTimestampBetween(fromDate, toDate, pageable)
                .map(rev -> RevisionInfoDTO.from(rev, getChangesForRevision(rev.getId())));
    }

    /**
     * Get a page of revisions for a given entity.
     *
     * @param pageable the page information
     * @param entity the entity for which to get the revisions
     * @return the requested page of revisions for the given entity
     */
    public Page<RevisionDTO> getRevisionsForEntity(Pageable pageable, AbstractEntity entity) {
        Number count = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(entity.getClass(), false, true)
                .add(AuditEntity.id().eq(entity.getId()))
                .addProjection(AuditEntity.revisionNumber().count()).getSingleResult();

        // find all revisions of the entity class that have the correct id
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(entity.getClass(), false, true)
                .add(AuditEntity.id().eq(entity.getId()));


        // add the page sorting information to the query
        if (pageable.getSort() != null) {
            pageable.getSort().forEach(order -> query.addOrder(order.getDirection().isAscending()
                    ? AuditEntity.property(order.getProperty()).asc()
                    : AuditEntity.property(order.getProperty()).desc()));
        }

        // add the page constraints (offset and amount of results)
        query.setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<Object[]> resultList = query.getResultList();
        List<RevisionDTO> revisionDtos = resultList.stream().map(objArray -> new RevisionDTO(
                new Revision(
                        new CustomRevisionMetadata((CustomRevisionEntity) objArray[1]),
                        objArray[0]),
                (RevisionType) objArray[2],
                toDto(objArray[0])))
                .collect(Collectors.toList());
        return new PageImpl<>(revisionDtos, pageable, count.longValue());
    }

    /**
     * Get a single revision.
     *
     * @param revision the revision number
     * @return the revision
     * @throws NotFoundException if the revision number does not exist
     */
    public RevisionInfoDTO getRevision(Integer revision) throws NotFoundException {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.findOne(revision);
        if (revisionEntity == null) {
            throw new NotFoundException("Revision not found with revision id", REVISION,
                    ERR_REVISIONS_NOT_FOUND,
                    Collections.singletonMap("revision-id", revision.toString()));
        }
        return RevisionInfoDTO.from(revisionEntity, getChangesForRevision(revision));
    }

    /**
     * Get changes for a specific revision number, ordered by revision type.
     *
     * @param revision the revision number
     * @return A map with as keys the revision types, and as values the list of changed objects
     *         of that type
     */
    public Map<RevisionType, List<Object>> getChangesForRevision(Integer revision) {
        // Custom implementation not using crosstyperevisionchangesreader.
        // It seems we need to clear the entitymanager before using the
        // crosstyperevisionchangesreader, or we get incorrect results: deleted entities do not
        // show up in revisions where they were still around. However clearing for every request
        // causes the revisions api to be quite slow so we retrieve the changes manually using
        // the AuditReader.
        Map<RevisionType, List<Object>> result = new HashMap<>();
        List<RevisionType> revisionTypes = Arrays.asList(RevisionType.values());
        revisionTypes.forEach(revisionType -> result.put(revisionType, new LinkedList<>()));
        CustomRevisionEntity revisionEntity = revisionEntityRepository.findOne(revision);
        if (revisionEntity == null) {
            throw new NotFoundException("The requested revision could not be found.", REVISION,
                ERR_REVISIONS_NOT_FOUND,
                    Collections.singletonMap("revision-id", revision.toString()));
        }
        revisionEntity.getModifiedEntityNames().forEach(entityName ->
                revisionTypes.forEach(revisionType -> result.get(revisionType).addAll(
                        auditReader.createQuery()
                                .forEntitiesModifiedAtRevision(classForEntityName(entityName),
                                        revision)
                                .add(AuditEntity.revisionType().eq(revisionType))
                                .getResultList())
                ));

        return result;
    }

    /**
     * Dynamically find the Mapstruct mapper that can map the entity to it's DTO counterpart,
     * then do the mapping and return the DTO.
     *
     * @param entity the entity to map to it's DTO form
     * @return the DTO form of the given entity
     */
    public Object toDto(Object entity) {
        if (entity == null) {
            return null;
        }
        // addMapperForClass adds quite some overhead, so better to check explicitly compared to
        // using putIfAbsent()
        if (!dtoMapperMap.containsKey(entity.getClass())) {
            dtoMapperMap.put(entity.getClass(), addMapperForClass(entity.getClass()));
        }
        return dtoMapperMap.get(entity.getClass()).apply(entity);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RevisionService.applicationContext = applicationContext;
    }

    /**
     * <p>Find the latest revision of an entity of a given class, that matches given criteria. This
     * is useful for finding deleted entities by properties other than their primary key. The
     * criteria should be defined such that zero or one entities match.</p>
     *
     * <p>Example: {@code getLatestRevisionForEntity(Subject.class, Arrays.asList(AuditEntity
     * .property("user.login").eq(requestedLogin)))}.</p>
     * @param clazz The entity class
     * @param criteria The list of criteria that will be added to the audit query
     * @return The DTO version of the latest revision of the requested entity, or an empty
     *         optional if no entity was found matching the given criteria.
     * @throws AuditException if the entity is not audited
     * @throws NonUniqueResultException if multiple enities match the criteria
     */
    public Optional<Object> getLatestRevisionForEntity(Class clazz, List<AuditCriterion> criteria)
            throws AuditException, NonUniqueResultException {
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(clazz, true, true);
        query.add(AuditEntity.revisionNumber().maximize().computeAggregationInInstanceContext());
        criteria.forEach(criterion -> query.add(criterion));
        try {
            return Optional.of(toDto(query.getSingleResult()));
        } catch (NoResultException ex) {
            log.debug("No entity of type " + clazz.getName() + " found in the revision history "
                    + "with the given criteria", ex);
            return Optional.empty();
        }
    }

    private Function<Object, Object> addMapperForClass(Class clazz) {
        // get a list of @Mapper annotated components
        ClassPathScanningCandidateComponentProvider scanner = new
                ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Mapper.class));
        // look only in the mapper package
        Set<BeanDefinition> beans = scanner.findCandidateComponents("org.radarcns.management"
                + ".service.mapper");
        for (BeanDefinition bd : beans) {
            String className = bd.getBeanClassName();
            // get the bean for the given class
            final Object mapper;
            try {
                mapper = applicationContext.getBean(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                // should not happen, we got the classname from the bean definition
                throw new InvalidStateException(ex.getMessage(), REVISION, "error.classNotFound");
            }
            // now we look for the correct method in the bean
            Optional<Method> method = Arrays.stream(mapper.getClass().getMethods())
                    // look for methods that return our entity's DTO, and take exactly one
                    // argument of the same type as our entity
                    .filter(m -> m.getGenericReturnType().getTypeName().endsWith(clazz
                            .getSimpleName() + "DTO") && m.getGenericParameterTypes().length == 1
                            && m.getGenericParameterTypes()[0].getTypeName().equals(clazz
                            .getTypeName()))
                    // there should not be more than one
                    .findFirst();
            if (method.isPresent()) {
                return obj -> {
                    try {
                        return method.get().invoke(mapper, obj);
                    } catch (IllegalAccessException ex) {
                        log.error(ex.getMessage(), ex);
                    } catch (InvocationTargetException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    return null;
                };
            }
        }
        // if we did not find a mapper for the class, just add a function that returns null
        return obj -> null;
    }

    private Class<?> classForEntityName(String entityName) {
        try {
            return Class.forName(entityName);
        } catch (ClassNotFoundException ex) {
            // this should not happen
            log.error("Unable to load class for modified entity", ex);
            throw new InvalidStateException(ex.getMessage(), REVISION, "error.classNotFound");
        }
    }
}
