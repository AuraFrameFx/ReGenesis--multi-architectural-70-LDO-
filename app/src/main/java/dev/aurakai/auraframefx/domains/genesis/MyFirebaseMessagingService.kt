package dev.aurakai.auraframefx.domains.genesis

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.domains.cascade.utils.AppCoroutineDispatchers
import dev.aurakai.auraframefx.domains.genesis.network.api.FcmTokenRequest
import dev.aurakai.auraframefx.domains.genesis.network.api.UserApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * MyFirebaseMessagingService - Handles FCM messages and token refreshes.
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userApi: UserApi

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    private val serviceScope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    private val tag = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO: Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.tag(tag).d("From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Timber.tag(tag).d("Message data payload: %s", remoteMessage.data)
            // Handle data payload here
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.tag(tag).d("Message Notification Body: ${it.body}")
            // Handle notification payload here
        }
    }

    override fun onNewToken(token: String) {
        Timber.tag(tag).d("Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    /**
     * Persist token to the backend server.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        serviceScope.launch(dispatchers.io) {
            try {
                val response = userApi.updateFcmToken(FcmTokenRequest(token))
                if (response.isSuccessful) {
                    Timber.tag(tag).d("Successfully updated FCM token on server")
                } else {
                    Timber.tag(tag).e("Failed to update FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Error updating FCM token on server")
            }
        }
    }
}
