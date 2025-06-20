package org.radarbase.management.service

import QueryContentGroupDTO
import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryContentGroup
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.QueryContentGroupRepository
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.mapper.QueryContentMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.*

@Service
@Transactional
class QueryContentService(
    private val queryContentRepository: QueryContentRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryContentMapper: QueryContentMapper,
    private val queryContentGroupRepository: QueryContentGroupRepository
) {

    fun convertImgStringToByteArray(imgString: String): ByteArray {
        val decoder = Base64.getDecoder()
        val partSeparator = ","
        var encodedImg = imgString
        if (encodedImg.contains(partSeparator)) {
            encodedImg = encodedImg.split(partSeparator)[10]
        }
        return decoder.decode(encodedImg.toByteArray(StandardCharsets.UTF_8))
    }

    fun saveAllOrUpdate(contentGroupDTO: QueryContentGroupDTO) {
        val decoder = Base64.getDecoder()

        val queryGroup = queryGroupRepository.findById(
            contentGroupDTO.queryGroupId
                ?: throw Exception("Missing queryGroupId")
        ).orElseThrow { Exception("Query group not found") }

        val contentGroup = if (contentGroupDTO.id != null) {
            val existingGroup = queryContentGroupRepository.findById(contentGroupDTO.id)
                .orElseThrow { Exception("Content group with id ${contentGroupDTO.id} does not exist") }

            if (existingGroup.contentGroupName != contentGroupDTO.contentGroupName) {
                existingGroup.contentGroupName = contentGroupDTO.contentGroupName
                existingGroup.updatedDate = ZonedDateTime.now()
                queryContentGroupRepository.save(existingGroup)
            }

            val oldContents = queryContentRepository.findAllByQueryContentGroupId(existingGroup.id!!)
            queryContentRepository.deleteAll(oldContents)

            existingGroup
        } else {
            queryContentGroupRepository.save(
                QueryContentGroup().apply {
                    this.queryGroup = queryGroup
                    this.contentGroupName = contentGroupDTO.contentGroupName
                    this.createdDate = ZonedDateTime.now()
                    this.updatedDate = ZonedDateTime.now()
                }
            )
        }

        contentGroupDTO.queryContentDTOList?.forEach { dto ->
            val queryContent = QueryContent().apply {
                this.queryGroup = queryGroup
                this.queryContentGroup = contentGroup
                this.type = dto.type
                if (this.type == ContentType.IMAGE) {
                    this.imageBlob = decoder.decode(dto.imageBlob)
                } else {
                    this.value = dto.value
                    this.heading = dto.heading
                }
            }
            queryContentRepository.save(queryContent)
        }
    }



    fun findAllContentsByQueryGroupId(queryGroupId: Long): List<QueryContentDTO> {
        val queryContentList = queryContentRepository.findAllByQueryGroupId(queryGroupId)
        return queryContentList.mapNotNull { queryContentMapper.queryContentToQueryContentDTO(it) }
    }


    fun deleteQueryContentGroup(queryContentGroupId: Long) {
        queryContentRepository.deleteAllByQueryContentGroupId(queryContentGroupId)
        queryContentGroupRepository.deleteById(queryContentGroupId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
