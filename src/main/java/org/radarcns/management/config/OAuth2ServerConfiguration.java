package org.radarcns.management.config;

import static org.springframework.orm.jpa.vendor.Database.POSTGRESQL;

import io.github.jhipster.security.AjaxLogoutSuccessHandler;
import io.github.jhipster.security.Http401UnauthorizedEntryPoint;
import java.security.KeyPair;
import java.util.Arrays;
import javax.sql.DataSource;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.config.ManagementPortalProperties.Oauth;
import org.radarcns.management.security.ClaimsTokenEnhancer;
import org.radarcns.management.security.PostgresApprovalStore;
import org.radarcns.management.security.jwt.KeyStoreUtil;
import org.radarcns.management.security.jwt.RadarBaseJwtTokenStore;
import org.radarcns.management.security.jwt.RadarJwtAccessTokenConverter;
import org.radarcns.management.security.jwt.RadarKeyStoreKeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
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
        private ManagementPortalProperties managementPortalProperties;

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
            return new RadarBaseJwtTokenStore(accessTokenConverter(), approvalStore());
        }

        @Bean
        public RadarJwtAccessTokenConverter accessTokenConverter() {
            RadarJwtAccessTokenConverter converter = new RadarJwtAccessTokenConverter();

            Oauth oauthConfig = managementPortalProperties.getOauth();

            // set the keypair for signing
            RadarKeyStoreKeyFactory keyFactory = new RadarKeyStoreKeyFactory(
                    Arrays.asList(
                            new ClassPathResource("/config/keystore.p12"),
                            new ClassPathResource("/config/keystore.jks")),
                    oauthConfig.getKeyStorePassword().toCharArray());
            String signKey = oauthConfig.getSigningKeyAlias();
            logger.debug("Using JWT signing key {}", signKey);
            KeyPair keyPair = keyFactory.getKeyPair(signKey);
            if (keyPair == null) {
                throw new IllegalArgumentException("Cannot load JWT signing key " + signKey
                        + " from JWT key store.");
            }

            converter.setAlgorithm(KeyStoreUtil.getAlgorithmFromKeyPair(keyPair));

//            // if a list of checking keys is defined, use that for checking
//            List<String> checkingAliases = oauthConfig.getCheckingKeyAliases();
//
//            if (checkingAliases == null || checkingAliases.isEmpty()) {
//                logger.debug("Using JWT verification key {}", signKey);
//            } else {
//                List<SignatureVerifier> verifiers = Stream
//                        .concat(checkingAliases.stream(), Stream.of(signKey))
//                        .distinct()
//                        .map(alias -> {
//                            KeyPair pair = keyFactory.getKeyPair(alias);
//                            JwtAlgorithm alg = RadarJwtAccessTokenConverter.getJwtAlgorithm(pair);
//                            if (alg != null) {
//                                logger.debug("Using JWT verification key {}", alias);
//                            }
//                            return alg;
//                        })
//                        .filter(Objects::nonNull)
//                        .map(JwtAlgorithm::getVerifier)
//                        .collect(Collectors.toList());
//
//                if (verifiers.size() > 1) {
//                    // get all public keys for verifying and set the converter's verifier
//                    // to a MultiVerifier
//                    converter.setVerifier(new MultiVerifier(verifiers));
//                } else if (verifiers.size() == 1) {
//                    // only has one verifier, use it directly
//                    converter.setVerifier(verifiers.get(0));
//                } else {
//                    // else, use the signing key verifier.
//                    logger.warn("Using JWT signing key {} for verification: none of the provided"
//                            + " verification keys were valid.", signKey);
//                }
//            }

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
                    .tokenKeyAccess("isAnonymous() || isAuthenticated()")
                    .passwordEncoder(new BCryptPasswordEncoder());
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(jdbcClientDetailsService);
        }
    }
}
