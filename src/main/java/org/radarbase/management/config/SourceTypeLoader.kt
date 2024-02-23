package org.radarbase.management.config

import org.radarbase.management.service.SourceTypeService
import org.radarbase.management.service.catalog.CatalogSourceType
import org.radarbase.management.service.catalog.SourceTypeResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

/**
 * Upon start of Spring application, this class automatically import the source-types provided by
 * Catalog server in Radar-Schemas. This will be executed when a valid URL of the catalog server is
 * provided and enableAutoImport is set to true.
 */
@Component
class SourceTypeLoader : CommandLineRunner {
    @Autowired
    private val sourceTypeService: SourceTypeService? = null

    @Autowired
    private val managementPortalProperties: ManagementPortalProperties? = null
    override fun run(vararg args: String) {
        if (!managementPortalProperties!!.catalogueServer.isEnableAutoImport) {
            log.info("Auto source-type import is disabled")
            return
        }
        val catalogServerUrl = managementPortalProperties.catalogueServer.serverUrl
        try {
            val restTemplate = RestTemplate()
            log.debug("Requesting source-types from catalog server...")
            val catalogues = restTemplate
                .getForEntity(catalogServerUrl, SourceTypeResponse::class.java)
            val catalogueDto = catalogues.body
            if (catalogueDto == null) {
                log.warn("Catalog Service {} returned empty response", catalogServerUrl)
                return
            }
            val catalogSourceTypes: MutableList<CatalogSourceType> = ArrayList()
            addNonNull(catalogSourceTypes, catalogueDto.passiveSources)
            addNonNull(catalogSourceTypes, catalogueDto.activeSources)
            addNonNull(catalogSourceTypes, catalogueDto.monitorSources)
            addNonNull(catalogSourceTypes, catalogueDto.connectorSources)
            sourceTypeService!!.saveSourceTypesFromCatalogServer(catalogSourceTypes)
        } catch (e: RestClientException) {
            log.warn(
                "Cannot fetch source types from Catalog Service at {}: {}", catalogServerUrl,
                e.toString()
            )
        } catch (exe: RuntimeException) {
            log.warn(
                "An error has occurred during auto import of source-types: {}", exe
                    .message
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SourceTypeLoader::class.java)
        private fun <T> addNonNull(collection: MutableCollection<T>, toAdd: Collection<T>?) {
            if (toAdd != null && !toAdd.isEmpty()) {
                collection.addAll(toAdd)
            }
        }
    }
}
