package com.robotomator.app

import android.accessibilityservice.AccessibilityService
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Global Actions functionality in RobotomatorAccessibilityService.
 */
class GlobalActionsTest {

    private lateinit var service: RobotomatorAccessibilityService

    @Before
    fun setUp() {
        // Create a spy of the service so we can mock performGlobalAction(int)
        service = spyk(RobotomatorAccessibilityService())

        // Set the service as connected by setting the instance reference
        val instanceRefField = RobotomatorAccessibilityService::class.java
            .getDeclaredField("instanceRef")
            .apply { isAccessible = true }
        val instanceRef = instanceRefField.get(null) as java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>
        instanceRef.set(service)
    }

    @After
    fun tearDown() {
        // Reset service connection state
        val instanceRefField = RobotomatorAccessibilityService::class.java
            .getDeclaredField("instanceRef")
            .apply { isAccessible = true }
        val instanceRef = instanceRefField.get(null) as java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>
        instanceRef.set(null)

        clearAllMocks()
    }

    @Test
    fun `performGlobalAction BACK returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.BACK)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }
    }

    @Test
    fun `performGlobalAction HOME returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.HOME)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
    }

    @Test
    fun `performGlobalAction RECENTS returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.RECENTS)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) }
    }

    @Test
    fun `performGlobalAction NOTIFICATIONS returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.NOTIFICATIONS)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) }
    }

    @Test
    fun `performGlobalAction QUICK_SETTINGS returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.QUICK_SETTINGS)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) }
    }

    @Test
    fun `performGlobalAction POWER_DIALOG returns Success when system accepts action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) } returns true

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.POWER_DIALOG)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Success)
        verify { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) }
    }

    @Test
    fun `performGlobalAction returns SystemDenied when system rejects action`() {
        // Given
        every { service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) } returns false

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.BACK)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.SystemDenied)
        val systemDenied = result as RobotomatorAccessibilityService.GlobalActionResult.SystemDenied
        assertEquals(RobotomatorAccessibilityService.GlobalAction.BACK, systemDenied.action)
    }

    @Test
    fun `performGlobalAction returns ServiceNotConnected when service is not connected`() {
        // Given - set service as disconnected
        val instanceRefField = RobotomatorAccessibilityService::class.java
            .getDeclaredField("instanceRef")
            .apply { isAccessible = true }
        val instanceRef = instanceRefField.get(null) as java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>
        instanceRef.set(null)

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.BACK)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.ServiceNotConnected)
        verify(exactly = 0) { service.performGlobalAction(any<Int>()) }
    }

    @Test
    fun `performGlobalAction returns Error when exception is thrown`() {
        // Given
        val exceptionMessage = "Test exception"
        every {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        } throws IllegalStateException(exceptionMessage)

        // When
        val result = service.performGlobalAction(RobotomatorAccessibilityService.GlobalAction.HOME)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.GlobalActionResult.Error)
        val error = result as RobotomatorAccessibilityService.GlobalActionResult.Error
        assertEquals(RobotomatorAccessibilityService.GlobalAction.HOME, error.action)
        assertEquals(exceptionMessage, error.message)
    }

    @Test
    fun `GlobalAction enum has correct action IDs`() {
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_BACK,
            RobotomatorAccessibilityService.GlobalAction.BACK.actionId
        )
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_HOME,
            RobotomatorAccessibilityService.GlobalAction.HOME.actionId
        )
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_RECENTS,
            RobotomatorAccessibilityService.GlobalAction.RECENTS.actionId
        )
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS,
            RobotomatorAccessibilityService.GlobalAction.NOTIFICATIONS.actionId
        )
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS,
            RobotomatorAccessibilityService.GlobalAction.QUICK_SETTINGS.actionId
        )
        assertEquals(
            AccessibilityService.GLOBAL_ACTION_POWER_DIALOG,
            RobotomatorAccessibilityService.GlobalAction.POWER_DIALOG.actionId
        )
    }

    @Test
    fun `GlobalAction fromActionId returns correct action`() {
        assertEquals(
            RobotomatorAccessibilityService.GlobalAction.BACK,
            RobotomatorAccessibilityService.GlobalAction.fromActionId(
                AccessibilityService.GLOBAL_ACTION_BACK
            )
        )
        assertEquals(
            RobotomatorAccessibilityService.GlobalAction.HOME,
            RobotomatorAccessibilityService.GlobalAction.fromActionId(
                AccessibilityService.GLOBAL_ACTION_HOME
            )
        )
    }

    @Test
    fun `GlobalAction fromActionId returns null for invalid action ID`() {
        assertNull(RobotomatorAccessibilityService.GlobalAction.fromActionId(99999))
    }

    @Test
    fun `GlobalAction enum contains all expected actions`() {
        val actions = RobotomatorAccessibilityService.GlobalAction.values()

        assertEquals(6, actions.size)
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.BACK))
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.HOME))
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.RECENTS))
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.NOTIFICATIONS))
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.QUICK_SETTINGS))
        assertTrue(actions.contains(RobotomatorAccessibilityService.GlobalAction.POWER_DIALOG))
    }
}
