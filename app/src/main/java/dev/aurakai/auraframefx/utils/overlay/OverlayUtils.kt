package dev.aurakai.auraframefx.utils.overlay

import android.content.Context
import android.content.om.IOverlayManager
import android.os.Build
import android.os.ServiceManager
import androidx.annotation.RequiresApi
import dev.aurakai.auraframefx.utils.AuraFxLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OverlayUtils - Android Overlay System Integration
 *
 * Provides system-wide color and theme customization via Android's overlay system.
 * Inspired by Iconify's OverlayUtils for ColorBlendr-style theming.
 *
 * Requires root/system permissions for IOverlayManager access.
 */
@Singleton
class OverlayUtils @Inject constructor(
    private val context: Context,
    private val logger: AuraFxLogger
) {

    private val overlayManager: IOverlayManager? by lazy {
        try {
            val service = ServiceManager.getService("overlay")
            IOverlayManager.Stub.asInterface(service)
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to get IOverlayManager: ${e.message}")
            null
        }
    }

    /**
     * Enable an overlay package system-wide
     */
    suspend fun enableOverlay(overlayPackage: String, userId: Int = 0): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            overlayManager?.setEnabled(overlayPackage, true, userId)
            logger.info("OverlayUtils", "Enabled overlay: $overlayPackage")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to enable overlay $overlayPackage: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Disable an overlay package system-wide
     */
    suspend fun disableOverlay(overlayPackage: String, userId: Int = 0): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            overlayManager?.setEnabled(overlayPackage, false, userId)
            logger.info("OverlayUtils", "Disabled overlay: $overlayPackage")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to disable overlay $overlayPackage: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Check if an overlay is currently enabled
     */
    fun isOverlayEnabled(overlayPackage: String, userId: Int = 0): Boolean {
        return try {
            val overlayInfos = overlayManager?.getOverlayInfosForTarget("android", userId)
            overlayInfos?.any {
                it.packageName == overlayPackage && it.isEnabled
            } ?: false
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to check overlay status: ${e.message}")
            false
        }
    }

    /**
     * Change overlay state (enable/disable)
     */
    suspend fun changeOverlayState(
        overlayPackage: String,
        enable: Boolean,
        userId: Int = 0
    ): Result<Unit> {
        return if (enable) {
            enableOverlay(overlayPackage, userId)
        } else {
            disableOverlay(overlayPackage, userId)
        }
    }

    /**
     * Get all overlays for a target package
     */
    fun getOverlaysForTarget(targetPackage: String, userId: Int = 0): List<OverlayInfo> {
        return try {
            val overlayInfos = overlayManager?.getOverlayInfosForTarget(targetPackage, userId)
            overlayInfos?.map { info ->
                OverlayInfo(
                    packageName = info.packageName,
                    targetPackage = info.targetPackageName,
                    isEnabled = info.isEnabled,
                    priority = info.priority
                )
            } ?: emptyList()
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to get overlays for $targetPackage: ${e.message}")
            emptyList()
        }
    }

    /**
     * Create FabricatedOverlay for runtime color customization (Android 12+)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun createFabricatedOverlay(
        overlayName: String,
        targetPackage: String,
        colors: Map<String, Int>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Use FabricatedOverlay API for Android 12+
            val fabricatedOverlay = android.content.om.FabricatedOverlay.Builder(
                "dev.aurakai.auraframefx",
                overlayName,
                targetPackage
            )

            // Add color resources
            colors.forEach { (resourceName, colorValue) ->
                fabricatedOverlay.setResourceValue(
                    "android:color/$resourceName",
                    android.content.res.Configuration.DENSITY_DPI_DEVICE_STABLE,
                    colorValue
                )
            }

            // Commit the overlay
            val overlay = fabricatedOverlay.build()
            overlayManager?.commit(
                android.content.om.OverlayManagerTransaction.Builder()
                    .registerFabricatedOverlay(overlay)
                    .build()
            )

            logger.info("OverlayUtils", "Created fabricated overlay: $overlayName")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to create fabricated overlay: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Remove a fabricated overlay
     */
    @RequiresApi(Build.VERSION_CODES.S)
    suspend fun removeFabricatedOverlay(overlayName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val overlayIdentifier = android.content.om.OverlayIdentifier(
                "dev.aurakai.auraframefx",
                overlayName
            )

            overlayManager?.commit(
                android.content.om.OverlayManagerTransaction.Builder()
                    .unregisterFabricatedOverlay(overlayIdentifier)
                    .build()
            )

            logger.info("OverlayUtils", "Removed fabricated overlay: $overlayName")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("OverlayUtils", "Failed to remove fabricated overlay: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Apply ColorBlendr-style color scheme system-wide
     */
    suspend fun applyColorScheme(
        primary: Int,
        secondary: Int,
        tertiary: Int,
        error: Int,
        background: Int,
        surface: Int
    ): Result<Unit> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return Result.failure(UnsupportedOperationException("Fabricated overlays require Android 12+"))
        }

        val colors = mapOf(
            "system_accent1_500" to primary,
            "system_accent2_500" to secondary,
            "system_accent3_500" to tertiary,
            "system_error_500" to error,
            "system_background" to background,
            "system_surface" to surface
        )

        return createFabricatedOverlay("aurakai_color_scheme", "android", colors)
    }

    /**
     * Reset to default system colors
     */
    suspend fun resetToDefaultColors(): Result<Unit> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return Result.failure(UnsupportedOperationException("Fabricated overlays require Android 12+"))
        }

        return removeFabricatedOverlay("aurakai_color_scheme")
    }

    data class OverlayInfo(
        val packageName: String,
        val targetPackage: String,
        val isEnabled: Boolean,
        val priority: Int
    )
}
