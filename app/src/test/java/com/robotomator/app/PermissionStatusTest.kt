package com.robotomator.app

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PermissionStatus data class
 *
 * Tests all computed properties and state transitions to ensure
 * correct permission status reporting.
 */
class PermissionStatusTest {

    @Test
    fun testFullyOperational() {
        val status = PermissionStatus(isEnabled = true, isServiceConnected = true)
        assertTrue("Should be fully operational when both enabled and connected", status.isFullyOperational)
        assertFalse("Should not be waiting for connection", status.isWaitingForConnection)
        assertFalse("Should not need permission", status.needsPermission)
        assertEquals("Fully operational - ready to automate!", status.getStatusDescription())
    }

    @Test
    fun testWaitingForConnection() {
        val status = PermissionStatus(isEnabled = true, isServiceConnected = false)
        assertFalse("Should not be fully operational", status.isFullyOperational)
        assertTrue("Should be waiting for connection when enabled but not connected", status.isWaitingForConnection)
        assertFalse("Should not need permission", status.needsPermission)
        assertEquals("Permission granted, waiting for service to connect", status.getStatusDescription())
    }

    @Test
    fun testNeedsPermission() {
        val status = PermissionStatus(isEnabled = false, isServiceConnected = false)
        assertFalse("Should not be fully operational", status.isFullyOperational)
        assertFalse("Should not be waiting for connection", status.isWaitingForConnection)
        assertTrue("Should need permission when not enabled", status.needsPermission)
        assertEquals("Accessibility permission not granted", status.getStatusDescription())
    }

    @Test
    fun testEdgeCase_DisablingService() {
        // This can happen briefly when user is disabling the service
        val status = PermissionStatus(isEnabled = false, isServiceConnected = true)
        assertFalse("Should not be fully operational", status.isFullyOperational)
        assertFalse("Should not be waiting for connection", status.isWaitingForConnection)
        assertTrue("Should need permission", status.needsPermission)
        assertTrue("Description should indicate unknown state",
            status.getStatusDescription().contains("Unknown state"))
    }

    @Test
    fun testDataClassEquality() {
        val status1 = PermissionStatus(isEnabled = true, isServiceConnected = true)
        val status2 = PermissionStatus(isEnabled = true, isServiceConnected = true)
        val status3 = PermissionStatus(isEnabled = false, isServiceConnected = true)

        assertEquals("Identical statuses should be equal", status1, status2)
        assertNotEquals("Different statuses should not be equal", status1, status3)
    }

    @Test
    fun testDataClassCopy() {
        val original = PermissionStatus(isEnabled = true, isServiceConnected = false)
        val modified = original.copy(isServiceConnected = true)

        assertTrue("Original should still be waiting", original.isWaitingForConnection)
        assertTrue("Modified should be fully operational", modified.isFullyOperational)
        assertNotEquals("Copy should be different after modification", original, modified)
    }

    @Test
    fun testAllStatesHaveDescription() {
        // Ensure every possible state has a description
        val states = listOf(
            PermissionStatus(isEnabled = true, isServiceConnected = true),
            PermissionStatus(isEnabled = true, isServiceConnected = false),
            PermissionStatus(isEnabled = false, isServiceConnected = false),
            PermissionStatus(isEnabled = false, isServiceConnected = true)
        )

        states.forEach { status ->
            assertNotNull("Every status should have a description", status.getStatusDescription())
            assertTrue("Description should not be empty", status.getStatusDescription().isNotEmpty())
        }
    }
}
