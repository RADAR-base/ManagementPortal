package org.radarcns.management.repository.search;

import org.radarcns.management.domain.Source;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Source entity.
 */
public interface SourceSearchRepository extends ElasticsearchRepository<Source, Long> {
}
