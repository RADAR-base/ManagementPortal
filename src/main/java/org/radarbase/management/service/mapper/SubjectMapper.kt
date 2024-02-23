package org.radarbase.management.service.mapper

import org.mapstruct.DecoratedWith
import org.mapstruct.IterableMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.radarbase.management.domain.Subject
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.decorator.SubjectMapperDecorator

/**
 * Mapper for the entity Subject and its DTO SubjectDTO.
 */
@Mapper(
    componentModel = "spring",
    uses = [UserMapper::class, ProjectMapper::class, SourceMapper::class, RoleMapper::class]
)
@DecoratedWith(
    SubjectMapperDecorator::class
)
interface SubjectMapper {
    @Mapping(source = "user.login", target = "login")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(source = "group.name", target = "group")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    fun subjectToSubjectDTO(subject: Subject?): SubjectDTO?

    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "group.name", target = "group")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    fun subjectToSubjectReducedProjectDTO(subject: Subject?): SubjectDTO?

    @Named(value = "subjectReducedProjectDTO")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "group.name", target = "group")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "user.roles", target = "roles")
    fun subjectToSubjectWithoutProjectDTO(subject: Subject?): SubjectDTO?

    @IterableMapping(qualifiedByName = ["subjectReducedProjectDTO"])
    fun subjectsToSubjectReducedProjectDTOs(subjects: List<Subject?>?): List<SubjectDTO?>?

    @Mapping(source = "login", target = "user.login")
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "user.email", ignore = true)
    @Mapping(target = "user.activated", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(source = "roles", target = "user.roles")
    @Mapping(target = "metaTokens", ignore = true)
    fun subjectDTOToSubject(subjectDto: SubjectDTO?): Subject?

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "removed", ignore = true)
    @Mapping(target = "metaTokens", ignore = true)
    @Mapping(target = "group", ignore = true)
    fun safeUpdateSubjectFromDTO(subjectDto: SubjectDTO?, @MappingTarget subject: Subject?): Subject?

    /**
     * Generating the fromId for all mappers if the databaseType is sql, as the class has
     * relationship to it might need it, instead of creating a new attribute to know if the entity
     * has any relationship from some other entity.
     *
     * @param id id of the entity
     * @return the entity instance
     */
    fun subjectFromId(id: Long?): Subject? {
        if (id == null) {
            return null
        }
        val subject = Subject()
        subject.id = id
        return subject
    }
}
