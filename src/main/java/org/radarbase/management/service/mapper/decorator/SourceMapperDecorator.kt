package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.domain.Source
import org.radarbase.management.repository.SourceRepository
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.service.mapper.SourceMapper
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

/**
 * Created by nivethika on 13-6-17.
 */
abstract class SourceMapperDecorator : SourceMapper {
    @Autowired
    @Qualifier("delegate")
    private val delegate: SourceMapper? = null

    @Autowired
    private val sourceRepository: SourceRepository? = null

    @Autowired
    private val subjectRepository: SubjectRepository? = null

    override fun minimalSourceDTOToSource(minimalSourceDetailsDto: MinimalSourceDetailsDTO): Source? {
        val source =
            sourceRepository
                ?.findOneBySourceId(minimalSourceDetailsDto.sourceId)
                ?: throw NotFoundException(
                    "Source ID " + minimalSourceDetailsDto.sourceId + " not found",
                    EntityName.Companion.SOURCE,
                    ErrorConstants.ERR_SOURCE_NOT_FOUND,
                    mapOf(Pair("sourceId", minimalSourceDetailsDto.sourceId.toString())),
                )
        source.assigned = minimalSourceDetailsDto.isAssigned
        return source
    }

    override fun sourceDTOToSource(sourceDto: SourceDTO): Source {
        val source = delegate?.sourceDTOToSource(sourceDto)
        if (sourceDto.id != null) {
            val existingSource =
                sourceDto.id?.let {
                    sourceRepository
                        ?.findById(it)
                        ?.orElseThrow<NotFoundException> {
                            NotFoundException(
                                "Source ID " + sourceDto.id + " not found",
                                EntityName.Companion.SOURCE,
                                ErrorConstants.ERR_SOURCE_NOT_FOUND,
                                mapOf(Pair("sourceId", sourceDto.id.toString())),
                            )
                        }
                }!!
            if (sourceDto.subjectLogin == null) {
                source?.subject = existingSource.subject
            } else {
                source?.subject = subjectRepository
                    ?.findOneWithEagerBySubjectLogin(sourceDto.subjectLogin)
                    ?: throw NoSuchElementException()
            }
        }
        return source!!
    }
}
