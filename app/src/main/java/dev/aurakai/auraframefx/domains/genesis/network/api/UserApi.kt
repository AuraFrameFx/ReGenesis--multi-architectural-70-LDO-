package dev.aurakai.auraframefx.domains.genesis.network.api

import dev.aurakai.auraframefx.domains.genesis.network.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * API interface for user-related operations.
 */
interface UserApi {
    /**
     * Get the current user's information.
     *
     * @return The current user's information.
     */
    @GET("user")
    suspend fun getCurrentUser(): User

    /**
     * Update the FCM registration token for the current user.
     *
     * @param request The FCM token request containing the new token.
     */
    @POST("user/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Unit>

    // Add more user-related endpoints as needed
}

/**
 * Request body for updating the FCM registration token.
 */
data class FcmTokenRequest(
    val token: String
)
