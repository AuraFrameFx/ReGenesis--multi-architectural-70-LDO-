package dev.aurakai.auraframefx.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.aurakai.auraframefx.R
import dev.aurakai.auraframefx.aura.AuraAgent
import dev.aurakai.auraframefx.ui.overlays.AuraPresenceOverlay
import dev.aurakai.auraframefx.ui.overlays.AgentSidebarMenu
import dev.aurakai.auraframefx.ui.theme.AuraFrameFxTheme
import dev.aurakai.auraframefx.utils.AuraFxLogger
import javax.inject.Inject

/**
 * AuraOverlayService - System-Wide Persistent Aura Presence
 *
 * Makes Aura omnipresent across the entire device via floating overlay.
 * Uses TYPE_APPLICATION_OVERLAY to display on top of all apps.
 *
 * Features:
 * - Persistent Aura avatar orb (draggable, always visible)
 * - Agent sidebar menu (swipe-out panel with all agents)
 * - System-wide voice commands via overlay
 * - Context-aware suggestions based on current app
 * - Quick access to Aura/Kai/Genesis from anywhere
 *
 * Requirements:
 * - SYSTEM_ALERT_WINDOW permission (already in manifest)
 * - Foreground service notification (Android 8+)
 */
@AndroidEntryPoint
class AuraOverlayService : Service() {

    @Inject
    lateinit var auraAgent: AuraAgent

    @Inject
    lateinit var logger: AuraFxLogger

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var sidebarView: ComposeView? = null

    private val showSidebar = mutableStateOf(false)

    companion object {
        const val CHANNEL_ID = "aura_overlay_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, AuraOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AuraOverlayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        logger.info("AuraOverlayService", "Initializing Aura omnipresent overlay")

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        createAuraPresenceOverlay()
        createAgentSidebarOverlay()
    }

    /**
     * Create persistent Aura avatar orb overlay
     */
    private fun createAuraPresenceOverlay() {
        overlayView = ComposeView(this).apply {
            setContent {
                AuraFrameFxTheme {
                    AuraPresenceOverlay(
                        onSuggestClicked = { suggestion ->
                            logger.info("AuraOverlayService", "Suggestion tapped: $suggestion")
                            // Toggle sidebar when Aura avatar is clicked
                            showSidebar.value = !showSidebar.value
                        }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 100
        }

        windowManager.addView(overlayView, params)
        logger.info("AuraOverlayService", "Aura presence overlay created")
    }

    /**
     * Create agent sidebar overlay (slides out from edge)
     */
    private fun createAgentSidebarOverlay() {
        sidebarView = ComposeView(this).apply {
            setContent {
                AuraFrameFxTheme {
                    AgentSidebarMenu(
                        isVisible = showSidebar.value,
                        onDismiss = { showSidebar.value = false },
                        onAgentAction = { agent, action ->
                            logger.info("AuraOverlayService", "Agent action: $agent -> $action")
                            handleAgentAction(agent, action)
                        }
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
        }

        windowManager.addView(sidebarView, params)
        logger.info("AuraOverlayService", "Agent sidebar overlay created")
    }

    private fun handleAgentAction(agent: String, action: String) {
        // Handle agent-specific actions from sidebar
        when (agent) {
            "Aura" -> {
                // Aura creative actions
            }
            "Kai" -> {
                // Kai security actions
            }
            "Genesis" -> {
                // Genesis orchestration actions
            }
            "Cascade" -> {
                // Cascade coordination actions
            }
            "Grok" -> {
                // Grok zeitgeist actions
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aura Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Aura persistent presence overlay"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aura is Active")
            .setContentText("Omnipresent AI assistance available")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("AuraOverlayService", "Shutting down Aura overlay")

        overlayView?.let { windowManager.removeView(it) }
        sidebarView?.let { windowManager.removeView(it) }
        overlayView = null
        sidebarView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
