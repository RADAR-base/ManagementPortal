package org.radarbase.management.config

import com.hazelcast.config.AttributeConfig
import com.hazelcast.config.Config
import com.hazelcast.config.EvictionPolicy
import com.hazelcast.config.IndexConfig
import com.hazelcast.config.IndexType
import com.hazelcast.config.MapConfig
import com.hazelcast.config.MaxSizePolicy
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor
import tech.jhipster.config.JHipsterConstants
import tech.jhipster.config.JHipsterProperties
import javax.annotation.PreDestroy

@Configuration
@EnableCaching
@AutoConfigureBefore(value = [WebConfigurer::class, DatabaseConfiguration::class])
open class CacheConfiguration {
    @Autowired
    private val env: Environment? = null
    @PreDestroy
    fun destroy() {
        log.info("Closing Cache Manager")
        Hazelcast.shutdownAll()
    }

    @Bean
    open fun HazelcastInstance?.cacheManager(): CacheManager {
        log.debug("Starting HazelcastCacheManager")
        return HazelcastCacheManager(
            this
        )
    }

    @Bean
    open fun hazelcastConfig(jHipsterProperties: JHipsterProperties): Config {
        val config = Config()
        config.setInstanceName("ManagementPortal")
        val networkConfig = config.networkConfig
        networkConfig.setPort(5701)
        networkConfig.setPortAutoIncrement(true)

        // In development, remove multicast auto-configuration
        if (env!!.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)) {
            networkConfig.interfaces.setEnabled(true)
            networkConfig.interfaces.setInterfaces(listOf("127.0.0.1"))
            networkConfig.join.awsConfig.setEnabled(false)
            networkConfig.join.multicastConfig.setEnabled(false)
            networkConfig.join.tcpIpConfig.setEnabled(false)
            networkConfig.join.azureConfig.setEnabled(false)
            networkConfig.join.gcpConfig.setEnabled(false)
            networkConfig.join.eurekaConfig.setEnabled(false)
            networkConfig.join.kubernetesConfig.setEnabled(false)
        }
        val attributeConfig = AttributeConfig()
            .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
            .setExtractorClassName(Hazelcast4PrincipalNameExtractor::class.java.getName())
        config.getMapConfig(Hazelcast4IndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
            .addAttributeConfig(attributeConfig).addIndexConfig(
                IndexConfig(
                    IndexType.HASH,
                    Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE
                )
            )
        config.mapConfigs["default"] = initializeDefaultMapConfig()
        config.mapConfigs["org.radarbase.management.domain.*"] = initializeDomainMapConfig(jHipsterProperties)
        return config
    }

    private fun initializeDefaultMapConfig(): MapConfig {
        val mapConfig = MapConfig()

        /* Number of backups. If 1 is set as the backup-count for example,
           then all entries of the map will be copied to another JVM for
           fail-safety. Valid numbers are 0 (no backup), 1, 2, 3. */mapConfig.setBackupCount(0)

        /* Valid values are:
           NONE (no eviction),
           LRU (Least Recently Used),
           LFU (Least Frequently Used).
           NONE is the default. */mapConfig.evictionConfig.setEvictionPolicy(EvictionPolicy.LRU)

        /* Maximum size of the map. When max size is reached,
           map is evicted based on the policy defined.
           Any integer between 0 and Integer.MAX_VALUE. 0 means
           Integer.MAX_VALUE. Default is 0. */mapConfig.evictionConfig.setMaxSizePolicy(MaxSizePolicy.USED_HEAP_SIZE)
        return mapConfig
    }

    private fun initializeDomainMapConfig(jHipsterProperties: JHipsterProperties): MapConfig {
        val mapConfig = MapConfig()
        mapConfig.setTimeToLiveSeconds(
            jHipsterProperties.cache.hazelcast.timeToLiveSeconds
        )
        return mapConfig
    }

    companion object {
        private val log = LoggerFactory.getLogger(CacheConfiguration::class.java)
    }
}
