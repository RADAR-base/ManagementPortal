package org.radarbase.management.service.mapper.decorator

import org.radarbase.management.domain.Project
import org.radarbase.management.domain.QueryContent
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.service.MetaTokenService
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.mapper.QueryContentMapper
import org.radarbase.management.web.rest.errors.BadRequestException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.util.*

/**
 * Created by nivethika on 30-8-17.
 */
abstract class QueryContentDecorator : QueryContentMapper {

    @Autowired @Qualifier("delegate") private lateinit var delegate: QueryContentMapper


    override fun queryContentToQueryContentDTO(queryContent: QueryContent?): QueryContentDTO? {
        val dto = delegate.queryContentToQueryContentDTO(queryContent)
        dto?.imageBlob =   queryContent!!.imageBlob?.let { Base64.getEncoder().encodeToString(it) }
        return dto
    }

    override fun queryContentDTOToQueryContent(dto: QueryContentDTO?): QueryContent? {
        val queryContent = delegate.queryContentDTOToQueryContent(dto)
        queryContent?.imageBlob =   dto!!.imageBlob?.let { Base64.getDecoder().decode(it) }
        return queryContent
    }

}
