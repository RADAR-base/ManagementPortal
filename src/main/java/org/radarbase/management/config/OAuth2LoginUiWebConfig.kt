package org.radarbase.management.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.HtmlUtils
import java.lang.Boolean
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.Any
import kotlin.String

/**
 * Created by dverbeec on 6/07/2017.
 */
@Controller
@SessionAttributes("authorizationRequest")
class OAuth2LoginUiWebConfig {
    @Autowired
    private val clientDetailsService: ClientDetailsService? = null

    /**
     * Login form for OAuth2 auhorization flows.
     * @param request the servlet request
     * @param response the servlet response
     * @return a ModelAndView to render the form
     */
    @RequestMapping("/login")
    fun getLogin(request: HttpServletRequest, response: HttpServletResponse?): ModelAndView {
        val model = TreeMap<String, Any?>()
        if (request.parameterMap.containsKey("error")) {
            model["loginError"] = Boolean.TRUE
        }
        return ModelAndView("login", model)
    }

    /**
     * Form for a client to confirm authorizing an OAuth client access to the requested resources.
     * @param request the servlet request
     * @param response the servlet response
     * @return a ModelAndView to render the form
     */
    @RequestMapping("/oauth/confirm_access")
    fun getAccessConfirmation(
        request: HttpServletRequest,
        response: HttpServletResponse?
    ): ModelAndView {
        val params = request.parameterMap
        val authorizationParameters = Stream.of(
            OAuth2Utils.CLIENT_ID, OAuth2Utils.REDIRECT_URI, OAuth2Utils.STATE,
            OAuth2Utils.SCOPE, OAuth2Utils.RESPONSE_TYPE
        )
            .filter { key: String -> params.containsKey(key) }
            .collect(Collectors.toMap(Function.identity(), Function { p: String -> params[p]!![0] }))
        val authorizationRequest = DefaultOAuth2RequestFactory(
            clientDetailsService
        ).createAuthorizationRequest(authorizationParameters)
        val model = Collections.singletonMap<String, Any?>(
            "authorizationRequest",
            authorizationRequest
        )
        return ModelAndView("authorize", model)
    }

    /**
     * A page to render errors that arised during an OAuth flow.
     * @param req the servlet request
     * @return a ModelAndView to render the page
     */
    @RequestMapping("/oauth/error")
    fun handleOAuthClientError(req: HttpServletRequest): ModelAndView {
        val model = TreeMap<String, Any?>()
        val error = req.getAttribute("error")
        // The error summary may contain malicious user input,
        // it needs to be escaped to prevent XSS
        val errorParams: MutableMap<String, String> = HashMap()
        errorParams["date"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .format(Date())
        if (error is OAuth2Exception) {
            val oauthError = error
            errorParams["status"] = String.format("%d", oauthError.httpErrorCode)
            errorParams["code"] = oauthError.oAuth2ErrorCode
            errorParams["message"] = HtmlUtils.htmlEscape(oauthError.message)
            // transform the additionalInfo map to a comma seperated list of key: value pairs
            if (oauthError.additionalInformation != null) {
                errorParams["additionalInfo"] = HtmlUtils.htmlEscape(
                    oauthError.additionalInformation.entries.joinToString(", ") { entry -> entry.key + ": " + entry.value }
                )
            }
        }
        // Copy non-empty entries to the model. Empty entries will not be present in the model,
        // so the default value will be rendered in the view.
        for ((key, value) in errorParams) {
            if (value != "") {
                model[key] = value
            }
        }
        return ModelAndView("error", model)
    }
}
