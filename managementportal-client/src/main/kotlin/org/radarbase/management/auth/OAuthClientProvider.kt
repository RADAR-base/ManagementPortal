package org.radarbase.management.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Auth::class.java)

/**
 * Installs the client's [BearerAuthProvider].
 */
fun Auth.clientCredentials(block: ClientCredentialsAuthConfig.() -> Unit) {
    with(ClientCredentialsAuthConfig().apply(block)) {
        this@clientCredentials.providers.add(ClientCredentialsAuthProvider(_requestToken, _loadTokens, _sendWithoutRequest, realm))
    }
}

fun Auth.clientCredentials(
    authConfig: ClientCredentialsConfig,
    targetHost: String? = null,
): Flow<MPOAuth2AccessToken?> {
    requireNotNull(authConfig.clientId) { "Missing client ID" }
    requireNotNull(authConfig.clientSecret) { "Missing client secret"}
    val flow = MutableStateFlow<MPOAuth2AccessToken?>(null)

    clientCredentials {
        if (targetHost != null) {
            sendWithoutRequest { request ->
                request.url.host == targetHost
            }
        }
        requestToken {
            val response = client.submitForm(
                url = authConfig.tokenUrl,
                formParameters = Parameters.build {
                    append("grant_type", "client_credentials")
                    append("client_id", authConfig.clientId)
                    append("client_secret", authConfig.clientSecret)
                }
            ) {
                accept(ContentType.Application.Json)
                markAsRequestTokenRequest()
            }
            val refreshTokenInfo: MPOAuth2AccessToken? = if (!response.status.isSuccess()) {
                logger.error("Failed to fetch new token: {}", response.bodyAsText())
                null
            } else {
                response.body<MPOAuth2AccessToken>()
            }
            flow.value = refreshTokenInfo
            refreshTokenInfo
        }
    }

    return flow
}

/**
 * Parameters to be passed to [BearerAuthConfig.refreshTokens] lambda.
 */
class RequestTokenParams(
    val client: HttpClient,
) {
    /**
     * Marks that this request is for requesting auth tokens, resulting in a special handling of it.
     */
    fun HttpRequestBuilder.markAsRequestTokenRequest() {
        attributes.put(Auth.AuthCircuitBreaker, Unit)
    }
}

/**
 * A configuration for [BearerAuthProvider].
 */
@KtorDsl
class ClientCredentialsAuthConfig {
    internal var _requestToken: suspend RequestTokenParams.() -> MPOAuth2AccessToken? = { null }
    internal var _loadTokens: suspend () -> MPOAuth2AccessToken? = { null }
    internal var _sendWithoutRequest: (HttpRequestBuilder) -> Boolean = { true }

    var realm: String? = null

    /**
     * Configures a callback that refreshes a token when the 401 status code is received.
     */
    fun requestToken(block: suspend RequestTokenParams.() -> MPOAuth2AccessToken?) {
        _requestToken = block
    }

    /**
     * Configures a callback that loads a cached token from a local storage.
     * Note: Using the same client instance here to make a request will result in a deadlock.
     */
    fun loadTokens(block: suspend () -> MPOAuth2AccessToken?) {
        _loadTokens = block
    }

    /**
     * Sends credentials without waiting for [HttpStatusCode.Unauthorized].
     */
    fun sendWithoutRequest(block: (HttpRequestBuilder) -> Boolean) {
        _sendWithoutRequest = block
    }
}

/**
 * An authentication provider for the Bearer HTTP authentication scheme.
 * Bearer authentication involves security tokens called bearer tokens.
 * As an example, these tokens can be used as a part of OAuth flow to authorize users of your application
 * by using external providers, such as Google, Facebook, Twitter, and so on.
 *
 * You can learn more from [Bearer authentication](https://ktor.io/docs/bearer-client.html).
 */
class ClientCredentialsAuthProvider(
    private val requestToken: suspend RequestTokenParams.() -> MPOAuth2AccessToken?,
    loadTokens: suspend () -> MPOAuth2AccessToken?,
    private val sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true },
    private val realm: String?,
) : AuthProvider {

    @Suppress("OverridingDeprecatedMember")
    @Deprecated("Please use sendWithoutRequest function instead", replaceWith = ReplaceWith("sendWithoutRequest(request)"))
    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    private val tokensHolder = AuthTokenHolder(loadTokens)

    override fun sendWithoutRequest(request: HttpRequestBuilder): Boolean = sendWithoutRequestCallback(request)

    /**
     * Checks if current provider is applicable to the request.
     */
    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        if (auth.authScheme != AuthScheme.Bearer) return false
        if (realm == null) return true
        if (auth !is HttpAuthHeader.Parameterized) return false

        return auth.parameter("realm") == realm
    }

    /**
     * Adds an authentication method headers and credentials.
     */
    override suspend fun addRequestHeaders(request: HttpRequestBuilder, authHeader: HttpAuthHeader?) {
        val token = tokensHolder.loadToken() ?: return

        request.headers {
            if (contains(HttpHeaders.Authorization)) {
                remove(HttpHeaders.Authorization)
            }
            append(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }
    }

    override suspend fun refreshToken(response: HttpResponse): Boolean {
        val newToken = tokensHolder.setToken {
            requestToken(RequestTokenParams(response.call.client))
        }
        return newToken != null
    }

    fun clearToken() {
        tokensHolder.clearToken()
    }
}
