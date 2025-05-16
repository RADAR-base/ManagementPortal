package org.radarbase.management.service

import org.radarbase.management.repository.QueryContentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
public class QueryContentService(
    private val queryContentRepository: QueryContentRepository

) {


    public fun save() {

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

}
