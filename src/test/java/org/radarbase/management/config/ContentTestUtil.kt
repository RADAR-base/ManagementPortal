package org.radarbase.management.config

import org.radarbase.management.domain.*
import org.radarbase.management.domain.enumeration.ContentType
import java.time.ZonedDateTime

object ContentTestUtil {


    fun addContentGroup(groupName: String, queryGroup: QueryGroup) : QueryContentGroup  {
        val contentGroup = QueryContentGroup()

        contentGroup.queryGroup = queryGroup
        contentGroup.contentGroupName = "Content Group Name"
        contentGroup.createdDate = ZonedDateTime.now()
        contentGroup.updatedDate = ZonedDateTime.now()

        return contentGroup
    }

    fun addQueryParticipantContent(queryGroup: QueryGroup, subject: Subject, contentGroup: QueryContentGroup) : QueryParticipantContent {
        val participantContent = QueryParticipantContent()
        participantContent.queryGroup = queryGroup
        participantContent.queryContentGroup = contentGroup
        participantContent.subject = subject
        participantContent.createdDate = ZonedDateTime.now()
        participantContent.isArchived = false;

        return participantContent
    }


    fun addContentItem(value: String, heading: String, type: ContentType, queryGroup: QueryGroup, contentGroup: QueryContentGroup) : QueryContent {
        val content =  QueryContent()
        content.queryGroup = queryGroup
        content.queryContentGroup = contentGroup
        content.value = "value"
        content.type = ContentType.PARAGRAPH
        content.heading = "heading1"

        return content;

    }


}
