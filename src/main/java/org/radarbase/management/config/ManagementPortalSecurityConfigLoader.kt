package org.radarbase.management.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import kotlinx.coroutines.runBlocking
import org.radarbase.auth.authorization.Permission.Companion.scopes
import org.radarbase.management.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.NoSuchClientException
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.transaction.Transactional

/**
 * Loads security configs such as oauth-clients, and overriding admin password if specified.
 * Created by dverbeec on 20/11/2017.
 */
@Component
class ManagementPortalSecurityConfigLoader(
    private val jdbcClientDetailsService: JdbcClientDetailsService? = null,
    private val managementPortalProperties: ManagementPortalProperties? = null,
    private val userService: UserService? = null
) {

    private var isAdminIdCreated: Boolean = false

    /**
     * Resets the admin password to the value of managementportal.common.adminPassword value if
     * exists.
     */
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun createAdminIdentity() {
        try {
            if (!isAdminIdCreated) {
                if (managementPortalProperties == null) {
                    logger.warn("ManagementPortalProperties is null, cannot create admin identity")
                    return
                }

                if (userService == null) {
                    logger.warn("UserService is null, cannot create admin identity")
                    return
                }

                val adminEmail = managementPortalProperties.identityServer?.adminEmail
                if (adminEmail.isNullOrBlank()) {
                    logger.warn("AdminEmail property is empty, cannot create admin identity")
                    return
                }

                val adminPassword = managementPortalProperties.common?.adminPassword
                if (adminPassword.isNullOrBlank()) {
                    logger.warn("AdminPassword property is empty, cannot create admin identity")
                    return
                }

                logger.info("Overriding admin email to $adminEmail")
                runBlocking {
                    userService.addAdminEmail(adminEmail).let {
                        userService.updateUser(it)
                    }
                    userService.addAdminPassword(adminPassword)
                }

                isAdminIdCreated = true
            }
        }
        catch (e: Throwable){
            logger.error("could not update/create admin identity. This may result in an unstable state", e)
        }
    }

    /**
     * Build the ClientDetails for the ManagementPortal frontend and load it to the database.
     */
    @EventListener(ContextRefreshedEvent::class)
    fun loadFrontendOauthClient() {
        logger.info("Loading ManagementPortal frontend client")
        if (managementPortalProperties == null) {
            logger.warn("Unable to load frontend OAuth client: managementPortalProperties is null")
            return
        }

        val frontend = managementPortalProperties.frontend
        val details = BaseClientDetails()
        details.clientId = frontend.clientId
        details.clientSecret = frontend.clientSecret
        details.accessTokenValiditySeconds = frontend.accessTokenValiditySeconds
        details.refreshTokenValiditySeconds = frontend.refreshTokenValiditySeconds
        details.setResourceIds(
            listOf(
                "res_ManagementPortal", "res_appconfig", "res_upload",
                "res_restAuthorizer"
            )
        )
        details.setAuthorizedGrantTypes(
            mutableListOf(
                "password", "refresh_token",
                "authorization_code"
            )
        )
        details.setRegisteredRedirectUri(
            setOf(
                managementPortalProperties.common.managementPortalBaseUrl + "/api/redirect/login",
                managementPortalProperties.common.managementPortalBaseUrl + "/api/redirect/account"
            )
        )
        details.setAdditionalInformation(Collections.singletonMap("protected", true))
        val allScopes = listOf(*scopes())
        details.setScope(allScopes)
        details.setAutoApproveScopes(allScopes)
        loadOAuthClient(details)
    }

    /**
     * Event listener method that loads OAuth clients from file as soon as the application
     * context is refreshed. This happens at least once, on application startup.
     */
    @EventListener(ContextRefreshedEvent::class)
    fun loadOAuthClientsFromFile() {
        logger.info("Loading additional ManagementPortal clients")
        if (managementPortalProperties == null) {
            logger.warn("ManagementPortalProperties is null, not loading additional clients")
            return
        }

        val path = managementPortalProperties.oauth?.clientsFile
        if (Objects.isNull(path) || path == "") {
            logger.info("No OAuth clients file specified, not loading additional clients")
            return
        }
        val file = Paths.get(path)
        // CsvSchema uses the @JsonPropertyOrder to define column order, it does not
        // read the header. Let's read the header ourselves and provide that as
        // column order
        val columnOrder = getCsvFileColumnOrder(file) ?: return
        val mapper = CsvMapper()
        val schema = mapper.schemaFor(CustomBaseClientDetails::class.java)
            .withColumnReordering(true)
            .sortedBy(*columnOrder)
            .withColumnSeparator(SEPARATOR)
            .withHeader()
        val reader = mapper
            .readerFor(CustomBaseClientDetails::class.java)
            .with(schema)
        try {
            Files.newInputStream(file).use { inputStream ->
                reader.readValues<BaseClientDetails>(inputStream).use { iterator ->
                    logger.info("Loading OAuth clients from {}", file.toAbsolutePath())
                    while (iterator.hasNext()) {
                        loadOAuthClient(iterator.nextValue())
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Unable to load OAuth clients from file: " + ex.message, ex)
        }
    }

    private fun loadOAuthClient(details: ClientDetails) {
        logger.info("Loading ManagementPortal OAuth client")
        if (jdbcClientDetailsService == null) {
            logger.warn("JdbcClientDetailsService is null, not loading OAuth client")
            return
        }
        try {
            val client = jdbcClientDetailsService.loadClientByClientId(details.clientId)
            // we delete the existing client and reload it in the next try block
            jdbcClientDetailsService.removeClientDetails(client.clientId)
            logger.info("Removed existing OAuth client: " + details.clientId)
        } catch (ex: NoSuchClientException) {
            // the client is not in the databse yet, this is ok
        } catch (ex: Exception) {
            // other error, e.g. database issue
            logger.error(ex.message, ex)
        }
        try {
            jdbcClientDetailsService.addClientDetails(details)
            logger.info("OAuth client loaded: " + details.clientId)
        } catch (ex: Exception) {
            logger.error(
                "Unable to load OAuth client " + details.clientId + ": "
                        + ex.message, ex
            )
        }
    }

    private fun getCsvFileColumnOrder(csvFile: Path): Array<String>? {
        try {
            Files.newBufferedReader(csvFile).use { bufferedReader ->
                return bufferedReader.readLine().split(SEPARATOR.toString().toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            }
        } catch (ex: Exception) {
            logger.error("Unable to read header from OAuth clients file: " + ex.message, ex)
            return null
        }
    }

    /**
     * Custom class that will also deserialize the additional_information field. This field holds a
     * JSON structure that needs to be converted to a `Map<String, Object>`. This field is
     * [com.fasterxml.jackson.annotation.JsonIgnore]d in BaseClientDetails but we need it.
     */
    private class CustomBaseClientDetails : BaseClientDetails() {
        @JsonProperty("additional_information")
        private var additionalInformation: Map<String, Any> = LinkedHashMap()
        override fun getAdditionalInformation(): Map<String, Any> {
            return additionalInformation
        }

        @JsonSetter("additional_information")
        fun setAdditionalInformation(additionalInformation: String) {
            if (Objects.isNull(additionalInformation) || additionalInformation == "") {
                this.additionalInformation = emptyMap()
                return
            }
            val mapper = ObjectMapper()
            try {
                this.additionalInformation = mapper.readValue<Map<String, Any>>(additionalInformation,
                    object : TypeReference<Map<String, Any>>() {})
            } catch (ex: Exception) {
                logger.error(
                    "Unable to parse additional_information field for client "
                            + clientId + ": " + ex.message, ex
                )
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManagementPortalSecurityConfigLoader::class.java)
        private const val SEPARATOR = ';'
    }
}
