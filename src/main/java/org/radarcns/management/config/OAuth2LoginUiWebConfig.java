package org.radarcns.management.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

/**
 * Created by dverbeec on 6/07/2017.
 */
@Controller
@SessionAttributes("authorizationRequest")
public class OAuth2LoginUiWebConfig {

    private final String orignalRequestAttributeName = "org.springframework.security.oauth2"
            + ".provider.endpoint.AuthorizationEndpoint.ORIGINAL_AUTHORIZATION_REQUEST";

    @Autowired
    private ClientDetailsService clientDetailsService;

    /**
     * Login form for OAuth2 auhorization flows.
     * @param request the servlet request
     * @param response the servlet response
     * @return a ModelAndView to render the form
     */
    @RequestMapping("/login")
    public ModelAndView getLogin(HttpServletRequest request, HttpServletResponse response) {
        TreeMap<String, Object> model = new TreeMap<>();
        if (request.getParameterMap().containsKey("error")) {
            model.put("loginError", Boolean.TRUE);
        }
        return new ModelAndView("login", model);
    }

    /**
     * Form for a client to confirm authorizing an OAuth client access to the requested resources.
     * @param request the servlet request
     * @param response the servlet response
     * @return a ModelAndView to render the form
     */
    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getAccessConfirmation(HttpServletRequest request,
            HttpServletResponse response) {

        Map<String, String[]> params = request.getParameterMap();

        Map<String, String> authorizationParameters = Stream.of(
                OAuth2Utils.CLIENT_ID, OAuth2Utils.REDIRECT_URI, OAuth2Utils.STATE,
                OAuth2Utils.SCOPE, OAuth2Utils.RESPONSE_TYPE)
                .filter(params::containsKey)
                .collect(Collectors.toMap(Function.identity(), p -> params.get(p)[0]));

        AuthorizationRequest authorizationRequest = new DefaultOAuth2RequestFactory(
                clientDetailsService).createAuthorizationRequest(authorizationParameters);

        TreeMap<String, Object> model = new TreeMap<>();
        model.put("authorizationRequest", authorizationRequest);
        model.put(orignalRequestAttributeName, authorizationRequest);
        return new ModelAndView("authorize", model);
    }

    /**
     * A page to render errors that arised during an OAuth flow.
     * @param req the servlet request
     * @return a ModelAndView to render the page
     */
    @RequestMapping("/oauth/error")
    public ModelAndView handleOAuthClientError(HttpServletRequest req) {
        TreeMap<String, Object> model = new TreeMap<>();
        Object error = req.getAttribute("error");
        // The error summary may contain malicious user input,
        // it needs to be escaped to prevent XSS
        Map<String, String> errorParams = new HashMap<>();
        errorParams.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        if (error instanceof OAuth2Exception) {
            OAuth2Exception oauthError = (OAuth2Exception) error;
            errorParams.put("status", String.format("%d", oauthError.getHttpErrorCode()));
            errorParams.put("code", oauthError.getOAuth2ErrorCode());
            errorParams.put("message", HtmlUtils.htmlEscape(oauthError.getMessage()));
            // transform the additionalInfo map to a comma seperated list of key: value pairs
            if (oauthError.getAdditionalInformation() != null) {
                errorParams.put("additionalInfo", HtmlUtils.htmlEscape(String.join(", ",
                        oauthError.getAdditionalInformation().entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .collect(Collectors.toList()))));
            }
        }
        // Copy non-empty entries to the model. Empty entries will not be present in the model,
        // so the default value will be rendered in the view.
        for (Map.Entry<String, String> entry : errorParams.entrySet()) {
            if (!entry.getValue().equals("")) {
                model.put(entry.getKey(), entry.getValue());
            }
        }
        return new ModelAndView("error", model);
    }
}
