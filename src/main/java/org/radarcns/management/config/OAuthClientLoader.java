package org.radarcns.management.config;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.radarcns.auth.authorization.Permission;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by dverbeec on 20/11/2017.
 */
@Component
public class OAuthClientLoader {

    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private  ManagementPortalProperties managementPortalProperties;

    private static Logger logger = LoggerFactory.getLogger(OAuthClientLoader.class);

    private static final Character SEPARATOR = ';';

    /**
     * Build the ClientDetails for the ManagementPortal frontend and load it to the database
     */
    @EventListener(ContextRefreshedEvent.class)
    public void loadFrontendOauthClient() {
        ManagementPortalProperties.Frontend frontend = managementPortalProperties.getFrontend();
        BaseClientDetails details = new BaseClientDetails();
        details.setClientId(frontend.getClientId());
        details.setClientSecret(frontend.getClientSecret());
        details.setAccessTokenValiditySeconds(frontend.getAccessTokenValiditySeconds());
        details.setRefreshTokenValiditySeconds(frontend.getRefreshTokenValiditySeconds());
        details.setResourceIds(Arrays.asList("res_ManagementPortal"));
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

    @EventListener(ContextRefreshedEvent.class)
    public void loadOAuthClientsFromFile() {
        String path = managementPortalProperties.getOauth().getClientsFile();
        if (Objects.isNull(path) || path.equals("")) {
            logger.info("No OAuth clients file specified, not loading additional clients");
            return;
        }
        File file = new File(path);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            logger.info("Loading OAuth clients from " + file.getAbsolutePath());
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = mapper.schemaFor(BaseClientDetails.class)
                    // CsvSchema uses the @JsonPropertyOrder to define column order, however that
                    // annotation is not present on BaseClientDetails so we need to read the
                    // header and pass the column names to the schema manually in order for it to
                    // know the order
                    .withColumnReordering(true)
                    .sortedBy(getCsvFileColumnOrder(file))
                    .withColumnSeparator(SEPARATOR)
                    .withHeader();
            MappingIterator<BaseClientDetails> iterator = mapper.readerFor(BaseClientDetails.class)
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

    private String[] getCsvFileColumnOrder(File csvFile) {
        String[] header;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))) {
            return bufferedReader.readLine().split(SEPARATOR.toString());
        } catch (Exception ex) {
            logger.error("Unable to read header from OAuth clients file: " + ex.getMessage(), ex);
            return null;
        }
    }
}
