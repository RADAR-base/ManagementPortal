package org.radarbase.management.config;

import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.session.hazelcast.Hazelcast4PrincipalNameExtractor;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.config.JHipsterProperties;

import javax.annotation.PreDestroy;
import java.util.List;

import static com.hazelcast.config.MaxSizePolicy.USED_HEAP_SIZE;

@Configuration
@EnableCaching
@AutoConfigureBefore(value = {WebConfigurer.class, DatabaseConfiguration.class})
public class CacheConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

    @Autowired
    private Environment env;

    @PreDestroy
    public void destroy() {
        log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        log.debug("Starting HazelcastCacheManager");
        return new HazelcastCacheManager(
                hazelcastInstance);
    }

    @Bean
    public Config hazelcastConfig(JHipsterProperties jHipsterProperties) {
        Config config = new Config();
        config.setInstanceName("ManagementPortal");
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);

        // In development, remove multicast auto-configuration
        if (env.acceptsProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)) {
            networkConfig.getInterfaces().setEnabled(true);
            networkConfig.getInterfaces().setInterfaces(List.of("127.0.0.1"));

            networkConfig.getJoin().getAwsConfig().setEnabled(false);
            networkConfig.getJoin().getMulticastConfig().setEnabled(false);
            networkConfig.getJoin().getTcpIpConfig().setEnabled(false);
            networkConfig.getJoin().getAzureConfig().setEnabled(false);
            networkConfig.getJoin().getGcpConfig().setEnabled(false);
            networkConfig.getJoin().getEurekaConfig().setEnabled(false);
            networkConfig.getJoin().getKubernetesConfig().setEnabled(false);
        }
        AttributeConfig attributeConfig = new AttributeConfig()
                .setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
                .setExtractorClassName(Hazelcast4PrincipalNameExtractor.class.getName());
        config.getMapConfig(Hazelcast4IndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
                .addAttributeConfig(attributeConfig).addIndexConfig(
                        new IndexConfig(IndexType.HASH,
                                Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));
        config.getMapConfigs().put("default", initializeDefaultMapConfig());
        config.getMapConfigs().put("org.radarbase.management.domain.*",
                initializeDomainMapConfig(jHipsterProperties));
        return config;
    }

    private MapConfig initializeDefaultMapConfig() {
        MapConfig mapConfig = new MapConfig();

        /* Number of backups. If 1 is set as the backup-count for example,
           then all entries of the map will be copied to another JVM for
           fail-safety. Valid numbers are 0 (no backup), 1, 2, 3. */
        mapConfig.setBackupCount(0);

        /* Valid values are:
           NONE (no eviction),
           LRU (Least Recently Used),
           LFU (Least Frequently Used).
           NONE is the default. */
        mapConfig.getEvictionConfig().setEvictionPolicy(EvictionPolicy.LRU);

        /* Maximum size of the map. When max size is reached,
           map is evicted based on the policy defined.
           Any integer between 0 and Integer.MAX_VALUE. 0 means
           Integer.MAX_VALUE. Default is 0. */
        mapConfig.getEvictionConfig().setMaxSizePolicy(USED_HEAP_SIZE);

        return mapConfig;
    }

    private MapConfig initializeDomainMapConfig(JHipsterProperties jHipsterProperties) {
        MapConfig mapConfig = new MapConfig();
        mapConfig.setTimeToLiveSeconds(
                jHipsterProperties.getCache().getHazelcast().getTimeToLiveSeconds());
        return mapConfig;
    }
}
