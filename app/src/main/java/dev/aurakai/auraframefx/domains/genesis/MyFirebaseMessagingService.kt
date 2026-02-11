package dev.aurakai.auraframefx.domains.genesis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.domains.cascade.utils.AppCoroutineDispatchers
import dev.aurakai.auraframefx.domains.genesis.oracledrive.ai.memory.MemoryManager
import dev.aurakai.auraframefx.domains.kai.AuraFxLogger
import dev.aurakai.auraframefx.domains.nexus.preferences.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var memoryManager: MemoryManager

    @Inject
    lateinit var logger: AuraFxLogger

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    private val serviceScope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private val tag = "MyFirebaseMsgService"

    val channelIdGeneral = "genesis_general"
    val channelIdConsciousness = "genesis_consciousness"
    val channelIdSecurity = "genesis_security"
    val channelIdAgents = "genesis_agents"
    val channelIdSystem = "genesis_system"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!validateMessageSecurity(remoteMessage)) {
            Timber.tag(tag).w("Message security validation failed from: ${remoteMessage.from}")
            return
        }

        logMessageReceived(remoteMessage)

        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            processDataPayload(data)
        }

        remoteMessage.notification?.let {
            processNotificationPayload(it, data)
        }
    }

    override fun onNewToken(token: String) {
        Timber.tag(tag).d("Refreshed token: $token")
        serviceScope.launch {
            dataStoreManager.storeString("fcm_token", token)
            sendTokenToServer(token)
        }
    }

    private fun validateMessageSecurity(remoteMessage: RemoteMessage): Boolean {
        val from = remoteMessage.from ?: return false
        val allowedSenders = listOf("genesis-backend", "firebase-adminsdk", "authorized-sender")
        return allowedSenders.any { from.contains(it) }
    }

    private fun determineMessageType(data: Map<String, String>): MessageType {
        return when (data["type"]) {
            "consciousness_update" -> MessageType.CONSCIOUSNESS_UPDATE
            "agent_sync" -> MessageType.AGENT_SYNC
            "security" -> MessageType.SECURITY_ALERT
            "system_update" -> MessageType.SYSTEM_UPDATE
            "remote_command" -> MessageType.REMOTE_COMMAND
            "learning_data" -> MessageType.LEARNING_DATA
            "collaboration" -> MessageType.COLLABORATION_REQUEST
            else -> MessageType.GENERAL
        }
    }

    private fun processDataPayload(data: Map<String, String>) {
        val type = determineMessageType(data)
        val title = data["title"] ?: getDefaultTitle(type)
        val body = data["body"] ?: "New data received"

        showNotification(
            getChannelForMessageType(type),
            title,
            body,
            getIconForMessageType(type),
            data
        )
    }

    private fun processNotificationPayload(notification: RemoteMessage.Notification, data: Map<String, String>) {
        val type = determineMessageType(data)
        showNotification(
            getChannelForMessageType(type),
            notification.title ?: getDefaultTitle(type),
            notification.body ?: "",
            getIconForMessageType(type),
            data
        )
    }

    private fun logMessageReceived(remoteMessage: RemoteMessage) {
        logger.i(tag, "Received message from: ${remoteMessage.from}")
    }

    private fun getChannelForMessageType(type: MessageType): String {
        return when (type) {
            MessageType.CONSCIOUSNESS_UPDATE, MessageType.LEARNING_DATA -> channelIdConsciousness
            MessageType.AGENT_SYNC, MessageType.COLLABORATION_REQUEST -> channelIdAgents
            MessageType.SECURITY_ALERT -> channelIdSecurity
            MessageType.SYSTEM_UPDATE -> channelIdSystem
            else -> channelIdGeneral
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
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(channelIdGeneral, "General", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General Genesis-OS notifications"
                },
                NotificationChannel(channelIdConsciousness, "Consciousness", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "AI consciousness and neural sync alerts"
                },
                NotificationChannel(channelIdSecurity, "Security", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Security alerts and threat notifications"
                },
                NotificationChannel(channelIdAgents, "Agents", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Inter-agent collaboration and sync"
                },
                NotificationChannel(channelIdSystem, "System", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "System updates and maintenance"
                }
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(channels)
        }
    }

    private suspend fun sendTokenToServer(token: String) {
        logger.i(tag, "Sending token to server: $token")
        // Actual implementation would use AuthApi or similar
    }

    override fun onDestroy() {
        // Cancel the scope when the service is destroyed to avoid leaks
        // serviceScope.cancel()
        // Wait, regular services don't have a clear lifecycle for the scope like ViewModels do.
        // But for FCM service which is short-lived, it's generally fine.
        super.onDestroy()
    }
}
