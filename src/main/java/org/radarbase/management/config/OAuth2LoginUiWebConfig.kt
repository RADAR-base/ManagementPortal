package org.radarbase.management.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.exceptions.InvalidClientException
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory
import org.springframework.stereotype.Controller
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.HtmlUtils
import java.net.URLEncoder
import java.security.Principal
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * Created by dverbeec on 6/07/2017.
 */
@Controller
@SessionAttributes("authorizationRequest")
class OAuth2LoginUiWebConfig(
    @Autowired private val tokenEndPoint: TokenEndpoint,
) {

    @Autowired
    private val clientDetailsService: ClientDetailsService? = null

    @RequestMapping("/oauth2/authorize")
    fun redirect_authorize(request: HttpServletRequest): String {
        val returnString = URLEncoder.encode(request.requestURL.toString().replace("oauth2", "oauth") + "?" + request.parameterMap.map{ param -> param.key + "=" + param.value.first()}.joinToString("&"), "UTF-8")
        return "redirect:https://radar-k3s-test.thehyve.net/kratos-ui/login?return_to=$returnString"
    }

    @PostMapping(value = ["/oauth/token"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    @Throws(
        HttpRequestMethodNotSupportedException::class
    )
    fun postAccessToken(@RequestParam parameters: Map<String, String>, principal: Principal?):
            ResponseEntity<OAuth2AccessToken> {
        if (principal !is Authentication) {
            throw InsufficientAuthenticationException(
                "There is no client authentication. Try adding an appropriate authentication filter."
            )
        }

        val clientId: String = parameters.get("client_id") ?: throw InvalidClientException("No client_id in request")
        var radarPrincipal = RadarPrincipal(clientId, principal)

        val token2 = this.tokenEndPoint.postAccessToken(radarPrincipal, parameters)// loadClientByClientId(clientId)
        return getResponse(token2.body)
    }

    fun getResponse(accessToken: OAuth2AccessToken): ResponseEntity<OAuth2AccessToken> {
        val headers = HttpHeaders()
        headers["Cache-Control"] = "no-store"
        headers["Pragma"] = "no-cache"
        headers["Content-Type"] = "application/json"
        return ResponseEntity(accessToken, headers, HttpStatus.OK)
    }


    @PostMapping(
        "/oauth2/token",
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
        )
    fun redirect_token(request: HttpServletRequest, response: HttpServletResponse) {
        var dispatcher: RequestDispatcher =  request.servletContext.getRequestDispatcher("/oauth/token/")
        dispatcher.forward(request, response)
    }

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
            model["loginError"] = true
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
            errorParams["message"] = oauthError.message?.let { HtmlUtils.htmlEscape(it) } ?: "No error message found"
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

    private class RadarPrincipal(private val name: String, private val auth: Authentication) : Principal, Authentication {

        override fun getName(): String {
            return name
        }

        override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
            return auth.authorities
        }

        override fun getCredentials(): Any {
            return auth.credentials
        }

        override fun getDetails(): Any {
            return auth.details
        }

        override fun getPrincipal(): Any {
            return this
        }

        override fun isAuthenticated(): Boolean {
            return auth.isAuthenticated
        }

        override fun setAuthenticated(isAuthenticated: Boolean) {
            auth.isAuthenticated = isAuthenticated
        }

    }
}
