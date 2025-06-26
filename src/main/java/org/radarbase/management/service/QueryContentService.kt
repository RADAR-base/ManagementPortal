package org.radarbase.management.service


import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.repository.*
import org.radarbase.management.service.dto.QueryContentDTO
import org.radarbase.management.service.dto.QueryContentGroupDTO
import org.radarbase.management.service.dto.QueryContentGroupResponseDTO
import org.radarbase.management.service.dto.QueryGroupContentDTO
import org.radarbase.management.service.mapper.QueryContentGroupMapper
import org.radarbase.management.service.mapper.QueryContentMapper
import org.radarbase.management.service.mapper.QueryGroupContentMapper
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
    private val queryContentGroupRepository: QueryContentGroupRepository,
    private val queryParticipantRepository: QueryParticipantRepository,
    private val subjectRepository: SubjectRepository,
    private val queryEvaluationRepository: QueryEvaluationRepository,
    private val queryContentGroup: QueryContentGroupRepository,
    private val queryParticipantContentRepository: QueryParticipantContentRepository,
    private val queryGroupContentMapper: QueryGroupContentMapper,
    private val queryContentGroupMapper: QueryContentGroupMapper


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

    fun getAllContentGroupsWithContentsQueryGroupId(queryGroupId: Long): List<QueryContentGroupDTO> {
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
        queryContentRepository.deleteAllByQueryContentGroupId(queryContentGroupId)
        queryContentGroupRepository.deleteById(queryContentGroupId)
    }


    fun sendNotification(contentGroup: QueryContentGroup) {
        //TODO: implement this once the notification capability was added
    }

     fun shouldSendNotification(evaluations : List<QueryEvaluation>): Boolean {
        return when(evaluations.size) {
            1 -> evaluations[0].result ?: false
            2 -> {
                val (latestEvaluation, previousEvaluations) = evaluations
                latestEvaluation.result == true && previousEvaluations.result == false
            }
            else -> false
        }
    }

    private fun saveParticipantContentGroup(queryGroup: QueryGroup, queryContentGroup: QueryContentGroup, subject: Subject) {
        val participantContentGroup = QueryParticipantContent()
        participantContentGroup.queryContentGroup = queryContentGroup
        participantContentGroup.queryGroup = queryGroup
        participantContentGroup.subject = subject
        participantContentGroup.createdDate = ZonedDateTime.now();
        participantContentGroup.isArchived = false;

        queryParticipantContentRepository.save(participantContentGroup);
    }




    fun getRandomAlreadyAssignedContent(queryGroup: QueryGroup, subject: Subject): QueryContentGroup? {
        val queryGroupId = queryGroup.id ?: return null

        val assignedContentGroups = queryParticipantContentRepository.findBySubjectAndQueryGroup(subject, queryGroup).map { it.queryContentGroup }

        return assignedContentGroups.randomOrNull()
    }


    fun tryAssignNewContent(queryGroup: QueryGroup, subject: Subject) : QueryContentGroup? {
        val queryGroupId = queryGroup.id ?: return null


        val allContentGroups = queryContentGroupRepository.findAllByQueryGroupId(queryGroupId);
        val assignedContentGroups = queryParticipantContentRepository.findBySubjectAndQueryGroup(subject, queryGroup).map { it.queryContentGroup }
        val assignedContentGroupIds = assignedContentGroups.map { it?.id }.toSet()


        val uniqueContent = allContentGroups.filter { it.id !in assignedContentGroupIds }

        if(uniqueContent.isNotEmpty()){
            val newContent = uniqueContent.random();
            saveParticipantContentGroup(queryGroup, newContent, subject)
            return newContent
        }

        return null
    }



    fun getContentItemsForSubjectAndContentGroup(subjectId: Long, contentGroupId: Long) : List<QueryContentDTO> {
        var result : List<QueryContentDTO> = emptyList()


        val contentGroupOpt = queryContentGroupRepository.findById(contentGroupId)
        val subjectOpt = subjectRepository.findById(subjectId)


        if(contentGroupOpt.isPresent && subjectOpt.isPresent) {
            val contentGroup = contentGroupOpt.get()
            val subject = subjectOpt.get()

            val participantContentGroupList = queryParticipantContentRepository.findByQueryContentGroupAndSubject(contentGroup, subject);

            if(participantContentGroupList.isNotEmpty()) {
                result = findAllByContentGroupId(contentGroup.id!!)
            }

        }

        return result;
    }
    fun getAllContentGroupsForParticipant(subjectId: Long): Map<String, List<QueryContentGroupDTO>>  {
        val result = mutableMapOf<String, List<QueryContentGroupDTO>>()

        val subjectOpt = subjectRepository.findById(subjectId)

        if(subjectOpt.isPresent) {
            val subject = subjectOpt.get();
            val allAssignedParticipantContent = queryParticipantContentRepository.findBySubject(subject)


            for(participantContent in allAssignedParticipantContent) {
                val queryGroup = participantContent.queryGroup ?: continue
                val queryContentGroupDTO = queryContentGroupMapper.queryContentGroupToQueryContentGroupDTO(participantContent.queryContentGroup)
                val key = queryGroup.name ?: continue

                if(queryContentGroupDTO != null) {
                    result[key] = result.getOrDefault(key, mutableListOf()) + queryContentGroupDTO
                }
            }
        }
        return result
    }


    fun processCompletedQueriesForParticipant(participantId: Long): Boolean {

        val queryParticipantList = queryParticipantRepository.findBySubjectId(participantId)
        if (queryParticipantList.isEmpty()) return false

        val subjectOpt = subjectRepository.findById(participantId)
        if (!subjectOpt.isPresent) return false

        val subject = subjectOpt.get()

        for (queryParticipant in queryParticipantList) {
            if(queryParticipant.queryGroup != null) {
                val evaluations = queryEvaluationRepository.findTop2BySubjectAndQueryGroupOrderByCreatedDateDesc(
                    subject,
                    queryParticipant.queryGroup!!
                )

                if(evaluations.isEmpty()) {
                    continue
                }

                val latestEvaluation = evaluations[0]
                val queryGroup = latestEvaluation.queryGroup ?: continue
                if(shouldSendNotification(evaluations)) {
                    var content = tryAssignNewContent(queryGroup, subject)

                    if(content == null) {
                        content = getRandomAlreadyAssignedContent(queryGroup, subject)
                    }
                    sendNotification(content!!)
                }
            }
        }

        return true;


    }

    companion object {
        private val log = LoggerFactory.getLogger(QueryContentService::class.java)
    }
}
