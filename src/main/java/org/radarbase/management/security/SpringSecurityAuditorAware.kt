package org.radarbase.management.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Autowired
    private Optional<Authentication> authentication;

    @Override
    @Nonnull
    public Optional<String> getCurrentAuditor() {
        return authentication.map(Authentication::getName)
                .filter(n -> !n.isEmpty());
    }
}
