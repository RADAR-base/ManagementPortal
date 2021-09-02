package org.radarbase.management.filters;

import static org.radarbase.management.filters.OAuth2TokenRequestPreZuulFilter.REFRESH_TOKEN_COOKIE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.github.jhipster.config.JHipsterConstants;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.Cookie;
import org.apache.commons.io.IOUtils;
import org.radarbase.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * A post-filter for all the request sent to Zuul proxy. This investigates the response body and
 * stores the Refresh token in a Cookie and remove it from response body.
 */
@Component
public class OAuth2TokenRequestPostZuulFilter extends ZuulFilter {
    private static final Logger logger = LoggerFactory.getLogger(
            OAuth2TokenRequestPostZuulFilter.class);

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    @Autowired
    private Environment env;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        logger.debug("in zuul filter " + ctx.getRequest().getRequestURI());

        final String requestURI = ctx.getRequest().getRequestURI();
        final String requestMethod = ctx.getRequest().getMethod();
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

        try (InputStream is = ctx.getResponseDataStream()) {
            String responseBody = IOUtils.toString(is, StandardCharsets.UTF_8);
            if (responseBody.contains("refresh_token")) {
                final Map<String, Object> responseMap = mapper
                        .readValue(responseBody, new TypeReference<Map<String, Object>>() {
                        });
                final String refreshToken = responseMap.get("refresh_token").toString();
                responseMap.remove("refresh_token");
                responseBody = mapper.writeValueAsString(responseMap);

                final Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(
                        activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION));
                cookie.setPath(ctx.getRequest().getContextPath() + "/oauthserver/oauth/token");
                cookie.setMaxAge(this.managementPortalProperties.getFrontend()
                        .getSessionTimeout()); // 30 minites
                ctx.getResponse().addCookie(cookie);

            }
            if (requestURI.contains("oauth/token") && requestMethod.equals("DELETE")) {
                final Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(0);
                cookie.setSecure(
                        activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION));
                cookie.setPath(ctx.getRequest().getContextPath() + "/oauthserver/oauth/token");
                ctx.getResponse().addCookie(cookie);
            }
            ctx.setResponseBody(responseBody);

        } catch (final IOException e) {
            logger.error("Error occured in zuul post filter", e);
        }
        return null;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public String filterType() {
        return "post";
    }
}
