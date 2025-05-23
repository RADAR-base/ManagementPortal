package org.radarbase.management.service

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.mapper.QueryContentMapper
import org.radarbase.management.web.rest.QueryContentResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.util.*

@Service
@Transactional
public class QueryContentService(
    private val queryContentRepository: QueryContentRepository,
    private val queryGroupRepository: QueryGroupRepository,
    private val queryContentMapper: QueryContentMapper

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

    public fun saveAll(queryGroupId: Long, queryContentDTOList: List<QueryContentDTO>) {
        val decoder = Base64.getDecoder()
        val partSeparator = ",";

        if(queryContentDTOList.size > 0 ) {
            val queryGroupOptional = queryGroupRepository.findById(queryGroupId)

            if(!queryGroupOptional.isPresent) {
                throw Exception("non existing querygroup ${queryContentDTOList[0].queryGroupId}: No ID")
            }

            deleteAnyExistingContent(queryGroupId);

            for(queryContentDTO in queryContentDTOList) {
                val queryContent = QueryContent();
                queryContent.queryGroup = queryGroupOptional.get()
                queryContent.type = queryContentDTO.type

                if(queryContent.type == ContentType.IMAGE ) {
                    queryContent.imageBlob = convertImgStringToByteArray(queryContentDTO.imageBlob!!)
                } else {
                    queryContent.value = queryContentDTO.value
                    queryContent.heading = queryContentDTO.heading
                }
                queryContentRepository.save((queryContent));
            }
        }
        log.info("[QUERY] before flush")
        queryContentRepository.flush();
        log.info("[QUERY] after flush")
    }

    public fun findAll() {

    }

   public fun findOne() {

   }
   public fun delete() {

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

    public fun deleteAllByQueryGroupId(queryGroupId: Long) {
        val allContent = queryContentRepository.findAllByQueryGroupId(queryGroupId);
        queryContentRepository.deleteAll(allContent);
    }


    public fun getContentByQueryGroupId(queryGroupId: Long): List<QueryContentDTO> {
        var queryContentList = queryContentRepository.findAllByQueryGroupId(queryGroupId);
        var result : List<QueryContentDTO> = mutableListOf()

        for(queryContentItem in queryContentList) {
            val queryContentDTO = queryContentMapper.queryContentToQueryContentDTO(queryContentItem);
            result += queryContentDTO!!;
        }

        return result;
    }
    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
