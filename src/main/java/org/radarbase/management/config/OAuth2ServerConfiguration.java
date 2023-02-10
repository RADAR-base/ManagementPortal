package org.radarbase.management.config;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.management.repository.UserRepository;
import org.radarbase.management.security.ClaimsTokenEnhancer;
import org.radarbase.management.security.Http401UnauthorizedEntryPoint;
import org.radarbase.management.security.JwtAuthenticationFilter;
import org.radarbase.management.security.PostgresApprovalStore;
import org.radarbase.management.security.jwt.ManagementPortalJwtAccessTokenConverter;
import org.radarbase.management.security.jwt.ManagementPortalJwtTokenStore;
import org.radarbase.management.security.jwt.ManagementPortalOauthKeyStoreHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.springframework.orm.jpa.vendor.Database.POSTGRESQL;

@Configuration
public class OAuth2ServerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2ServerConfiguration.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Configuration
    @Order(-20)
    protected static class LoginConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ManagementPortalOauthKeyStoreHandler keyStoreHandler;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .formLogin().loginPage("/login").permitAll()
                    .and()
                    .addFilterBefore(jwtAuthenticationFilter(),
                            UsernamePasswordAuthenticationFilter.class)
                    .requestMatchers()
                        .antMatchers("/login", "/oauth/authorize", "/oauth/confirm_access")
                    .and()
                    .authorizeRequests().anyRequest().authenticated();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.parentAuthenticationManager(authenticationManager);
        }

        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(
                    keyStoreHandler.getTokenValidator(), authenticationManager, userRepository, true);
        }
    }

    @Bean
    public JdbcClientDetailsService jdbcClientDetailsService() {
        JdbcClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        clientDetailsService.setPasswordEncoder(passwordEncoder);
        return clientDetailsService;
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        @Autowired
        private ManagementPortalOauthKeyStoreHandler keyStoreHandler;

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint;

        @Autowired
        private LogoutSuccessHandler logoutSuccessHandler;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private UserRepository userRepository;

        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return new JwtAuthenticationFilter(
                    keyStoreHandler.getTokenValidator(), authenticationManager, userRepository
            )
                    .skipUrlPattern(HttpMethod.GET, "/management/health")
                    .skipUrlPattern(HttpMethod.GET, "/api/meta-token/*")
                    .skipUrlPattern(HttpMethod.GET, "/images/**")
                    .skipUrlPattern(HttpMethod.GET, "/css/**")
                    .skipUrlPattern(HttpMethod.GET, "/js/**");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .exceptionHandling()
                    .authenticationEntryPoint(http401UnauthorizedEntryPoint)
                    .and()
                    .logout()
                        .invalidateHttpSession(true)
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                    .and()
                    .csrf()
                        .disable()
                    .addFilterBefore(jwtAuthenticationFilter(),
                            UsernamePasswordAuthenticationFilter.class)
                    .headers()
                        .frameOptions()
                            .disable()
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .and()
                    .authorizeRequests()
                        .antMatchers("/oauth/**").permitAll()
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .antMatchers("/api/register")
                            .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                        .antMatchers("/api/profile-info").permitAll()
                        .antMatchers("/api/**").authenticated()
                        // Allow management/health endpoint to all to allow kubernetes to be able to
                        // detect the health of the service
                        .antMatchers("/management/health").permitAll()
                        .antMatchers("/management/**")
                            .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY)
                        .antMatchers("/v2/api-docs/**").permitAll()
                        .antMatchers("/swagger-resources/configuration/ui").permitAll()
                        .antMatchers("/swagger-ui/index.html")
                            .hasAnyAuthority(RoleAuthority.SYS_ADMIN_AUTHORITY);
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("res_ManagementPortal")
                    .tokenStore(tokenStore)
                    .eventPublisher(new CustomEventPublisher());
        }

        protected static class CustomEventPublisher extends DefaultAuthenticationEventPublisher {

            @Override
            public void publishAuthenticationSuccess(Authentication authentication) {
                // OAuth2AuthenticationProcessingFilter publishes an authentication success audit
                // event for EVERY successful OAuth request to our API resoruces, this is way too
                // much so we override the event publisher to not publish these events.

            }
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends
            AuthorizationServerConfigurerAdapter {

        @Autowired
        private JpaProperties jpaProperties;

        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private JdbcClientDetailsService jdbcClientDetailsService;

        @Autowired
        private ManagementPortalOauthKeyStoreHandler keyStoreHandler;

        @Bean
        protected AuthorizationCodeServices authorizationCodeServices() {
            return new JdbcAuthorizationCodeServices(dataSource);
        }

        @Bean
        public ApprovalStore approvalStore() {
            if (jpaProperties.getDatabase().equals(POSTGRESQL)) {
                return new PostgresApprovalStore(dataSource);
            } else {
                // to have compatibility for other databases including H2
                return new JdbcApprovalStore(dataSource);
            }
        }

        @Bean
        public TokenEnhancer tokenEnhancer() {
            return new ClaimsTokenEnhancer();
        }

        @Bean
        public TokenStore tokenStore() {
            return new ManagementPortalJwtTokenStore(accessTokenConverter());
        }

        @Bean
        public ManagementPortalJwtAccessTokenConverter accessTokenConverter() {
            logger.debug("loading token converter from keystore configurations");
            return new ManagementPortalJwtAccessTokenConverter(
                    keyStoreHandler.getAlgorithmForSigning(),
                    keyStoreHandler.getVerifiers(),
                    keyStoreHandler.getRefreshTokenVerifiers());
        }

        @Bean
        @Primary
        public DefaultTokenServices tokenServices(TokenStore tokenStore) {
            DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
            defaultTokenServices.setTokenStore(tokenStore);
            defaultTokenServices.setSupportRefreshToken(true);
            defaultTokenServices.setReuseRefreshToken(false);
            return defaultTokenServices;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
            tokenEnhancerChain.setTokenEnhancers(
                    Arrays.asList(tokenEnhancer(), accessTokenConverter()));

            endpoints
                    .authorizationCodeServices(authorizationCodeServices())
                    .approvalStore(approvalStore())
                    .tokenStore(tokenStore())
                    .tokenEnhancer(tokenEnhancerChain)
                    .reuseRefreshTokens(false)
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
            oauthServer.allowFormAuthenticationForClients()
                    .checkTokenAccess("isAuthenticated()")
                    .tokenKeyAccess("permitAll()")
                    .passwordEncoder(new BCryptPasswordEncoder());
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(jdbcClientDetailsService);
        }
    }
}
