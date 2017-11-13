package org.radarcns.management.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.radarcns.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;

/**
 * A pre-filter for all request sent to Zuul proxy.
 * This adds the client credentials and scp
 */
@Component
public class OAuth2TokenRequestPreZuulFilter extends ZuulFilter {

    static final String REFRESH_TOKEN_COOKIE = "rft";
    @Autowired
    private  ManagementPortalProperties managementPortalProperties;

    Logger logger = LoggerFactory.getLogger(OAuth2TokenRequestPreZuulFilter.class);
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getRequest().getRequestURI().contains("/oauth/token")) {
            byte[] encoded;
            try {
                // get this from properties, this will allow us to use ENV variables for docker
                encoded = Base64.encode((managementPortalProperties.getFrontend().getClientId()+":"+managementPortalProperties.getFrontend().getClientSecret()).getBytes("UTF-8"));
                ctx.addZuulRequestHeader("Authorization", "Basic " + new String(encoded));
                final HttpServletRequest req = ctx.getRequest();
                final String refreshToken = extractRefreshToken(req);
                if (refreshToken != null) {
                    final Map<String, String[]> param = new HashMap<String, String[]>();
                    param.put("refresh_token", new String[] { refreshToken });
                    param.put("grant_type", new String[] { "refresh_token" });
                    ctx.setRequest(new CustomHttpServletRequest(req, param));
                }

            } catch (UnsupportedEncodingException e) {
                logger.error("Error occured in pre filter", e);
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest req) {
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equalsIgnoreCase(REFRESH_TOKEN_COOKIE)) {
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }

    @Override
    public boolean shouldFilter() {
        return true;
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
