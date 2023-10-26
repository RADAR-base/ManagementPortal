package org.radarbase.management.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.auth.authorization.EntityRelationService
import org.radarbase.auth.authorization.MPAuthorizationOracle
import org.radarbase.management.repository.ProjectRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AuthorizationConfiguration(
    private val projectRepository: ProjectRepository,
) {
    @Bean
    open fun authorizationOracle(): AuthorizationOracle = MPAuthorizationOracle(
        object : EntityRelationService {
            override suspend fun findOrganizationOfProject(project: String): String? = withContext(Dispatchers.IO) {
                projectRepository.findOneWithEagerRelationshipsByName(project)
                    ?.organization?.name
            }
        }
    )
}
