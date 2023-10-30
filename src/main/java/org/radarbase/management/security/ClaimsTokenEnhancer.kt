package org.radarbase.management.security

import org.radarbase.auth.authorization.AuthorizationOracle
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.jwt.JwtTokenVerifier
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.Source
import org.radarbase.management.domain.User
import org.radarbase.management.repository.SubjectRepository
import org.radarbase.management.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.TokenEnhancer
import java.security.Principal
import java.time.Instant
import java.util.*

class ClaimsTokenEnhancer(
    @Autowired private val subjectRepository: SubjectRepository,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val auditEventRepository: AuditEventRepository,
    @Autowired private val authorizationOracle: AuthorizationOracle
) : TokenEnhancer, InitializingBean {

    @Value("\${spring.application.name}")
    private val appName: String? = null
    override fun enhance(
        accessToken: OAuth2AccessToken, authentication: OAuth2Authentication
    ): OAuth2AccessToken {
        logger.debug("Enhancing token of authentication {}", authentication)
        val additionalInfo: MutableMap<String, Any?> = HashMap()
        val userName = authentication.getName()
        if (authentication.principal is Principal || authentication.principal is UserDetails) {
            // add the 'sub' claim in accordance with JWT spec
            additionalInfo["sub"] = userName
            userRepository.findOneByLogin(userName)?.let { user: User? ->
                val roles = user!!.roles!!.stream().map { role: Role ->
                    val auth = role.authority!!.name
                    when (role.role!!.scope) {
                        RoleAuthority.Scope.GLOBAL -> auth
                        RoleAuthority.Scope.ORGANIZATION -> (role.organization!!.name + ":" + auth)

                        RoleAuthority.Scope.PROJECT -> (role.project!!.projectName + ":" + auth)
                    }
                }.toList()
                additionalInfo[JwtTokenVerifier.ROLES_CLAIM] = roles

                // Do not grant scopes that cannot be given to a user.
                val currentScopes: MutableSet<String> = accessToken.scope
                val newScopes: Set<String> = currentScopes
                    .filter { scope: String ->
                        val permission: Permission = Permission.ofScope(scope)

                        val roleAuthorities = user
                            .roles!!.mapNotNull { it.role }
                            .let{ _ -> EnumSet.noneOf(RoleAuthority::class.java)}
                            .filterNotNull()

                        roleAuthorities.mayBeGranted(permission)
                    }
                    .toSet()

                if (newScopes != currentScopes) {
                    (accessToken as DefaultOAuth2AccessToken).scope = newScopes
                }
            }
            val assignedSources = subjectRepository.findSourcesBySubjectLogin(userName)
            val sourceIds = assignedSources.stream().map { s: Source? -> s!!.sourceId.toString() }.toList()
            additionalInfo[JwtTokenVerifier.SOURCES_CLAIM] = sourceIds
        }
        // add iat and iss optional JWT claims
        additionalInfo["iat"] = Instant.now().epochSecond
        additionalInfo["iss"] = appName
        additionalInfo[JwtTokenVerifier.GRANT_TYPE_CLAIM] = authentication.oAuth2Request.getGrantType()
        (accessToken as DefaultOAuth2AccessToken).additionalInformation = additionalInfo

        // HACK: since all granted tokens need to pass here, we can use this point to create an
        // audit event for a granted token, there is an open issue about oauth2 audit events in
        // spring security but it has been inactive for a long time:
        // https://github.com/spring-projects/spring-security-oauth/issues/223
        val auditData = auditData(accessToken, authentication)
        auditEventRepository.add(
            AuditEvent(
                userName, GRANT_TOKEN_EVENT, auditData
            )
        )
        logger.info("[{}] for {}: {}", GRANT_TOKEN_EVENT, userName, auditData)
        return accessToken
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        // nothing to do for now
    }

    private fun auditData(
        accessToken: OAuth2AccessToken, authentication: OAuth2Authentication
    ): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap()
        result["tokenType"] = accessToken.tokenType
        result["scope"] = java.lang.String.join(", ", accessToken.scope)
        result["expiresIn"] = accessToken.expiresIn.toString()
        result.putAll(accessToken.additionalInformation)
        val request = authentication.oAuth2Request
        result["clientId"] = request.clientId
        result["grantType"] = request.getGrantType()
        return result
    }

    open fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean = with(authorizationOracle) {
        role.mayBeGranted(permission)
    }

    fun Collection<RoleAuthority>.mayBeGranted(permission: Permission): Boolean = with(authorizationOracle) {
        return any { it.mayBeGranted(permission) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClaimsTokenEnhancer::class.java)
        private const val GRANT_TOKEN_EVENT = "GRANT_ACCESS_TOKEN"
    }
}

