package org.radarcns.management.service;

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
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
import org.radarcns.management.web.rest.errors.CustomServerException;
import org.radarcns.management.web.rest.errors.ErrorConstants;
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
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.history.RevisionRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RevisionService implements ApplicationContextAware {

    @Autowired
    private List<Repository> repositories;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CustomRevisionEntityRepository revisionEntityRepository;

    private EntityManager entityManager;

    private AuditReader auditReader;

    private final Map<Class, Optional<RevisionRepository>> repositoryMap = new HashMap<>();

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
        List<Object[]> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(entity.getClass(), false, true)
                .add(AuditEntity.id().eq(entity.getId()))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
        if (revisions.isEmpty()) {
            // we did not find any auditing info, so we just return an empty object
            return new EntityAuditInfo();
        }
        // the list will be ordered by revision number, so the first and last revision will be
        // the first and last elements of this list
        CustomRevisionEntity first = (CustomRevisionEntity) revisions.get(0)[1];
        CustomRevisionEntity last = (CustomRevisionEntity) revisions.get(revisions.size() - 1)[1];
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
     * Find the RevisionRepository for a given entity. This will cache the result in the local
     * repositoryMap field, and return future requests for the same entity from that map.
     *
     * @param entity the entity to find a repository for
     * @return an {@link Optional} that contains the repository if it was found.
     */
    public Optional<RevisionRepository> getRepository(AbstractEntity entity) {
        if (repositoryMap.containsKey(entity.getClass())) {
            return repositoryMap.get(entity.getClass());
        }
        // Find a repository that is a RevisionRepository, has the RepositoryDefinition
        // annotation which is needed for DefaultRepositoryMetadata, and has the correct domain type
        Optional<RevisionRepository> result = repositories.stream()
                .filter(repo -> repo instanceof RevisionRepository)
                .filter(repo -> Arrays.stream(repo.getClass().getInterfaces())
                        .anyMatch(repoInterface -> repoInterface
                                .isAnnotationPresent(RepositoryDefinition.class)
                                && DefaultRepositoryMetadata.getMetadata(repoInterface)
                                .getDomainType().equals(entity.getClass()))
                ).findFirst().map(repo -> (RevisionRepository) repo);
        repositoryMap.put(entity.getClass(), result);
        return result;
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
     * @throws CustomNotFoundException if the revision number does not exist
     */
    public RevisionInfoDTO getRevision(Integer revision) throws CustomNotFoundException {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.findOne(revision);
        if (revisionEntity == null) {
            throw new CustomNotFoundException("Requested revision not found", Collections
                    .emptyMap());
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
            throw new CustomNotFoundException("The requested revision could not be found.",
                    Collections.emptyMap());
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
        dtoMapperMap.putIfAbsent(entity.getClass(), addMapperForClass(entity.getClass()));
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
                log.error(ex.getMessage(), ex);
                continue;
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
            throw new CustomServerException(ErrorConstants.ERR_INTERNAL_SERVER_ERROR,
                    Collections.emptyMap());
        }
    }
}
