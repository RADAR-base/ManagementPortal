package org.radarbase.management.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.radarbase.management.domain.SourceData;
import org.radarbase.management.domain.SourceType;
import org.radarbase.management.repository.SourceDataRepository;
import org.radarbase.management.repository.SourceTypeRepository;
import org.radarbase.management.service.catalog.CatalogSourceData;
import org.radarbase.management.service.catalog.CatalogSourceType;
import org.radarbase.management.service.catalog.SourceTypeResponse;
import org.radarbase.management.service.mapper.CatalogSourceDataMapper;
import org.radarbase.management.service.mapper.CatalogSourceTypeMapper;
import org.radarbase.management.web.rest.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private SourceTypeRepository sourceTypeRepository;

    @Autowired
    private SourceDataRepository sourceDataRepository;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private CatalogSourceTypeMapper catalogSourceTypeMapper;

    @Autowired
    private CatalogSourceDataMapper catalogSourceDataMapper;

    @Override
    public void run(String... args) {
        if (!managementPortalProperties.getCatalogueServer().isEnableAutoImport()) {
            log.info("Auto source-type import is disabled");
            return;
        }

        String catalogServerUrl = managementPortalProperties.getCatalogueServer().getServerUrl();

        try {
            if (HttpUtil.isReachable(new URL(catalogServerUrl))) {
                log.warn("Catalog Service {} is unreachable", catalogServerUrl);
                return;
            }
            RestTemplate restTemplate = new RestTemplate();
            log.debug("Requesting source-types from catalogue server...");
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
            saveSourceTypesFromCatalogServer(catalogSourceTypes);
        } catch (MalformedURLException e) {
            log.warn("Invalid Url provided for Catalog server url {} : {}", catalogServerUrl,
                    e.getMessage());
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

    /**
     * Converts given {@link CatalogSourceType} to {@link SourceType} and saves it to the databse
     * after validations.
     * @param catalogSourceTypes list of source-type from catalogue-server.
     */
    @Transactional
    public void saveSourceTypesFromCatalogServer(List<CatalogSourceType> catalogSourceTypes) {
        for (CatalogSourceType catalogSourceType : catalogSourceTypes) {
            SourceType sourceType = catalogSourceTypeMapper
                    .catalogSourceTypeToSourceType(catalogSourceType);

            if (!isSourceTypeValid(sourceType)) {
                continue;
            }

            // check whether a source-type is already available with given config
            if (sourceTypeRepository.hasOneByProducerAndModelAndVersion(
                    sourceType.getProducer(), sourceType.getModel(),
                    sourceType.getCatalogVersion())) {
                // skip for existing source-types
                log.info("Source-type {} is already available ", sourceType.getProducer()
                        + "_" + sourceType.getModel()
                        + "_" + sourceType.getCatalogVersion());
            } else {
                try {
                    // create new source-type
                    sourceType = sourceTypeRepository.save(sourceType);

                    // create source-data for the new source-type
                    for (CatalogSourceData catalogSourceData : catalogSourceType.getData()) {
                        saveSourceData(sourceType, catalogSourceData);
                    }
                } catch (RuntimeException ex) {
                    log.error("Failed to import source type {}", sourceType, ex);
                }
            }
        }
        log.info("Completed source-type import from catalog-server");
    }

    private void saveSourceData(SourceType sourceType, CatalogSourceData catalogSourceData) {
        try {
            SourceData sourceData = catalogSourceDataMapper
                    .catalogSourceDataToSourceData(catalogSourceData);
            // sourceDataName should be unique
            // generated by combining sourceDataType and source-type configs
            sourceData.sourceDataName(sourceType.getProducer()
                    + "_" + sourceType.getModel()
                    + "_" + sourceType.getCatalogVersion()
                    + "_" + sourceData.getSourceDataType());
            sourceData.sourceType(sourceType);
            sourceDataRepository.save(sourceData);
        } catch (RuntimeException ex) {
            log.error("Failed to import source data {}", catalogSourceData, ex);
        }
    }

    private static boolean isSourceTypeValid(SourceType sourceType) {
        if (sourceType.getProducer() == null) {
            log.warn("Catalog source-type {} does not have a vendor. "
                    + "Skipping importing this type", sourceType.getName());
            return false;
        }

        if (sourceType.getModel() == null) {
            log.warn("Catalog source-type {} does not have a model. "
                    + "Skipping importing this type", sourceType.getName());
            return false;
        }

        if (sourceType.getCatalogVersion() == null) {
            log.warn("Catalog source-type {} does not have a version. "
                    + "Skipping importing this type", sourceType.getName());
            return false;
        }
        return true;
    }
}
