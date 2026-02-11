package dev.aurakai.auraframefx.domains.genesis.network

import dev.aurakai.auraframefx.domains.kai.security.auth.TokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepts network requests to add authentication tokens and handle token refresh.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
) : Interceptor {

    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login/refresh endpoints
        if (isAuthRequest(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        // Add token to the request
        val initialToken = tokenManager.accessToken
        if (initialToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        var request = originalRequest.newBuilder()
            .header("Authorization", "Bearer $initialToken")
            .build()

        // Execute the request
        val response = chain.proceed(request)

        // If unauthorized, try to refresh the token and retry the request
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            val newToken = runBlocking {
                refreshMutex.withLock {
                    // Check if the token has already been refreshed by another thread
                    val currentToken = tokenManager.accessToken
                    if (currentToken != initialToken && !currentToken.isNullOrBlank()) {
                        return@withLock currentToken
                    }

                    // Perform the refresh
                    try {
                        tokenManager.refreshToken?.let { refreshToken ->
                            val refreshResponse = authApi.refreshToken(
                                RefreshTokenRequest(refreshToken = refreshToken)
                            )

                            if (refreshResponse.isSuccessful) {
                                val newAccessToken = refreshResponse.body()?.accessToken
                                val newRefreshToken = refreshResponse.body()?.refreshToken
                                val expiresIn = refreshResponse.body()?.expiresIn ?: 3600L

                                if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                                    tokenManager.updateTokens(
                                        accessToken = newAccessToken,
                                        refreshToken = newRefreshToken,
                                        expiresInSeconds = expiresIn
                                    )
                                    return@withLock newAccessToken
                                }
                            } else {
                                // If refresh fails, clear tokens
                                tokenManager.clearTokens()
                                // TODO: Notify UI about session expiration
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to refresh token")
                    }
                    null
                }
            }

            if (newToken != null) {
                response.close()
                val retriedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retriedRequest)
            }
        }

        return response
    }

    private fun isAuthRequest(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/auth/login") ||
                path.endsWith("/auth/refresh") ||
                path.endsWith("/auth/register")
    }

    private fun Response.createErrorResponse(code: Int, message: String): Response {
        val json = JSONObject().apply {
            put("success", false)
            put("message", message)
            put("code", code)
        }.toString()

        return newBuilder()
            .code(code)
            .message(message)
            .body(json.toResponseBody())
            .build()
    }
}

/**
 * Data class for refresh token request.
 */
data class RefreshTokenRequest(
    val refreshToken: String,
)

/**
 * Data class for token response.
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 3600,
)
