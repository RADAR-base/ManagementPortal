package org.radarbase.management.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.radarbase.auth.authorization.Permission;
import org.radarbase.management.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Component;

/**
 * Loads security configs such as oauth-clients, and overriding admin password if specified.
 * Created by dverbeec on 20/11/2017.
 */
@Component
public class ManagementPortalSecurityConfigLoader {

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private UserService userService;

    private static final Logger logger =
            LoggerFactory.getLogger(ManagementPortalSecurityConfigLoader.class);

    private static final Character SEPARATOR = ';';

    /**
     * Resets the admin password to the value of managementportal.common.adminPassword value if
     * exists.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void overrideAdminPassword() {
        String adminPassword = managementPortalProperties.getCommon().getAdminPassword();

        if (adminPassword != null && !adminPassword.isEmpty()) {
            logger.info("Overriding admin password to configured password");
            userService.changePassword("admin", adminPassword);
        } else {
            logger.info("AdminPassword property is empty. Using default password...");
        }
    }


    /**
     * Build the ClientDetails for the ManagementPortal frontend and load it to the database.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void loadFrontendOauthClient() {
        logger.info("Loading ManagementPortal frontend client");
        ManagementPortalProperties.Frontend frontend = managementPortalProperties.getFrontend();
        BaseClientDetails details = new BaseClientDetails();
        details.setClientId(frontend.getClientId());
        details.setClientSecret(null);
        details.setAccessTokenValiditySeconds(frontend.getAccessTokenValiditySeconds());
        details.setRefreshTokenValiditySeconds(frontend.getRefreshTokenValiditySeconds());
        details.setResourceIds(Collections.singletonList("res_ManagementPortal"));
        details.setAuthorizedGrantTypes(Arrays.asList("password", "refresh_token",
                "authorization_code"));
        details.setAdditionalInformation(Collections.singletonMap("protected", Boolean.TRUE));
        List<String> allScopes = Arrays.stream(Permission.Entity.values())
                .map(Permission.Entity::name)
                .flatMap(e -> Arrays.stream(Permission.Operation.values())
                        .map(o -> String.join(".", e, o.name())))
                .collect(Collectors.toList());
        details.setScope(allScopes);
        details.setAutoApproveScopes(allScopes);
        loadOAuthClient(details);
    }

    /**
     * Event listener method that loads OAuth clients from file as soon as the application
     * context is refreshed. This happens at least once, on application startup.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void loadOAuthClientsFromFile() {
        String path = managementPortalProperties.getOauth().getClientsFile();
        if (Objects.isNull(path) || path.equals("")) {
            logger.info("No OAuth clients file specified, not loading additional clients");
            return;
        }
        Path file = Paths.get(path);
        try (InputStream inputStream = Files.newInputStream(file)) {
            logger.info("Loading OAuth clients from {}", file.toAbsolutePath());
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(CustomBaseClientDetails.class)
                    // CsvSchema uses the @JsonPropertyOrder to define column order, it does not
                    // read the header. Let's read the header ourselves and provide that as
                    // column order
                    .withColumnReordering(true)
                    .sortedBy(getCsvFileColumnOrder(file))
                    .withColumnSeparator(SEPARATOR)
                    .withHeader();
            MappingIterator<BaseClientDetails> iterator = mapper
                    .readerFor(CustomBaseClientDetails.class)
                    .with(schema).readValues(inputStream);
            while (iterator.hasNext()) {
                loadOAuthClient(iterator.nextValue());
            }
        } catch (Exception ex) {
            logger.error("Unable to load OAuth clients from file: " + ex.getMessage(), ex);
        }
    }

    private void loadOAuthClient(ClientDetails details) {
        try {
            ClientDetails client = clientDetailsService.loadClientByClientId(details.getClientId());
            // we delete the existing client and reload it in the next try block
            clientDetailsService.removeClientDetails(client.getClientId());
            logger.info("Removed existing OAuth client: " + details.getClientId());
        } catch (NoSuchClientException ex) {
            // the client is not in the databse yet, this is ok
        } catch (Exception ex) {
            // other error, e.g. database issue
            logger.error(ex.getMessage(), ex);
        }
        try {
            clientDetailsService.addClientDetails(details);
            logger.info("OAuth client loaded: " + details.getClientId());
        } catch (Exception ex) {
            logger.error("Unable to load OAuth client " + details.getClientId() + ": "
                    + ex.getMessage(), ex);
        }
    }

    private String[] getCsvFileColumnOrder(Path csvFile) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(csvFile)) {
            return bufferedReader.readLine().split(SEPARATOR.toString());
        } catch (Exception ex) {
            logger.error("Unable to read header from OAuth clients file: " + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Custom class that will also deserialize the additional_information field. This field holds a
     * JSON structure that needs to be converted to a {@code Map<String, Object>}. This field is
     * {@link com.fasterxml.jackson.annotation.JsonIgnore}d in BaseClientDetails but we need it.
     */
    private static class CustomBaseClientDetails extends BaseClientDetails {

        @JsonProperty("additional_information")
        private Map<String, Object> additionalInformation = new LinkedHashMap<>();

        @Override
        public Map<String, Object> getAdditionalInformation() {
            return additionalInformation;
        }

        @JsonSetter("additional_information")
        public void setAdditionalInformation(String additionalInformation) {
            if (Objects.isNull(additionalInformation) || additionalInformation.equals("")) {
                this.additionalInformation = Collections.emptyMap();
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.additionalInformation = mapper.readValue(additionalInformation,
                        new TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception ex) {
                logger.error("Unable to parse additional_information field for client "
                        + getClientId() + ": " + ex.getMessage(), ex);
            }
        }
    }
}
