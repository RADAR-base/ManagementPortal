package org.radarcns.management.config;

import java.util.ArrayList;
import java.util.List;
import org.radarcns.management.domain.SourceData;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.repository.SourceDataRepository;
import org.radarcns.management.repository.SourceTypeRepository;
import org.radarcns.management.service.catalog.CatalogSourceData;
import org.radarcns.management.service.catalog.CatalogSourceType;
import org.radarcns.management.service.catalog.SourceTypeResponse;
import org.radarcns.management.service.mapper.CatalogSourceDataMapper;
import org.radarcns.management.service.mapper.CatalogSourceTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SourceTypeLoader implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(SourceTypeLoader.class);

    @Autowired
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private SourceDataRepository sourceDataRepository;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private CatalogSourceTypeMapper catalogSourceTypeMapper;

    @Autowired
    private CatalogSourceDataMapper catalogSourceDataMapper;

    public void run(String... args) {
        log.debug("Requesting source-types from catalogue server...");

        if(managementPortalProperties.getCatalogueServer().isEnableAutoImport()) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<SourceTypeResponse> catalogues = null;
            catalogues = restTemplate
                .getForEntity(managementPortalProperties.getCatalogueServer().getServerUrl(),
                    SourceTypeResponse.class);
            SourceTypeResponse catalogueDTO = catalogues.getBody();
            List<CatalogSourceType> catalogSourceTypes = new ArrayList<>();
            if (catalogueDTO.getPassiveSources() != null) {
                catalogSourceTypes.addAll(catalogueDTO.getPassiveSources());
            }
            if (catalogueDTO.getActiveSources() != null) {
                catalogSourceTypes.addAll(catalogueDTO.getActiveSources());
            }
            if (catalogueDTO.getMonitorSources() != null) {
                catalogSourceTypes.addAll(catalogueDTO.getMonitorSources());
            }

            for (CatalogSourceType catalogSourceType : catalogSourceTypes) {
                SourceType sourceType = catalogSourceTypeMapper
                    .catalogSourceTypeToSourceType(catalogSourceType);
                sourceTypeRepository.save(sourceType);

                for (CatalogSourceData catalogSourceData : catalogSourceType.getData()) {
                    SourceData sourceData = catalogSourceDataMapper
                        .catalogSourceDataToSourceData(catalogSourceData);
                    sourceData.sourceDataName(
                        sourceType.getProducer() + "_" + sourceType.getModel() + "_" + sourceType
                            .getCatalogVersion() + "_" + sourceData.getSourceDataType());
                    sourceData.sourceType(sourceType);
                    sourceDataRepository.save(sourceData);
                }
            }

            log.info("Completed source-type import from catalog-server");
        }
        else {
            log.info("Auto source-type import is disabled");
        }
    }
}
