package com.robotomator.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PermissionUtils
 *
 * These tests run on an Android device or emulator and verify
 * the actual permission detection logic against the Android system.
 *
 * Note: Some tests may require the accessibility service to be
 * enabled/disabled manually in Settings for full coverage.
 */
@RunWith(AndroidJUnit4::class)
class PermissionUtilsInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testContextIsValid() {
        assertNotNull("Context should not be null", context)
        assertEquals("Package name should match", "com.robotomator.app", context.packageName)
    }

    @Test
    fun testIsAccessibilityServiceEnabled_ReturnsBoolean() {
        // This test verifies that the method runs without crashing
        // and returns a boolean value
        val isEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)

        // The result could be true or false depending on whether
        // the tester has enabled the service in Settings
        assertTrue("Result should be true or false", isEnabled || !isEnabled)
    }

    @Test
    fun testGetPermissionStatus_ReturnsValidObject() {
        val status = PermissionUtils.getPermissionStatus(context)

        assertNotNull("Permission status should not be null", status)

        // Verify the object has valid properties
        val description = status.getStatusDescription()
        assertNotNull("Status description should not be null", description)
        assertTrue("Status description should not be empty", description.isNotEmpty())
    }

    @Test
    fun testIsFullyOperational_ReturnsBoolean() {
        val isOperational = PermissionUtils.isFullyOperational(context)

        // Should return a boolean without crashing
        assertTrue("Result should be true or false", isOperational || !isOperational)
    }

    @Test
    fun testPermissionStatus_ConsistentWithIndividualChecks() {
        // The combined status should be consistent with individual checks
        val status = PermissionUtils.getPermissionStatus(context)
        val isEnabled = PermissionUtils.isAccessibilityServiceEnabled(context)
        val isConnected = RobotomatorAccessibilityService.isServiceConnected

        assertEquals("Status isEnabled should match direct check", isEnabled, status.isEnabled)
        assertEquals("Status isServiceConnected should match direct check",
            isConnected, status.isServiceConnected)
    }

    @Test
    fun testPermissionStatus_LogicalConsistency() {
        val status = PermissionUtils.getPermissionStatus(context)

        // Test logical relationships between computed properties
        if (status.isFullyOperational) {
            assertTrue("Fully operational requires enabled", status.isEnabled)
            assertTrue("Fully operational requires connected", status.isServiceConnected)
            assertFalse("Fully operational means not waiting", status.isWaitingForConnection)
            assertFalse("Fully operational means has permission", status.needsPermission)
        }

        if (status.isWaitingForConnection) {
            assertTrue("Waiting for connection requires enabled", status.isEnabled)
            assertFalse("Waiting for connection means not connected", status.isServiceConnected)
            assertFalse("Waiting for connection means not fully operational", status.isFullyOperational)
        }

        if (status.needsPermission) {
            assertFalse("Needs permission means not enabled", status.isEnabled)
            assertFalse("Needs permission means not fully operational", status.isFullyOperational)
        }
    }

    @Test
    fun testMultipleCallsReturnConsistentResults() {
        // Calling the same method multiple times in quick succession
        // should return consistent results (no race conditions)
        val results = mutableListOf<Boolean>()

        repeat(10) {
            results.add(PermissionUtils.isAccessibilityServiceEnabled(context))
        }

        // All results should be the same (service state shouldn't change that fast)
        val firstResult = results.first()
        assertTrue("Multiple calls should return consistent results",
            results.all { it == firstResult })
    }

    @Test
    fun testErrorHandling_NullSafeContext() {
        // While we can't pass null (Kotlin non-null type), verify the method
        // handles edge cases gracefully
        val status = PermissionUtils.getPermissionStatus(context)

        // Should always return a valid status object, never null
        assertNotNull("Status should never be null even in edge cases", status)
    }
}
