package org.radarcns.management.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.HttpMethod;
import org.radarcns.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;

/**
 * A pre-filter for all request sent to Zuul proxy. This adds the client credentials and scp
 */
@Component
public class OAuth2TokenRequestPreZuulFilter extends ZuulFilter {

    protected static final String REFRESH_TOKEN_COOKIE = "rft";
    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    private final Logger logger = LoggerFactory.getLogger(OAuth2TokenRequestPreZuulFilter.class);

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        byte[] encoded;
        try {
            // get this from properties, this will allow us to use ENV variables for docker
            encoded = Base64.encode((managementPortalProperties.getFrontend().getClientId() + ":"
                    + managementPortalProperties.getFrontend().getClientSecret())
                    .getBytes("UTF-8"));
            ctx.addZuulRequestHeader("Authorization", "Basic " + new String(encoded));

        } catch (UnsupportedEncodingException e) {
            logger.error("Error occured in pre filter", e);
        }
        return null;
    }

    //    private String extractRefreshToken(HttpServletRequest req) {
    //        final Cookie[] cookies = req.getCookies();
    //        if (cookies != null) {
    //            for (int i = 0; i < cookies.length; i++) {
    //                if (cookies[i].getName().equalsIgnoreCase(REFRESH_TOKEN_COOKIE)) {
    //                    return cookies[i].getValue();
    //                }
    //            }
    //        }
    //        return null;
    //    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String method = ctx.getRequest().getMethod();
        return ctx.getRequest().getRequestURI().contains("/oauth/token") && HttpMethod.POST
                .equals(method);
    }

    @Override
    public int filterOrder() {
        return -2;
    }

    @Override
    public String filterType() {
        return "pre";
    }
}
