package org.radarbase.management.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.radarbase.management.service.SourceTypeService;
import org.radarbase.management.service.catalog.CatalogSourceType;
import org.radarbase.management.service.catalog.SourceTypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Upon start of Spring application, this class automatically import the source-types provided by
 * Catalog server in Radar-Schemas. This will be executed when a valid URL of the catalog server is
 * provided and enableAutoImport is set to true.
 */
@Component
public class SourceTypeLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SourceTypeLoader.class);

    @Autowired
    private SourceTypeService sourceTypeService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Override
    public void run(String... args) {
        if (!managementPortalProperties.getCatalogueServer().isEnableAutoImport()) {
            log.info("Auto source-type import is disabled");
            return;
        }

        String catalogServerUrl = managementPortalProperties.getCatalogueServer().getServerUrl();

        try {
            RestTemplate restTemplate = new RestTemplate();
            log.debug("Requesting source-types from catalog server...");
            ResponseEntity<SourceTypeResponse> catalogues = restTemplate
                    .getForEntity(catalogServerUrl, SourceTypeResponse.class);
            SourceTypeResponse catalogueDto = catalogues.getBody();
            if (catalogueDto == null) {
                log.warn("Catalog Service {} returned empty response", catalogServerUrl);
                return;
            }
            List<CatalogSourceType> catalogSourceTypes = new ArrayList<>();
            addNonNull(catalogSourceTypes, catalogueDto.getPassiveSources());
            addNonNull(catalogSourceTypes, catalogueDto.getActiveSources());
            addNonNull(catalogSourceTypes, catalogueDto.getMonitorSources());
            addNonNull(catalogSourceTypes, catalogueDto.getConnectorSources());
            sourceTypeService.saveSourceTypesFromCatalogServer(catalogSourceTypes);
        } catch (RestClientException e) {
            log.warn("Cannot fetch source types from Catalog Service at {}: {}", catalogServerUrl,
                    e.toString());
        } catch (RuntimeException exe) {
            log.warn("An error has occurred during auto import of source-types: {}", exe
                    .getMessage());
        }
    }

    private static <T> void addNonNull(Collection<T> collection, Collection<? extends T> toAdd) {
        if (toAdd != null && !toAdd.isEmpty()) {
            collection.addAll(toAdd);
        }
    }
}
