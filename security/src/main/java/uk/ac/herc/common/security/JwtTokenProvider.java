package uk.ac.herc.common.security;

import org.springframework.security.core.Authentication;

public interface JwtTokenProvider {
    String createToken(Authentication authentication, boolean rememberMe);
}
