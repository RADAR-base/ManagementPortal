package org.radarcns.management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by dverbeec on 6/07/2017.
 */
@Controller
@SessionAttributes("authorizationRequest")
public class OAuth2LoginUiWebConfig {

    @Autowired
    ClientDetailsService clientDetailsService;

    private Logger log = LoggerFactory.getLogger(OAuth2LoginUiWebConfig.class);

    @RequestMapping("/login")
    public String getLogin() {
        return "login";
    }


    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        Map<String, String[]> params = request.getParameterMap();
        for (String param : params.keySet()) {
            log.info("Received param " + param + ": [" + String.join(",", params.get(param)) + "]");
        }

        Map<String, String> authorizationParameters = new HashMap<>();
        Arrays.asList(OAuth2Utils.CLIENT_ID, OAuth2Utils.REDIRECT_URI, OAuth2Utils.STATE,
            OAuth2Utils.SCOPE, OAuth2Utils.RESPONSE_TYPE)
            .stream()
            .filter(p -> params.containsKey(p))
            .forEach(p -> authorizationParameters.put(p, params.get(p)[0]));

        AuthorizationRequest authorizationRequest = new DefaultOAuth2RequestFactory
            (clientDetailsService).createAuthorizationRequest(authorizationParameters);

        TreeMap<String, Object> model = new TreeMap<String, Object>();
        model.put("authorizationRequest", authorizationRequest);
        return new ModelAndView("authorize", model);
    }
}
