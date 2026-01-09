package com.robotomator.app

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log

/**
 * Utilities for detecting accessibility service permission status.
 *
 * This provides the "golden ticket" detection - knowing whether Robotomator
 * has been granted accessibility permissions by the user.
 *
 * Thread Safety: All methods are thread-safe and can be called from any thread.
 * Note: There is a potential TOCTOU (Time Of Check Time Of Use) race condition
 * between checking settings and service connection status, but this is acceptable
 * for UI updates and doesn't affect security.
 */
object PermissionUtils {

    private const val TAG = "PermissionUtils"

    /**
     * Checks if the Robotomator accessibility service is currently enabled.
     *
     * This checks the system settings to see if our service is in the list of
     * enabled accessibility services. Note that this checks the system setting,
     * not just whether the service is connected (which is tracked separately
     * by RobotomatorAccessibilityService.isServiceConnected).
     *
     * Why both checks matter:
     * - This method: Checks if user has granted permission in Settings
     * - isServiceConnected: Checks if the service is actively bound and running
     *
     * A service can be enabled in settings but not yet connected (e.g., during boot),
     * or connected but about to be disabled (brief window during settings change).
     *
     * @param context Android context for accessing system settings
     * @return true if the accessibility service is enabled, false otherwise
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val expectedComponentName = "${context.packageName}/${RobotomatorAccessibilityService::class.java.name}"

            // Query system settings for the list of enabled accessibility services
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            if (enabledServicesSetting.isNullOrEmpty()) {
                return false
            }

            // The setting is a colon-separated list of enabled service component names
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                    Log.d(TAG, "Accessibility service is enabled in settings")
                    return true
                }
            }

            Log.d(TAG, "Accessibility service is not enabled in settings")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service status", e)
            // Fail closed - assume not enabled if we can't check
            false
        }
    }

    /**
     * Comprehensive permission status check.
     *
     * Returns a PermissionStatus object that includes both whether the permission
     * is enabled in settings AND whether the service is currently connected.
     *
     * This gives a complete picture of the permission state:
     * - enabled=true, connected=true: Fully operational (the golden ticket!)
     * - enabled=true, connected=false: Enabled but service not running yet
     * - enabled=false, connected=false: User needs to grant permission
     * - enabled=false, connected=true: Should not happen (service being disabled)
     *
     * @param context Android context for accessing system settings
     * @return PermissionStatus with full permission state
     */
    fun getPermissionStatus(context: Context): PermissionStatus {
        val isEnabled = isAccessibilityServiceEnabled(context)
        val isConnected = RobotomatorAccessibilityService.isServiceConnected

        return PermissionStatus(
            isEnabled = isEnabled,
            isServiceConnected = isConnected
        )
    }

    /**
     * Simple check for whether Robotomator is fully operational.
     *
     * This is the primary check most code should use: it returns true only when
     * both the permission is granted AND the service is actively connected.
     *
     * @param context Android context for accessing system settings
     * @return true if we have permission and the service is running
     */
    fun isFullyOperational(context: Context): Boolean {
        return isAccessibilityServiceEnabled(context) &&
               RobotomatorAccessibilityService.isServiceConnected
    }
}

/**
 * Complete permission status information.
 *
 * @property isEnabled Whether the accessibility service is enabled in system settings
 * @property isServiceConnected Whether the accessibility service is currently bound and active
 */
data class PermissionStatus(
    val isEnabled: Boolean,
    val isServiceConnected: Boolean
) {
    /**
     * True when everything is working - permission granted and service active.
     */
    val isFullyOperational: Boolean
        get() = isEnabled && isServiceConnected

    /**
     * True when user has granted permission but service isn't connected yet.
     * This is usually a temporary state during system startup.
     */
    val isWaitingForConnection: Boolean
        get() = isEnabled && !isServiceConnected

    /**
     * True when the user needs to enable the service in accessibility settings.
     */
    val needsPermission: Boolean
        get() = !isEnabled

    /**
     * Human-readable description of the current state.
     * Useful for debugging and UI display.
     */
    fun getStatusDescription(): String = when {
        isFullyOperational -> "Fully operational - ready to automate!"
        isWaitingForConnection -> "Permission granted, waiting for service to connect"
        !isEnabled && isServiceConnected -> "Unknown state (enabled=$isEnabled, connected=$isServiceConnected)"
        needsPermission -> "Accessibility permission not granted"
        else -> "Unknown state (enabled=$isEnabled, connected=$isServiceConnected)"
    }
}
