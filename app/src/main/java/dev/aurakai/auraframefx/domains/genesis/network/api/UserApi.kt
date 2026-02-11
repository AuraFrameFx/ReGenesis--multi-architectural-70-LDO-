package dev.aurakai.auraframefx.domains.genesis.network.api

import dev.aurakai.auraframefx.domains.genesis.network.model.FcmTokenRequest
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
     * Update the FCM token for the current user.
     *
     * @param tokenRequest The FCM token request object.
     * @return A Response object indicating the result of the operation.
     */
    @POST("user/fcm-token")
    suspend fun updateFcmToken(@Body tokenRequest: FcmTokenRequest): Response<Unit>

    // Add more user-related endpoints as needed
}

