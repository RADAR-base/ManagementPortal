package org.radarbase.management.auth

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.atomic.AtomicReference

internal class AuthTokenHolder<T>(
    private val loadTokens: suspend () -> T?
) {
    private val refreshTokensDeferred = AtomicReference<CompletableDeferred<T?>?>(null)
    private val loadTokensDeferred = AtomicReference<CompletableDeferred<T?>?>(null)

    internal fun clearToken() {
        loadTokensDeferred.set(null)
        refreshTokensDeferred.set(null)
    }

    internal suspend fun loadToken(): T? {
        var deferred: CompletableDeferred<T?>?
        do {
            deferred = loadTokensDeferred.get()
            val newValue = deferred ?: CompletableDeferred()
        } while (!loadTokensDeferred.compareAndSet(deferred, newValue))

        return if (deferred != null) {
            deferred.await()
        } else {
            val newTokens = loadTokens()
            loadTokensDeferred.get()!!.complete(newTokens)
            newTokens
        }
    }

    internal suspend fun setToken(block: suspend () -> T?): T? {
        var deferred: CompletableDeferred<T?>?
        do {
            deferred = refreshTokensDeferred.get()
            val newValue = deferred ?: CompletableDeferred()
        } while (!refreshTokensDeferred.compareAndSet(deferred, newValue))

        val newToken = if (deferred == null) {
            val newTokens = block()
            refreshTokensDeferred.get()!!.complete(newTokens)
            refreshTokensDeferred.set(null)
            newTokens
        } else {
            deferred.await()
        }
        loadTokensDeferred.set(CompletableDeferred(newToken))
        return newToken
    }
}
