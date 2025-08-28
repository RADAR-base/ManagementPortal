package org.radarbase.management.service.config

import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.DefaultUserService
import org.radarbase.management.service.KratosUserService
import org.radarbase.management.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.beans.factory.annotation.Autowired
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.PasswordService
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.service.RevisionService
import org.radarbase.management.service.AuthService
import org.slf4j.LoggerFactory
import org.radarbase.management.service.MailService

@Configuration
class UserServiceConfiguration {

    @Bean
    @Primary
    fun userService(
        userRepository: UserRepository,
        passwordService: PasswordService,
        userMapper: UserMapper,
        revisionService: RevisionService,
        managementPortalProperties: ManagementPortalProperties,
        authService: AuthService,
        mailService: MailService
    ): UserService {
        return if (managementPortalProperties.identityServer.internal) {
            log.info("Using internal user management")
            DefaultUserService(userRepository, passwordService, userMapper, 
                              revisionService, managementPortalProperties, authService, mailService)
        } else {
            log.info("Using Kratos external user management")
            // Could add validation here
            require(managementPortalProperties.identityServer.serverUrl.isNotBlank()) {
                "Kratos server URL must be configured when using external identity management"
            }
            KratosUserService(userRepository, passwordService, userMapper, 
                             revisionService, managementPortalProperties, authService)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceConfiguration::class.java)
    }
}