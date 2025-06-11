package org.radarbase.management.service

import QueryGroupContentDTO
import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryGroupContent
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.QueryGroupContentRepository
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.service.dto.QueryContentDTO
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
        val queryContentList = queryContentRepository.findAllByQueryGroupId(queryGroupId);

        if(!queryContentList.isEmpty()) {
            queryContentRepository.deleteAll(queryContentList);
        }
    }

    fun saveAll(queryGroupId: Long, contentGroupName: String, queryContentDTOList: List<QueryContentDTO>) {
        val decoder = Base64.getDecoder()
        val partSeparator = ","

        if (queryContentDTOList.isNotEmpty()) {
            val queryGroupOptional = queryGroupRepository.findById(queryGroupId)

            if (!queryGroupOptional.isPresent) {
                throw Exception("non existing querygroup ${queryContentDTOList[0].queryGroupId}: No ID")
            }

            deleteAnyExistingContent(queryGroupId)

            for (queryContentDTO in queryContentDTOList) {
                val queryContent = QueryContent().apply {
                    queryGroup = queryGroupOptional.get()
                    type = queryContentDTO.type
                    if (type == ContentType.IMAGE) {
                        imageBlob = convertImgStringToByteArray(queryContentDTO.imageBlob!!)
                    } else {
                        value = queryContentDTO.value
                        heading = queryContentDTO.heading
                    }
                }

                queryContentRepository.save(queryContent)

                val queryGroupContent = QueryGroupContent().apply {
                    this.queryGroup = queryGroupOptional.get()
                    this.queryContent = queryContent
                    this.contentGroupName = contentGroupName
                    this.createdDate = ZonedDateTime.now()
                    this.updatedDate = ZonedDateTime.now()
                }

                queryGroupContentRepository.save(queryGroupContent)
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

    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
