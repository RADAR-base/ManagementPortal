package org.radarbase.management.service

import QueryGroupContentDTO
import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryGroupContent
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.QueryGroupContentRepository
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.dto.QueryContentGroupResponseDTO
import org.radarbase.management.service.mapper.QueryContentMapper
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.AbstractAuditable_.createdDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.EntityNotFoundException

@Service
@Transactional
public class QueryContentService(
    private val queryContentRepository: QueryContentRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryContentMapper: QueryContentMapper,
    private val queryGroupContentRepository: QueryGroupContentRepository

) {


    public fun convertImgStringToByteArray(imgString: String): ByteArray {
        val decoder = Base64.getDecoder()
        val partSeparator = ",";


        var encodedImg = imgString
        if(encodedImg.contains(partSeparator)) {
            encodedImg = encodedImg.split(partSeparator)[10];
        }
        val decodedByte = decoder.decode(encodedImg.toByteArray(StandardCharsets.UTF_8));
        return decodedByte;

    }

    private fun deleteAnyExistingContent(queryGroupId: Long) {
        queryGroupContentRepository.deleteAllByQueryGroupId(queryGroupId)
        queryContentRepository.deleteAllByQueryGroupId(queryGroupId)
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

        // existing contents
        val existingContentIds = queryContentRepository.findAllByQueryGroupId(queryGroupId)
            .map { it.id!! }
            .toSet()

        for (queryContentDTO in queryContentDTOList) {
            // if content not exist
            if (queryContentDTO.id == null ||
                !existingContentIds.contains(queryContentDTO.id)
            ) {
                val queryContent = QueryContent().apply {
                    this.queryGroup = queryGroup
                    this.type = queryContentDTO.type
                    if (this.type == ContentType.IMAGE) {
                        this.imageBlob = decoder.decode(queryContentDTO.imageBlob)
                    } else {
                        this.value = queryContentDTO.value
                        this.heading = queryContentDTO.heading
                    }
                }

                val savedContent = queryContentRepository.save(queryContent)

                QueryGroupContent().apply {
                    this.queryGroup = queryGroup
                    this.queryContent = savedContent
                    this.contentGroupName = contentGroupName
                    this.createdDate = ZonedDateTime.now()
                    this.updatedDate = ZonedDateTime.now()
                    queryGroupContentRepository.save(this)
                }
            }
        }
    }



    public fun findAllByQueryGroupId(queryGroupId: Long) : List<QueryContentDTO> {
        val queryContentList  = queryContentRepository.findAllByQueryGroupId(queryGroupId);

        val result : MutableList<QueryContentDTO> = mutableListOf();

        for(queryContent in queryContentList) {
            val queryContentDTO =  queryContentMapper.queryContentToQueryContentDTO(queryContent);
            result += queryContentDTO!!
        }

        return result;
    }

    fun findAllGroupContentsByQueryGroupId(queryGroupId: Long): List<QueryGroupContentDTO> {
        val groupContents = queryGroupContentRepository.findAllByQueryGroupId(queryGroupId)

        return groupContents.map { qgc ->
            QueryGroupContentDTO(
                contentGroupName = qgc.contentGroupName.toString(),
                queryGroupId = qgc.queryGroup?.id!!,
                queryContentId = qgc.queryContent?.id!!
            )
        }
    }
    //get all query contents by query group id
    fun getAllContentsQueryGroupId(queryGroupId: Long): List<QueryContentGroupResponseDTO>{
        val groupContents = queryGroupContentRepository.findAllByQueryGroupId(queryGroupId)

        val grouped = groupContents.groupBy { it.contentGroupName }

        return grouped.map { (groupName, groupItems) ->
            val queryContents = groupItems.mapNotNull {
                queryContentMapper.queryContentToQueryContentDTO(it.queryContent)
            }
            QueryContentGroupResponseDTO(
                contentGroupName = groupName,
                queryGroupId = queryGroupId,
                queryContentDTOList = queryContents
            )
        }
    }



    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
