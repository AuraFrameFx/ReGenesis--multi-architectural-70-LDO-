package dev.aurakai.auraframefx.xposed.hooks

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import dev.aurakai.auraframefx.models.NotchBarConfig
import androidx.compose.ui.graphics.toArgb

/**
 * Xposed hooker for customizing the Android notch bar (status bar cutout area).
 * Applies visual customizations like color, height, and visibility.
 */
class NotchBarHooker(
    private val classLoader: ClassLoader,
    private val config: NotchBarConfig,
) {
    /**
     * Applies Xposed hooks to customize the notch bar appearance and behavior.
     *
     * Hooks into system UI classes to modify:
     * - Notch bar background color
     * - Notch bar height/size
     * - Notch bar visibility
     */
    fun applyNotchBarHooks() {
        try {
            // Hook the PhoneStatusBarView or similar system UI class
            // Note: Actual class names vary by Android version and OEM
            val statusBarClass = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.PhoneStatusBarView",
                classLoader
            )

            // Hook the onFinishInflate method to apply customizations
            XposedHelpers.findAndHookMethod(
                statusBarClass,
                "onFinishInflate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.thisObject as View

                            // Apply notch bar color if configured - Convert Compose Color to Android Color
                            try {
                                val androidColor = config.backgroundColor.toArgb()
                                view.setBackgroundColor(androidColor)
                            } catch (e: Exception) {
                                XposedBridge.log("NotchBarHooker: Failed to set background color: ${e.message}")
                            }

                            // Apply notch bar height if configured
                            try {
                                val layoutParams = view.layoutParams ?: ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    config.height
                                )
                                layoutParams.height = config.height
                                view.layoutParams = layoutParams
                            } catch (e: Exception) {
                                XposedBridge.log("NotchBarHooker: Failed to set height: ${e.message}")
                            }

                            // Apply visibility if configured
                            try {
                                view.visibility = if (config.isVisible) View.VISIBLE else View.GONE
                            } catch (e: Exception) {
                                XposedBridge.log("NotchBarHooker: Failed to set visibility: ${e.message}")
                            }
                        } catch (e: Exception) {
                            XposedBridge.log("NotchBarHooker: Error in afterHookedMethod: ${e.message}")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            // Log error but don't crash - notch bar customization is optional
            XposedBridge.log("NotchBarHooker: Failed to apply notch bar hooks: ${e.message}")
        }
    }
}
