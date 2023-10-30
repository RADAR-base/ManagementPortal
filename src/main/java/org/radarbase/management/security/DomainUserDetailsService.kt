package org.radarbase.management.security

import org.radarbase.management.domain.Authority
import org.radarbase.management.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
class DomainUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    @Transactional
    override fun loadUserByUsername(login: String): UserDetails {
        log.debug("Authenticating {}", login)
        val lowercaseLogin = login.lowercase()
        val user = userRepository.findOneWithRolesByLogin(lowercaseLogin)
            ?: throw UsernameNotFoundException(
                    "User $lowercaseLogin was not found in the database"
                )
        if (!user.activated) {
            throw UserNotActivatedException(
                "User " + lowercaseLogin
                        + " was not activated"
            )
        }
        val grantedAuthorities =
            user.authorities!!.mapNotNull { authority: Authority? -> SimpleGrantedAuthority(authority?.name) }
        return User(
            lowercaseLogin,
            user.password,
            grantedAuthorities
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DomainUserDetailsService::class.java)
    }
}
