package org.radarbase.management.service

import org.radarbase.management.domain.QueryContent
import org.radarbase.management.domain.QueryGroup
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.QueryContentRepository
import org.radarbase.management.repository.QueryGroupRepository
import org.radarbase.management.service.dto.QueryContentDTO
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
    private val queryGroupRepository: QueryGroupRepository

) {


    private fun convertImgStringToByteArray(imgString: String): ByteArray {
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
                    queryContent.imageBlob = convertImgStringToByteArray(queryContentDTO.value!!)
                } else {
                    queryContent.value = queryContentDTO.value
                }
                queryContentRepository.save((queryContent));
            }
        }
        queryContentRepository.flush();
    }

    public fun findAll() {

    }

   public fun findOne() {

   }
   public fun delete() {

   }

    public fun deleteAllByQueryGroupId(queryGroupId: Long) {
        val allContent = queryContentRepository.findAllByQueryGroupId(queryGroupId);
        queryContentRepository.deleteAll(allContent);
    }
    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
