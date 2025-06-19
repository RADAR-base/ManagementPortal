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

    @Transactional
    fun saveAll(
        queryGroupId: Long,
        contentGroupName: String,
        queryContentDTOList: List<QueryContentDTO>
    ) {
        val decoder = Base64.getDecoder()
        val queryGroup = queryGroupRepository.findById(queryGroupId)
            .orElseThrow { Exception("non-existing querygroup $queryGroupId") }

        // Check if content group exists, if not create it
        val contentGroup = queryContentGroupRepository
            .findByQueryGroupIdAndContentGroupName(queryGroupId, contentGroupName)
            ?: queryContentGroupRepository.save(
                QueryContentGroup().apply {
                    this.queryGroup = queryGroup
                    this.contentGroupName = contentGroupName
                    this.createdDate = ZonedDateTime.now()
                    this.updatedDate = ZonedDateTime.now()
                }
            )

        val existingContentIds = queryContentRepository.findAllByQueryGroupId(queryGroupId)
            .mapNotNull { it.id }
            .toSet()

        for (queryContentDTO in queryContentDTOList) {
            if (queryContentDTO.id == null || !existingContentIds.contains(queryContentDTO.id)) {
                val queryContent = QueryContent().apply {
                    this.queryGroup = queryGroup
                    this.queryContentGroup = contentGroup
                    this.type = queryContentDTO.type
                    if (this.type == ContentType.IMAGE) {
                        this.imageBlob = decoder.decode(queryContentDTO.imageBlob)
                    } else {
                        this.value = queryContentDTO.value
                        this.heading = queryContentDTO.heading
                    }
                }

                queryContentRepository.save(queryContent)
            }
        }
    }

    fun findAllByQueryGroupId(queryGroupId: Long): List<QueryContentDTO> {
        val queryContentList = queryContentRepository.findAllByQueryGroupId(queryGroupId)
        return queryContentList.mapNotNull { queryContentMapper.queryContentToQueryContentDTO(it) }
    }

    fun getAllContentsQueryGroupId(queryGroupId: Long): List<QueryContentGroupDTO> {
        val contentGroups = queryContentGroupRepository.findAllByQueryGroupId(queryGroupId)

        return contentGroups.map { group ->
            val queryContents = queryContentRepository.findAllByQueryContentGroupId(group.id!!)
            val contentDTOs = queryContents.mapNotNull {
                queryContentMapper.queryContentToQueryContentDTO(it)
            }
            QueryContentGroupDTO(
                contentGroupName = group.contentGroupName,
                queryGroupId = queryGroupId,
                queryContentDTOList = contentDTOs,
                id= group.id
            )
        }
    }

    fun deleteQueryContentGroup(queryContentGroupId: Long) {
        //delete query contents first
        queryContentRepository.deleteAllByQueryContentGroupId(queryContentGroupId)
        queryContentGroupRepository.deleteById(queryContentGroupId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
