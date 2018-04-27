package org.radarcns.management.service;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.mapstruct.Mapper;
import org.radarcns.management.domain.AbstractEntity;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.domain.audit.CustomRevisionMetadata;
import org.radarcns.management.repository.CustomRevisionEntityRepository;
import org.radarcns.management.service.dto.RevisionDTO;
import org.radarcns.management.service.dto.RevisionInfoDTO;
import org.radarcns.management.web.rest.errors.CustomNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
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
     * Search the audit log for when the given entity was created.
     *
     * @param entity the entity for which to find the creation time
     * @return an {@link Optional} of the {@link Instant} of the created time
     */
    public Optional<Instant> getCreatedAt(AbstractEntity entity) {
        Optional<RevisionRepository> repo = getRepository(entity);
        if (!repo.isPresent()) {
            log.debug("No RevisionRepository found for class {}", entity.getClass().getName());
            return Optional.empty();
        }
        try {
            List<Revision> revisions = repo.get().findRevisions(entity.getId()).getContent();
            return Optional.of(revisions.get(0).getRevisionDate().toDate().toInstant());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
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
        // if we don't clear the entitymanager before using the crosstyperevisionchangesreader we
        // get incorrect results, not sure why, need to investigate later, since clearing for
        // every request causes the revisions api to be quite slow
        entityManager.clear();
        return auditReader.getCrossTypeRevisionChangesReader()
                .findEntitiesGroupByRevisionType(revision).entrySet().stream().collect(
                        Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                                .map(this::toDto).collect(Collectors.toList())));
    }

    /**
     * Dynamically find the Mapstruct mapper that can map the entity to it's DTO counterpart,
     * then do the mapping and return the DTO.
     *
     * @param entity the entity to map to it's DTO form
     * @return the DTO form of the given entity
     */
    public Object toDto(Object entity) {
        dtoMapperMap.putIfAbsent(entity.getClass(), addMapperForClass(entity.getClass()));
        return dtoMapperMap.get(entity.getClass()).apply(entity);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RevisionService.applicationContext = applicationContext;
    }

    /**
     * <p>Find the latest revision of an entity of a given class, that matches given criteria. This
     * is useful for finding deleted entities by properties other than their primary key.</p>
     *
     * <p>Example: {@code getLatestRevisionForEntity(Subject.class, Arrays.asList(AuditEntity
     * .property("user.login").eq(requestedLogin)))}.</p>
     * @param clazz The entity class
     * @param criteria The list of criteria that will be added to the audit query
     * @return The DTO version of the latest revision of the requested entity
     */
    public Optional<Object> getLatestRevisionForEntity(Class clazz, List<AuditCriterion> criteria)
            throws CustomNotFoundException {
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(clazz, true, true);
        query.addOrder(AuditEntity.id().desc());
        criteria.forEach(criterion -> query.add(criterion));
        List<Object> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toDto(resultList.get(0)));
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
}
