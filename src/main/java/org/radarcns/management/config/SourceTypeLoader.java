package org.radarcns.management.config;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.radarcns.management.web.rest.util.HttpUtil;
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

    private final Logger log = LoggerFactory.getLogger(SourceTypeLoader.class);

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

        String catalogServerUrl = managementPortalProperties.getCatalogueServer().getServerUrl();

        if (managementPortalProperties.getCatalogueServer().isEnableAutoImport()) {

            try {
                if (HttpUtil.isReachable(new URL(catalogServerUrl))) {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<SourceTypeResponse> catalogues = null;
                    log.debug("Requesting source-types from catalogue server...");
                    catalogues = restTemplate
                            .getForEntity(
                                    managementPortalProperties.getCatalogueServer().getServerUrl(),
                                    SourceTypeResponse.class);
                    SourceTypeResponse catalogueDto = catalogues.getBody();
                    List<CatalogSourceType> catalogSourceTypes = new ArrayList<>();
                    if (catalogueDto.getPassiveSources() != null) {
                        catalogSourceTypes.addAll(catalogueDto.getPassiveSources());
                    }
                    if (catalogueDto.getActiveSources() != null) {
                        catalogSourceTypes.addAll(catalogueDto.getActiveSources());
                    }
                    if (catalogueDto.getMonitorSources() != null) {
                        catalogSourceTypes.addAll(catalogueDto.getMonitorSources());
                    }

                    saveSourceTypesFromCatalogServer(catalogSourceTypes);

                } else {
                    log.warn("Catalog Service {} is unreachable: {}", catalogServerUrl);
                }
            } catch (MalformedURLException e) {
                log.warn("Invalid Url provided for Catalog server url {} : {}", catalogServerUrl,
                        e.getMessage());
            } catch (RuntimeException exe) {
                log.warn("An error has occurred during auto import of source-types. ", exe
                        .getMessage());
            }
        } else {
            log.info("Auto source-type import is disabled");
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

            if (sourceType.getProducer() == null) {
                log.warn("Catalog source-type {} does not have a vendor. "
                        + "Skipping importing this type", catalogSourceType.getName());
                continue;
            }

            if (sourceType.getModel() == null) {
                log.warn("Catalog source-type {} does not have a model. "
                        + "Skipping importing this type", catalogSourceType.getName());
                continue;
            }

            if (sourceType.getCatalogVersion() == null) {
                log.warn("Catalog source-type {} does not have a version. "
                        + "Skipping importing this type", catalogSourceType.getName());
                continue;
            }
            // check whether a source-type is already available with given config
            if (!sourceTypeRepository.findOneWithEagerRelationshipsByProducerAndModelAndVersion(
                    sourceType.getProducer(), sourceType.getModel(),
                    sourceType.getCatalogVersion()).isPresent()) {
                // create new source-type
                sourceTypeRepository.save(sourceType);

                // create source-data for the new source-type
                for (CatalogSourceData catalogSourceData : catalogSourceType.getData()) {
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
                }
            } else {
                // skip for existing source-types
                log.info("Source-type {} is already available ", sourceType.getProducer()
                        + "_" + sourceType.getModel()
                        + "_" + sourceType.getCatalogVersion());
            }
        }
        log.info("Completed source-type import from catalog-server");
    }
}
