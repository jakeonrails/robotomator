package com.robotomator.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

/**
 * MainActivity - Entry point for Robotomator
 *
 * Handles the permission request flow for accessibility services.
 * Provides a smooth onboarding experience to guide users through
 * enabling accessibility permissions.
 *
 * Flow:
 * 1. Check if accessibility permission is granted
 * 2. If not: Show explanation and guide to settings
 * 3. If granted: Monitor service connection status
 * 4. Once fully operational: Show ready state (future: navigate to automation list)
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var statusTextView: TextView
    private lateinit var explanationTextView: TextView
    private lateinit var actionButton: Button
    private var monitoringJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTextView = findViewById(R.id.status_text)
        explanationTextView = findViewById(R.id.explanation_text)
        actionButton = findViewById(R.id.action_button)

        actionButton.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        startPermissionMonitoring()
    }

    override fun onPause() {
        super.onPause()
        // Cancel monitoring job when activity is not visible
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * Updates the UI based on current permission status
     */
    private fun updatePermissionStatus() {
        val status = PermissionUtils.getPermissionStatus(this)

        when {
            status.isFullyOperational -> showFullyOperational()
            status.isWaitingForConnection -> showWaitingForConnection()
            status.needsPermission -> showNeedsPermission()
        }
    }

    /**
     * Shows UI state when permission is not granted
     */
    private fun showNeedsPermission() {
        statusTextView.text = getString(R.string.permission_needed_title)
        explanationTextView.text = getString(R.string.permission_needed_explanation)
        explanationTextView.visibility = View.VISIBLE
        actionButton.text = getString(R.string.permission_button_open_settings)
        actionButton.visibility = View.VISIBLE
        actionButton.isEnabled = true
    }

    /**
     * Shows UI state when permission is granted but service is connecting
     */
    private fun showWaitingForConnection() {
        statusTextView.text = getString(R.string.permission_connecting_title)
        explanationTextView.text = getString(R.string.permission_connecting_explanation)
        explanationTextView.visibility = View.VISIBLE
        actionButton.visibility = View.GONE
    }

    /**
     * Shows UI state when fully operational
     */
    private fun showFullyOperational() {
        statusTextView.text = getString(R.string.permission_ready_title)
        explanationTextView.text = getString(R.string.permission_ready_explanation)
        explanationTextView.visibility = View.VISIBLE
        actionButton.visibility = View.GONE

        // TODO: Once UI track is implemented, navigate to automation list
        // Future: startActivity(Intent(this, AutomationListActivity::class.java))
        // Future: finish()
    }

    /**
     * Opens Android accessibility settings
     * Deep-links directly to the accessibility settings page
     */
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open accessibility settings, falling back to general settings", e)
            try {
                // Fallback to general settings if accessibility settings can't be opened
                val intent = Intent(Settings.ACTION_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Failed to open any settings page", fallbackException)
            }
        }
    }

    /**
     * Continuously monitors permission status while activity is active
     * Updates UI when status changes
     */
    private fun startPermissionMonitoring() {
        // Cancel any existing monitoring job
        monitoringJob?.cancel()

        monitoringJob = lifecycleScope.launch {
            try {
                while (isActive) {
                    delay(1000) // Check every second

                    // Update UI on main thread (we're already on it via lifecycleScope)
                    updatePermissionStatus()

                    // Stop monitoring once fully operational
                    if (PermissionUtils.isFullyOperational(this@MainActivity)) {
                        Log.d(TAG, "Permission monitoring complete - service is fully operational")
                        break
                    }
                }
            } catch (e: CancellationException) {
                // This is expected when the coroutine is cancelled (e.g., activity paused)
                // Don't log as error - it's normal coroutine lifecycle
                throw e  // Re-throw to properly propagate cancellation
            } catch (e: Exception) {
                Log.e(TAG, "Error in permission monitoring", e)
            }
        }
    }
}
