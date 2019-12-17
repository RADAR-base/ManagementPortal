package org.radarcns.management.config;

import static org.springframework.orm.jpa.vendor.Database.POSTGRESQL;

import java.util.Arrays;
import javax.sql.DataSource;

import io.github.jhipster.security.AjaxLogoutSuccessHandler;
import io.github.jhipster.security.Http401UnauthorizedEntryPoint;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.security.ClaimsTokenEnhancer;
import org.radarcns.management.security.PostgresApprovalStore;
import org.radarcns.management.security.jwt.ManagementPortalJwtAccessTokenConverter;
import org.radarcns.management.security.jwt.ManagementPortalJwtTokenStore;
import org.radarcns.management.security.jwt.ManagementPortalOauthKeyStoreHandler;
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
import org.springframework.web.filter.CorsFilter;

@Configuration
public class OAuth2ServerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2ServerConfiguration.class);

    @Autowired
    private DataSource dataSource;

    @Configuration
    @Order(-20)
    protected static class LoginConfig extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .formLogin().loginPage("/login").permitAll()
                    .and()
                    .requestMatchers().antMatchers("/login",
                    "/oauth/authorize",
                    "/oauth/confirm_access")
                    .and()
                    .authorizeRequests().anyRequest().authenticated();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.parentAuthenticationManager(authenticationManager);
        }
    }

    @Bean
    public JdbcClientDetailsService jdbcClientDetailsService() {
        JdbcClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        clientDetailsService.setPasswordEncoder(new BCryptPasswordEncoder());
        return clientDetailsService;
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private Http401UnauthorizedEntryPoint http401UnauthorizedEntryPoint;

        @Autowired
        private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

        @Autowired
        private CorsFilter corsFilter;

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .exceptionHandling()
                    .authenticationEntryPoint(http401UnauthorizedEntryPoint)
                    .and()
                    .logout()
                    .logoutUrl("/api/logout")
                    .logoutSuccessHandler(ajaxLogoutSuccessHandler)
                    .and()
                    .csrf()
                    .disable()
                    .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                    .headers()
                    .frameOptions().disable()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers("/api/authenticate").permitAll()
                    .antMatchers("/api/register").hasAnyAuthority(AuthoritiesConstants.SYS_ADMIN)
                    .antMatchers("/api/profile-info").permitAll()
                    .antMatchers("/api/**").authenticated()
                    // Allow management/health endpoint to all to allow kubernetes to be able to
                    // detect the health of the service
                    .antMatchers("/management/health").permitAll()
                    .antMatchers("/management/**").hasAnyAuthority(AuthoritiesConstants.SYS_ADMIN)
                    .antMatchers("/v2/api-docs/**").permitAll()
                    .antMatchers("/swagger-resources/configuration/ui").permitAll()
                    .antMatchers("/swagger-ui/index.html")
                    .hasAnyAuthority(AuthoritiesConstants.SYS_ADMIN);
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
                    keyStoreHandler.getVerifiers());
        }

        @Bean
        @Primary
        public DefaultTokenServices tokenServices() {
            DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
            defaultTokenServices.setTokenStore(tokenStore());
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
