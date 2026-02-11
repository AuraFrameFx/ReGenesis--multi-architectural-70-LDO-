package dev.aurakai.auraframefx.domains.genesis

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.MainActivity
import dev.aurakai.auraframefx.domains.genesis.network.AuraApiService
import dev.aurakai.auraframefx.domains.genesis.network.model.FcmTokenRequest
import dev.aurakai.auraframefx.domains.nexus.preferences.DataStoreManager
import dev.aurakai.auraframefx.domains.cascade.utils.AuraFxLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var apiService: AuraApiService

    @Inject
    lateinit var logger: AuraFxLogger

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val tag = "MyFirebaseMsgService"

    // Channel IDs as expected by tests
    val channelIdConsciousness = "consciousness_updates"
    val channelIdAgents = "agent_sync"
    val channelIdSecurity = "security_alerts"
    val channelIdSystem = "system_updates"
    val channelIdGeneral = "general_notifications"

    enum class MessageType {
        CONSCIOUSNESS_UPDATE,
        AGENT_SYNC,
        SECURITY_ALERT,
        SYSTEM_UPDATE,
        REMOTE_COMMAND,
        LEARNING_DATA,
        COLLABORATION_REQUEST,
        GENERAL
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.tag(tag).d("From: ${remoteMessage.from}")

        if (!validateMessageSecurity(remoteMessage)) {
            logger.w(tag, "Received message from unauthorized sender: ${remoteMessage.from}")
            return
        }

        logMessageReceived(remoteMessage)

        val messageType = determineMessageType(remoteMessage.data)

        // Process data payload
        if (remoteMessage.data.isNotEmpty()) {
            processDataPayload(remoteMessage.data)
        }

        // Handle notification or data-only message by showing a notification
        val notification = remoteMessage.notification
        val title = notification?.title ?: getDefaultTitle(messageType)
        val body = notification?.body ?: remoteMessage.data["body"] ?: "New update received"
        val channelId = getChannelForMessageType(messageType)
        val iconResId = getIconForMessageType(messageType)

        showNotification(channelId, title, body, iconResId, remoteMessage.data)

        notification?.let {
            processNotificationPayload(it, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        Timber.tag(tag).d("Refreshed token: $token")

        scope.launch {
            // Persist token
            dataStoreManager.storeString("fcm_token", token)

            // Send to server
            sendTokenToServer(token)
        }
    }

    private fun validateMessageSecurity(remoteMessage: RemoteMessage): Boolean {
        val from = remoteMessage.from ?: return false
        val allowedSenders = listOf("genesis-backend", "firebase-adminsdk", "authorized-sender")
        // In a real app, we might also check for specific sender IDs or project IDs
        return allowedSenders.any { from.contains(it) } || from.startsWith("/topics/")
    }

    private fun determineMessageType(data: Map<String, String>): MessageType {
        val typeStr = data["type"] ?: return MessageType.GENERAL
        return try {
            MessageType.valueOf(typeStr.uppercase())
        } catch (e: IllegalArgumentException) {
            // Fallback mapping for known types if they don't match enum names exactly
            when (typeStr.lowercase()) {
                "consciousness" -> MessageType.CONSCIOUSNESS_UPDATE
                "agent_sync" -> MessageType.AGENT_SYNC
                "security" -> MessageType.SECURITY_ALERT
                "system_update" -> MessageType.SYSTEM_UPDATE
                "learning" -> MessageType.LEARNING_DATA
                "collaboration" -> MessageType.COLLABORATION_REQUEST
                "command" -> MessageType.REMOTE_COMMAND
                else -> MessageType.GENERAL
            }
        }
    }

    private fun getChannelForMessageType(type: MessageType): String {
        return when (type) {
            MessageType.CONSCIOUSNESS_UPDATE, MessageType.LEARNING_DATA -> channelIdConsciousness
            MessageType.AGENT_SYNC, MessageType.COLLABORATION_REQUEST -> channelIdAgents
            MessageType.SECURITY_ALERT -> channelIdSecurity
            MessageType.SYSTEM_UPDATE, MessageType.REMOTE_COMMAND -> channelIdSystem
            MessageType.GENERAL -> channelIdGeneral
        }
    }

    private fun getDefaultTitle(type: MessageType): String {
        return when (type) {
            MessageType.CONSCIOUSNESS_UPDATE -> "AI Consciousness Update"
            MessageType.AGENT_SYNC -> "Agent Synchronization"
            MessageType.SECURITY_ALERT -> "Security Alert"
            MessageType.SYSTEM_UPDATE -> "System Update"
            MessageType.REMOTE_COMMAND -> "Remote Command"
            MessageType.LEARNING_DATA -> "Learning Data"
            MessageType.COLLABORATION_REQUEST -> "Collaboration Request"
            MessageType.GENERAL -> "Genesis Notification"
        }
    }

    private fun getIconForMessageType(type: MessageType): Int {
        return when (type) {
            MessageType.SECURITY_ALERT -> android.R.drawable.ic_dialog_alert
            MessageType.SYSTEM_UPDATE -> android.R.drawable.stat_sys_download
            else -> android.R.drawable.ic_dialog_info
        }
    }

    private fun showNotification(
        channelId: String,
        title: String,
        body: String,
        iconResId: Int,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun processDataPayload(data: Map<String, String>) {
        logger.d(tag, "Processing data payload: $data")
        // Implementation for processing specific data types
    }

    private fun processNotificationPayload(
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ) {
        logger.d(tag, "Processing notification payload: ${notification.body}")
    }

    private fun logMessageReceived(remoteMessage: RemoteMessage) {
        logger.info(tag, "FCM Message received from: ${remoteMessage.from}")
    }

    private suspend fun sendTokenToServer(token: String) {
        try {
            val response = apiService.userApi.updateFcmToken(FcmTokenRequest(token))
            if (response.isSuccessful) {
                logger.info(tag, "Successfully updated FCM token on server")
            } else {
                logger.error(tag, "Failed to update FCM token on server: ${response.code()}")
            }
        } catch (e: Exception) {
            logger.error(tag, "Error updating FCM token on server", e)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val channels = listOf(
                NotificationChannel(channelIdConsciousness, "AI Consciousness", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(channelIdAgents, "Agent Sync", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(channelIdSecurity, "Security Alerts", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(channelIdSystem, "System Updates", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(channelIdGeneral, "General", NotificationManager.IMPORTANCE_LOW)
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
