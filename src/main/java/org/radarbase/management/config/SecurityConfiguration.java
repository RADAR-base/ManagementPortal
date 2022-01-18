package org.radarbase.management.config;


import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.security.Http401UnauthorizedEntryPoint;
import org.radarbase.management.security.RadarAuthenticationProvider;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import tech.jhipster.security.AjaxLogoutSuccessHandler;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.radarbase.management.security.JwtAuthenticationFilter.TOKEN_ATTRIBUTE;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final UserDetailsService userDetailsService;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfiguration(AuthenticationManagerBuilder authenticationManagerBuilder,
            UserDetailsService userDetailsService,
            ApplicationEventPublisher applicationEventPublisher,
            PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDetailsService = userDetailsService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        try {
            authenticationManagerBuilder
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(passwordEncoder)
                    .and()
                    .authenticationProvider(new RadarAuthenticationProvider())
                    .authenticationEventPublisher(
                            new DefaultAuthenticationEventPublisher(applicationEventPublisher));
        } catch (Exception e) {
            throw new BeanInitializationException("Security configuration failed", e);
        }
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new AjaxLogoutSuccessHandler();
    }

    @Bean
    public Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint() {
        return new Http401UnauthorizedEntryPoint();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/bower_components/**")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/**")
                .antMatchers("/api-docs/**")
                .antMatchers("/swagger-ui.html")
                .antMatchers("/api-docs{,.json,.yml}")
                .antMatchers("/api/register")
                .antMatchers("/api/profile-info")
                .antMatchers("/api/activate")
                .antMatchers("/api/account/reset_password/init")
                .antMatchers("/api/account/reset_password/finish")
                .antMatchers("/test/**")
                .antMatchers("/h2-console/**")
                .antMatchers("/management/health")
                .antMatchers(HttpMethod.GET, "/api/meta-token/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().realmName("ManagementPortal")
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Scope(value = "request", proxyMode = TARGET_CLASS)
    @Bean
    public RadarToken radarToken(HttpServletRequest request) {
        Object token = request.getAttribute(TOKEN_ATTRIBUTE);
        if (token == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                token = session.getAttribute(TOKEN_ATTRIBUTE);
            }
        }
        if (token == null) {
            // should not happen, the JwtAuthenticationFilter would throw an exception first if it
            // can not decode the authorization header into a valid JWT
            throw new AccessDeniedException("No token was found in the request context.");
        }
        if (!(token instanceof RadarToken)) {
            // should not happen, the JwtAuthenticationFilter will only set a DecodedJWT object
            throw new AccessDeniedException("Expected token to be of type org.radarbase"
                    + ".auth.token.RadarToken but was " + token.getClass().getName());
        }
        return (RadarToken) token;
    }
}
