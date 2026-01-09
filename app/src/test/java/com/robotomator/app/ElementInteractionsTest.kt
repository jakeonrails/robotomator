package com.robotomator.app

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for Element Interactions functionality in RobotomatorAccessibilityService.
 */
class ElementInteractionsTest {

    private lateinit var service: RobotomatorAccessibilityService
    private lateinit var mockRootNode: AccessibilityNodeInfo
    private lateinit var mockTargetNode: AccessibilityNodeInfo

    @Before
    fun setUp() {
        // Create a spy of the service
        service = spyk(RobotomatorAccessibilityService())

        // Set the service as connected by setting the instance reference
        val instanceRefField = RobotomatorAccessibilityService::class.java
            .getDeclaredField("instanceRef")
            .apply { isAccessible = true }
        val instanceRef = instanceRefField.get(null) as java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>
        instanceRef.set(service)

        // Create mock nodes
        mockRootNode = mockk(relaxed = true)
        mockTargetNode = mockk(relaxed = true)

        // Mock rootInActiveWindow
        every { service.rootInActiveWindow } returns mockRootNode
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

    // ===== ElementSelector Tests =====

    @Test
    fun `ElementSelector requires at least one criterion`() {
        // Should succeed with at least one criterion
        RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        RobotomatorAccessibilityService.ElementSelector(resourceId = "button_submit")
        RobotomatorAccessibilityService.ElementSelector(className = "android.widget.Button")
        RobotomatorAccessibilityService.ElementSelector(contentDescription = "Submit button")

        // Should fail with no criteria
        try {
            RobotomatorAccessibilityService.ElementSelector()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("At least one selector criterion") == true)
        }
    }

    // ===== findElement Tests =====

    @Test
    fun `findElement returns ServiceNotConnected when service not connected`() {
        // Given
        setServiceConnected(false)

        // When
        val result = service.findElement(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.FindElementResult.ServiceNotConnected)
    }

    @Test
    fun `findElement returns NotFound when no active window`() {
        // Given
        every { service.rootInActiveWindow } returns null

        // When
        val result = service.findElement(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.FindElementResult.NotFound)
    }

    @Test
    fun `findElement returns Found when element matches by text`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetText = "Submit"
        )

        // When
        val result = service.findElement(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.FindElementResult.Found)
        val found = result as RobotomatorAccessibilityService.FindElementResult.Found
        assertEquals(mockTargetNode, found.node)
    }

    @Test
    fun `findElement returns Found when element matches by resourceId`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetResourceId = "com.example:id/submit_button"
        )

        // When
        val result = service.findElement(
            RobotomatorAccessibilityService.ElementSelector(
                resourceId = "com.example:id/submit_button"
            )
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.FindElementResult.Found)
    }

    @Test
    fun `findElement returns NotFound when element does not exist`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = false)

        // When
        val result = service.findElement(
            RobotomatorAccessibilityService.ElementSelector(text = "NonExistent")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.FindElementResult.NotFound)
    }

    // ===== performTap Tests =====

    @Test
    fun `performTap returns Success when tap succeeds`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = true, targetText = "Submit")
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK) } returns true

        // When
        val result = service.performTap(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK) }
        verify { mockTargetNode.recycle() }
    }

    @Test
    fun `performTap returns ActionFailed when tap returns false`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = true, targetText = "Submit")
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK) } returns false

        // When
        val result = service.performTap(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ActionFailed)
        val actionFailed = result as RobotomatorAccessibilityService.InteractionResult.ActionFailed
        assertEquals("Click action returned false", actionFailed.reason)
    }

    @Test
    fun `performTap returns ElementNotFound when element does not exist`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = false)

        // When
        val result = service.performTap(
            RobotomatorAccessibilityService.ElementSelector(text = "NonExistent")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ElementNotFound)
    }

    @Test
    fun `performTap returns ServiceNotConnected when service not connected`() {
        // Given
        setServiceConnected(false)

        // When
        val result = service.performTap(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ServiceNotConnected)
    }

    // ===== performLongPress Tests =====

    @Test
    fun `performLongPress returns Success when long press succeeds`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = true, targetText = "Submit")
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK) } returns true

        // When
        val result = service.performLongPress(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK) }
        verify { mockTargetNode.recycle() }
    }

    @Test
    fun `performLongPress returns ActionFailed when long press returns false`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = true, targetText = "Submit")
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK) } returns false

        // When
        val result = service.performLongPress(
            RobotomatorAccessibilityService.ElementSelector(text = "Submit")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ActionFailed)
        val actionFailed = result as RobotomatorAccessibilityService.InteractionResult.ActionFailed
        assertEquals("Long click action returned false", actionFailed.reason)
    }

    @Test
    fun `performLongPress returns ElementNotFound when element does not exist`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = false)

        // When
        val result = service.performLongPress(
            RobotomatorAccessibilityService.ElementSelector(text = "NonExistent")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ElementNotFound)
    }

    // ===== performType Tests =====

    @Test
    fun `performType returns Success when typing succeeds`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetText = "",
            targetResourceId = "input_field"
        )
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS) } returns true
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, any()) } returns true

        // When
        val result = service.performType(
            RobotomatorAccessibilityService.ElementSelector(resourceId = "input_field"),
            "Hello World"
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS) }
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, any()) }
        verify { mockTargetNode.recycle() }
    }

    @Test
    fun `performType returns ActionFailed when focus fails`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetText = "",
            targetResourceId = "input_field"
        )
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS) } returns false

        // When
        val result = service.performType(
            RobotomatorAccessibilityService.ElementSelector(resourceId = "input_field"),
            "Hello World"
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ActionFailed)
        val actionFailed = result as RobotomatorAccessibilityService.InteractionResult.ActionFailed
        assertEquals("Could not focus element", actionFailed.reason)
    }

    @Test
    fun `performType returns ActionFailed when setText fails`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetText = "",
            targetResourceId = "input_field"
        )
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS) } returns true
        every { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, any()) } returns false

        // When
        val result = service.performType(
            RobotomatorAccessibilityService.ElementSelector(resourceId = "input_field"),
            "Hello World"
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ActionFailed)
        val actionFailed = result as RobotomatorAccessibilityService.InteractionResult.ActionFailed
        assertEquals("Set text action returned false", actionFailed.reason)
    }

    @Test
    fun `performType returns ElementNotFound when element does not exist`() {
        // Given
        setupNodeTree(rootMatches = false, targetMatches = false)

        // When
        val result = service.performType(
            RobotomatorAccessibilityService.ElementSelector(text = "NonExistent"),
            "Hello"
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ElementNotFound)
    }

    // ===== performScroll Tests =====

    @Test
    fun `performScroll returns Success when scrolling down succeeds`() {
        // Given
        setupScrollableNodeTree()
        every {
            mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } returns true

        // When
        val result = service.performScroll(RobotomatorAccessibilityService.ScrollDirection.DOWN)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) }
    }

    @Test
    fun `performScroll returns Success when scrolling up succeeds`() {
        // Given
        setupScrollableNodeTree()
        every {
            mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        } returns true

        // When
        val result = service.performScroll(RobotomatorAccessibilityService.ScrollDirection.UP)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) }
    }

    @Test
    fun `performScroll with selector scrolls specific element`() {
        // Given
        setupNodeTree(
            rootMatches = false,
            targetMatches = true,
            targetText = "ScrollView",
            targetScrollable = true
        )
        every {
            mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } returns true

        // When
        val result = service.performScroll(
            RobotomatorAccessibilityService.ScrollDirection.DOWN,
            RobotomatorAccessibilityService.ElementSelector(text = "ScrollView")
        )

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.Success)
        verify { mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) }
    }

    @Test
    fun `performScroll returns ElementNotFound when no scrollable element exists`() {
        // Given
        every { mockRootNode.isScrollable } returns false
        every { mockRootNode.childCount } returns 0
        every { mockRootNode.recycle() } just Runs

        // When
        val result = service.performScroll(RobotomatorAccessibilityService.ScrollDirection.DOWN)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ElementNotFound)
    }

    @Test
    fun `performScroll returns ActionFailed when scroll returns false`() {
        // Given
        setupScrollableNodeTree()
        every {
            mockTargetNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        } returns false

        // When
        val result = service.performScroll(RobotomatorAccessibilityService.ScrollDirection.DOWN)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ActionFailed)
    }

    @Test
    fun `performScroll returns ServiceNotConnected when service not connected`() {
        // Given
        setServiceConnected(false)

        // When
        val result = service.performScroll(RobotomatorAccessibilityService.ScrollDirection.DOWN)

        // Then
        assertTrue(result is RobotomatorAccessibilityService.InteractionResult.ServiceNotConnected)
    }

    // ===== ScrollDirection Tests =====

    @Test
    fun `ScrollDirection enum has all expected values`() {
        val directions = RobotomatorAccessibilityService.ScrollDirection.values()

        assertEquals(6, directions.size)
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.UP))
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.DOWN))
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.LEFT))
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.RIGHT))
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.FORWARD))
        assertTrue(directions.contains(RobotomatorAccessibilityService.ScrollDirection.BACKWARD))
    }

    // ===== Helper Methods =====

    private fun setServiceConnected(connected: Boolean) {
        val instanceRefField = RobotomatorAccessibilityService::class.java
            .getDeclaredField("instanceRef")
            .apply { isAccessible = true }
        val instanceRef = instanceRefField.get(null) as java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>
        instanceRef.set(if (connected) service else null)
    }

    private fun setupNodeTree(
        rootMatches: Boolean,
        targetMatches: Boolean,
        targetText: String? = null,
        targetResourceId: String? = null,
        targetClassName: String? = null,
        targetContentDescription: String? = null,
        targetScrollable: Boolean = false
    ) {
        // Setup root node
        every { mockRootNode.text } returns if (rootMatches && targetText != null) targetText else "Root"
        every { mockRootNode.viewIdResourceName } returns if (rootMatches && targetResourceId != null) {
            targetResourceId
        } else "root_id"
        every { mockRootNode.className } returns if (rootMatches && targetClassName != null) {
            targetClassName
        } else "android.view.ViewGroup"
        every { mockRootNode.contentDescription } returns if (rootMatches && targetContentDescription != null) {
            targetContentDescription
        } else "Root view"
        every { mockRootNode.childCount } returns if (targetMatches) 1 else 0
        every { mockRootNode.getChild(0) } returns if (targetMatches) mockTargetNode else null
        every { mockRootNode.recycle() } just Runs
        every { mockRootNode.isScrollable } returns false

        if (targetMatches) {
            // Setup target node
            every { mockTargetNode.text } returns targetText
            every { mockTargetNode.viewIdResourceName } returns targetResourceId
            every { mockTargetNode.className } returns targetClassName
            every { mockTargetNode.contentDescription } returns targetContentDescription
            every { mockTargetNode.childCount } returns 0
            every { mockTargetNode.recycle() } just Runs
            every { mockTargetNode.isScrollable } returns targetScrollable
        }
    }

    private fun setupScrollableNodeTree() {
        every { mockRootNode.isScrollable } returns false
        every { mockRootNode.childCount } returns 1
        every { mockRootNode.getChild(0) } returns mockTargetNode
        every { mockRootNode.recycle() } just Runs

        every { mockTargetNode.isScrollable } returns true
        every { mockTargetNode.childCount } returns 0
        every { mockTargetNode.recycle() } just Runs
    }
}
