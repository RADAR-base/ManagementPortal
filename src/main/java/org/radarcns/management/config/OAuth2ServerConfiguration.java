package org.radarcns.management.config;

import io.github.jhipster.security.AjaxLogoutSuccessHandler;
import io.github.jhipster.security.Http401UnauthorizedEntryPoint;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.security.ClaimsTokenEnhancer;
import org.radarcns.management.security.jwt.EcdsaVerifier;
import org.radarcns.management.security.jwt.MultiVerifier;
import org.radarcns.management.security.jwt.RadarJwtAccessTokenConverter;
import org.radarcns.management.security.jwt.RadarKeyStoreKeyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
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
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OAuth2ServerConfiguration {

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
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Autowired
        private DataSource dataSource;

        @Autowired
        private JdbcClientDetailsService jdbcClientDetailsService;

        @Autowired
        private ManagementPortalProperties managementPortalProperties;

        @Bean
        protected AuthorizationCodeServices authorizationCodeServices() {
            return new JdbcAuthorizationCodeServices(dataSource);
        }

        @Bean
        public ApprovalStore approvalStore() {
            return new JdbcApprovalStore(dataSource);
        }

        @Bean
        public TokenEnhancer tokenEnhancer() {
            return new ClaimsTokenEnhancer();
        }

        @Bean
        public TokenStore tokenStore() {
            return new JwtTokenStore(accessTokenConverter());
        }

        @Bean
        public JwtAccessTokenConverter accessTokenConverter() {
            RadarJwtAccessTokenConverter converter = new RadarJwtAccessTokenConverter();

            // set the keypair for signing
            RadarKeyStoreKeyFactory kf = new RadarKeyStoreKeyFactory(
                    new ClassPathResource("/config/keystore.jks"),
                    managementPortalProperties.getOauth().getKeyStorePassword().toCharArray());
            String signKey = managementPortalProperties.getOauth().getSigningKeyAlias();
            KeyPair keyPair = kf.getKeyPair(signKey);
            converter.setKeyPair(keyPair);

            // if a list of checking keys is defined, use that for checking
            if (managementPortalProperties.getOauth().getCheckingKeyAliases() != null
                    && !managementPortalProperties.getOauth().getCheckingKeyAliases().isEmpty()) {
                // get all public keys for verifying and set the converter's verifier
                // to a MultiVerifier
                List<SignatureVerifier> verifiers = managementPortalProperties.getOauth()
                        .getCheckingKeyAliases().stream()
                        .map(alias -> kf.getKeyPair(alias).getPublic())
                        .filter(publicKey -> publicKey instanceof RSAPublicKey
                                || publicKey instanceof ECPublicKey).map(publicKey -> {
                                    if (publicKey instanceof RSAPublicKey) {
                                        return new RsaVerifier((RSAPublicKey) publicKey);
                                    } else {
                                        return new EcdsaVerifier((ECPublicKey) publicKey);
                                    }
                                })
                        .collect(Collectors.toList());
                converter.setVerifier(new MultiVerifier(verifiers));
            }

            return converter;
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
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
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
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.allowFormAuthenticationForClients()
                    .checkTokenAccess("isAuthenticated()")
                    .tokenKeyAccess("isAnonymous() || isAuthenticated()")
                    .passwordEncoder(new BCryptPasswordEncoder());
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(jdbcClientDetailsService);
        }
    }
}
