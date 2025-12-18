package org.radarbase.management.service.config

import org.radarbase.management.config.annotations.IdentityServerEnabled
import org.radarbase.management.config.annotations.IdentityServerDisabled
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.DefaultUserService
import org.radarbase.management.service.KratosUserService
import org.radarbase.management.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.radarbase.management.repository.UserRepository
import org.radarbase.management.service.PasswordService
import org.radarbase.management.service.mapper.UserMapper
import org.radarbase.management.service.RevisionService
import org.radarbase.management.service.AuthService
import org.slf4j.LoggerFactory
import org.radarbase.management.service.MailService

@Configuration
class UserServiceConfiguration {

    @IdentityServerEnabled
    @Configuration
    class DefaultUserServiceConfiguration {
        @Bean
        @Primary
        fun defaultUserService(
            userRepository: UserRepository,
            passwordService: PasswordService,
            userMapper: UserMapper,
            revisionService: RevisionService,
            managementPortalProperties: ManagementPortalProperties,
            authService: AuthService,
            mailService: MailService
        ): UserService {
            log.info("Using internal user management")
            return DefaultUserService(
                userRepository,
                passwordService,
                userMapper,
                revisionService,
                managementPortalProperties,
                authService,
                mailService
            )
        }
    }

    @IdentityServerDisabled
    @Configuration
    class KratosUserServiceConfiguration {
        @Bean
        @Primary
        fun kratosUserService(
            userRepository: UserRepository,
            passwordService: PasswordService,
            userMapper: UserMapper,
            revisionService: RevisionService,
            managementPortalProperties: ManagementPortalProperties,
            authService: AuthService
        ): UserService {
            log.info("Using Kratos external user management")
            require(managementPortalProperties.identityServer.serverUrl.isNotBlank()) {
                "Kratos server URL must be configured when using external identity management"
            }
            return KratosUserService(
                userRepository,
                passwordService,
                userMapper,
                revisionService,
                managementPortalProperties,
                authService
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserServiceConfiguration::class.java)
    }
}