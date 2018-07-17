package org.radarcns.management.service;

import org.radarcns.management.domain.Source;
import org.radarcns.management.domain.User;
import org.radarcns.management.service.dto.ClientDetailsDTO;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.management.service.dto.SourceTypeDTO;
import org.radarcns.management.service.dto.SubjectDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Consolidates the generation of location URI's for all of the resources.
 */
public class ResourceUriService {

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(SubjectDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "subjects", resource.getLogin()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(ClientDetailsDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "oauth-clients", resource.getClientId()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(MinimalSourceDetailsDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "sources", resource.getSourceName()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(RoleDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "roles", resource.getProjectName(),
                resource.getAuthorityName()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(SourceTypeDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "source-types", resource.getProducer(),
                resource.getModel(), resource.getCatalogVersion()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(SourceDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "sources", resource.getSourceName()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(Source resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "sources", resource.getSourceName()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(User resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "users", resource.getLogin()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(SourceDataDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "source-data", resource.getSourceDataName()));
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See {@link URI#URI(String)}
     */
    public static URI getUri(ProjectDTO resource) throws URISyntaxException {
        return new URI(HeaderUtil.buildPath("api", "projects", resource.getProjectName()));
    }
}
