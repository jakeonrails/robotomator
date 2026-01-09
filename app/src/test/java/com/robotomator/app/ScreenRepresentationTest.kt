package com.robotomator.app

import android.graphics.Rect
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ScreenRepresentation data structures.
 *
 * Tests the immutable data classes used to represent screen content,
 * which are created by the accessibility service when reading screens.
 */
class ScreenRepresentationTest {

    @Test
    fun testScreenElementCreation() {
        // Create a simple screen element
        val element = ScreenElement(
            text = "Hello",
            contentDescription = "Greeting",
            className = "android.widget.TextView",
            viewIdResourceName = "com.example:id/greeting",
            bounds = Rect(0, 0, 100, 50),
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 0
        )

        assertEquals("Hello", element.text)
        assertEquals("Greeting", element.contentDescription)
        assertEquals("android.widget.TextView", element.className)
        assertEquals("com.example:id/greeting", element.viewIdResourceName)
        assertTrue(element.isEnabled)
        assertFalse(element.isClickable)
        assertEquals(0, element.depth)
    }

    @Test
    fun testScreenElementDescribe() {
        val element = ScreenElement(
            text = "Click me",
            contentDescription = "Button",
            className = "android.widget.Button",
            viewIdResourceName = "com.example:id/btn",
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = true,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 1
        )

        val description = element.describe()
        assertTrue(description.contains("Click me"))
        assertTrue(description.contains("Button"))
        assertTrue(description.contains("com.example:id/btn"))
        assertTrue(description.contains("clickable"))
    }

    @Test
    fun testScreenElementTotalCount() {
        // Create a tree: root with 2 children, first child has 1 child
        val grandchild = createSimpleElement(depth = 2)
        val child1 = createSimpleElement(depth = 1, children = listOf(grandchild))
        val child2 = createSimpleElement(depth = 1)
        val root = createSimpleElement(depth = 0, children = listOf(child1, child2))

        // Total: root + child1 + child2 + grandchild = 4
        assertEquals(4, root.totalElementCount())
    }

    @Test
    fun testScreenElementFlatten() {
        // Create a tree: root with 2 children
        val child1 = createSimpleElement(depth = 1, text = "Child 1")
        val child2 = createSimpleElement(depth = 1, text = "Child 2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val flattened = root.flatten()
        assertEquals(3, flattened.size)
        assertEquals("Root", flattened[0].text)
        assertEquals("Child 1", flattened[1].text)
        assertEquals("Child 2", flattened[2].text)
    }

    @Test
    fun testScreenRepresentationCreation() {
        val element = createSimpleElement(depth = 0)
        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = "com.example.app.MainActivity",
            rootElement = element,
            timestamp = 12345L
        )

        assertEquals(WindowType.APPLICATION, screen.windowType)
        assertEquals("com.example.app", screen.packageName)
        assertEquals("com.example.app.MainActivity", screen.activityName)
        assertEquals(12345L, screen.timestamp)
        assertNotNull(screen.rootElement)
    }

    @Test
    fun testScreenElementPasswordField() {
        val passwordElement = ScreenElement(
            text = null, // Password text should be null
            contentDescription = "Password field",
            className = "android.widget.EditText",
            viewIdResourceName = "com.example:id/password",
            bounds = Rect(0, 0, 200, 50),
            isClickable = true,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = true,
            isFocusable = true,
            isFocused = false,
            isPassword = true,
            children = emptyList(),
            depth = 1
        )

        assertTrue(passwordElement.isPassword)
        assertTrue(passwordElement.isEditable)
        assertNull(passwordElement.text)

        val description = passwordElement.describe()
        assertTrue(description.contains("password"))
    }

    @Test
    fun testScreenElementDisabledState() {
        val disabledElement = createSimpleElement(depth = 0, isEnabled = false)

        assertFalse(disabledElement.isEnabled)
        val description = disabledElement.describe()
        assertTrue(description.contains("disabled"))
    }

    @Test
    fun testWindowTypeEnum() {
        // Verify all window types are available
        assertEquals(WindowType.APPLICATION, WindowType.valueOf("APPLICATION"))
        assertEquals(WindowType.SYSTEM, WindowType.valueOf("SYSTEM"))
        assertEquals(WindowType.INPUT_METHOD, WindowType.valueOf("INPUT_METHOD"))
        assertEquals(WindowType.UNKNOWN, WindowType.valueOf("UNKNOWN"))
    }

    @Test
    fun testScreenReadResultSealed() {
        // Test the sealed class hierarchy
        val success: ScreenReadResult = ScreenReadResult.Success(
            ScreenRepresentation(
                windowType = WindowType.APPLICATION,
                packageName = "com.example",
                activityName = null,
                rootElement = createSimpleElement(depth = 0),
                timestamp = System.currentTimeMillis()
            )
        )

        val noWindow: ScreenReadResult = ScreenReadResult.NoActiveWindow
        val notConnected: ScreenReadResult = ScreenReadResult.ServiceNotConnected
        val error: ScreenReadResult = ScreenReadResult.Error("Test error")

        // Verify we can check types
        assertTrue(success is ScreenReadResult.Success)
        assertTrue(noWindow is ScreenReadResult.NoActiveWindow)
        assertTrue(notConnected is ScreenReadResult.ServiceNotConnected)
        assertTrue(error is ScreenReadResult.Error)

        // Test error message
        assertEquals("Test error", (error as ScreenReadResult.Error).message)
    }

    @Test
    fun testScreenElementDepth() {
        // Verify depth tracking
        val root = createSimpleElement(depth = 0)
        val child = createSimpleElement(depth = 1)
        val grandchild = createSimpleElement(depth = 2)

        assertEquals(0, root.depth)
        assertEquals(1, child.depth)
        assertEquals(2, grandchild.depth)
    }

    @Test
    fun testScreenElementBounds() {
        // Test that bounds are stored in the element
        val element = createSimpleElement(depth = 0)

        // Verify bounds object exists
        assertNotNull(element.bounds)
    }

    @Test
    fun testScreenElementDescribeWithEmptyStrings() {
        // Test that empty strings are filtered out
        val element = ScreenElement(
            text = "",
            contentDescription = "",
            className = "android.widget.View",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 100, 50),
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 0
        )

        val description = element.describe()
        // Empty text and contentDescription should not appear
        assertFalse(description.contains("text="))
        assertFalse(description.contains("desc="))
        assertTrue(description.contains("class='android.widget.View'"))
    }

    @Test
    fun testScreenElementDescribeWithAllAttributes() {
        // Test element with all attributes set
        val element = ScreenElement(
            text = "Test",
            contentDescription = "Description",
            className = "android.widget.Button",
            viewIdResourceName = "test:id/button",
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
            isCheckable = true,
            isChecked = true,
            isEnabled = false,  // Note: disabled
            isScrollable = true,
            isEditable = true,
            isFocusable = true,
            isFocused = true,
            isPassword = true,
            children = emptyList(),
            depth = 1
        )

        val description = element.describe()
        assertTrue(description.contains("clickable"))
        assertTrue(description.contains("checkable"))
        assertTrue(description.contains("checked"))
        assertTrue(description.contains("scrollable"))
        assertTrue(description.contains("editable"))
        assertTrue(description.contains("focused"))
        assertTrue(description.contains("password"))
        assertTrue(description.contains("disabled"))
    }

    @Test
    fun testScreenElementTotalCountWithDeepNesting() {
        // Create a deep tree to verify counting works at any depth
        var current = createSimpleElement(depth = 10, text = "Leaf")
        for (i in 9 downTo 0) {
            current = createSimpleElement(depth = i, text = "Level $i", children = listOf(current))
        }

        // Should count all 11 elements (depth 0 through 10)
        assertEquals(11, current.totalElementCount())
    }

    @Test
    fun testScreenElementFlattenWithComplexTree() {
        // Create a complex tree:
        //       root
        //      /    \
        //    c1      c2
        //   /  \      |
        // gc1  gc2   gc3
        val grandchild1 = createSimpleElement(depth = 2, text = "GC1")
        val grandchild2 = createSimpleElement(depth = 2, text = "GC2")
        val grandchild3 = createSimpleElement(depth = 2, text = "GC3")
        val child1 = createSimpleElement(depth = 1, text = "C1", children = listOf(grandchild1, grandchild2))
        val child2 = createSimpleElement(depth = 1, text = "C2", children = listOf(grandchild3))
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val flattened = root.flatten()
        assertEquals(6, flattened.size)

        // Verify depth-first order
        assertEquals("Root", flattened[0].text)
        assertEquals("C1", flattened[1].text)
        assertEquals("GC1", flattened[2].text)
        assertEquals("GC2", flattened[3].text)
        assertEquals("C2", flattened[4].text)
        assertEquals("GC3", flattened[5].text)
    }

    @Test
    fun testScreenElementWithNullText() {
        val element = createSimpleElement(depth = 0, text = null)
        assertNull(element.text)

        // describe() should handle null text gracefully
        val description = element.describe()
        assertFalse(description.contains("null"))
    }

    @Test
    fun testScreenRepresentationWithNullFields() {
        // Test that ScreenRepresentation can handle null packageName and activityName
        val element = createSimpleElement(depth = 0)
        val screen = ScreenRepresentation(
            windowType = WindowType.UNKNOWN,
            packageName = null,
            activityName = null,
            rootElement = element,
            timestamp = 12345L
        )

        assertNull(screen.packageName)
        assertNull(screen.activityName)
        assertEquals(WindowType.UNKNOWN, screen.windowType)
    }

    @Test
    fun testScreenElementSingleChild() {
        val child = createSimpleElement(depth = 1)
        val parent = createSimpleElement(depth = 0, children = listOf(child))

        assertEquals(2, parent.totalElementCount())
        assertEquals(2, parent.flatten().size)
    }

    @Test
    fun testScreenElementNoChildren() {
        val element = createSimpleElement(depth = 0)

        assertEquals(1, element.totalElementCount())
        assertEquals(1, element.flatten().size)
        assertTrue(element.children.isEmpty())
    }

    // Helper function to create simple test elements
    private fun createSimpleElement(
        depth: Int,
        text: String? = "Test",
        children: List<ScreenElement> = emptyList(),
        isEnabled: Boolean = true,
        bounds: Rect = Rect(0, 0, 100, 50)
    ): ScreenElement {
        return ScreenElement(
            text = text,
            contentDescription = null,
            className = "android.widget.View",
            viewIdResourceName = null,
            bounds = bounds,
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = isEnabled,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = children,
            depth = depth
        )
    }
}
