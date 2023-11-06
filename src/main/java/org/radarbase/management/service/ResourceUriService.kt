package org.radarbase.management.service

import org.radarbase.management.domain.MetaToken
import org.radarbase.management.domain.Source
import org.radarbase.management.domain.User
import org.radarbase.management.service.dto.ClientDetailsDTO
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO
import org.radarbase.management.service.dto.OrganizationDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.SourceDTO
import org.radarbase.management.service.dto.SourceDataDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.web.rest.util.HeaderUtil
import java.net.URI
import java.net.URISyntaxException

/**
 * Consolidates the generation of location URI's for all of the resources.
 */
object ResourceUriService {
    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: OrganizationDTO): URI {
        return URI(HeaderUtil.buildPath("api", "organizations", resource.name))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: SubjectDTO): URI {
        return URI(resource.login?.let { HeaderUtil.buildPath("api", "subjects", it) })
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: ClientDetailsDTO): URI {
        return URI(HeaderUtil.buildPath("api", "oauth-clients", resource.clientId))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: MinimalSourceDetailsDTO): URI {
        return URI(HeaderUtil.buildPath("api", "sources", resource.sourceName!!))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: RoleDTO?): URI {
        return URI(
            HeaderUtil.buildPath(
                "api", "roles", resource?.projectName!!,
                resource.authorityName!!
            )
        )
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: SourceTypeDTO): URI {
        return URI(
            HeaderUtil.buildPath(
                "api", "source-types", resource.producer,
                resource.model, resource.catalogVersion
            )
        )
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: SourceDTO): URI {
        return URI(HeaderUtil.buildPath("api", "sources", resource.sourceName))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: Source): URI {
        return URI(HeaderUtil.buildPath("api", "sources", resource.sourceName!!))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: User): URI {
        return URI(HeaderUtil.buildPath("api", "users", resource.login))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: SourceDataDTO): URI {
        return URI(HeaderUtil.buildPath("api", "source-data", resource.sourceDataName!!))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: ProjectDTO): URI {
        return URI(HeaderUtil.buildPath("api", "projects", resource.projectName!!))
    }

    /**
     * Get the API location for the given resource.
     * @param resource the resource
     * @return the API location
     * @throws URISyntaxException See [URI.URI]
     */
    @Throws(URISyntaxException::class)
    fun getUri(resource: MetaToken): URI {
        return URI(HeaderUtil.buildPath("api", "meta-token", resource.tokenName!!))
    }
}
