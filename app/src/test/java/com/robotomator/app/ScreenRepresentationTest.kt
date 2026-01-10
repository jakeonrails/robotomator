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

    @Test
    fun testHierarchicalTextSimpleElement() {
        // Test a simple single element screen
        val element = ScreenElement(
            text = "Click me",
            contentDescription = null,
            className = "android.widget.Button",
            viewIdResourceName = "com.example:id/submit",
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
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

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = "com.example.app.MainActivity",
            rootElement = element,
            timestamp = 12345L
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify header
        assertTrue(hierarchicalText.contains("App: com.example.app (com.example.app.MainActivity)"))
        assertTrue(hierarchicalText.contains("Window: APPLICATION"))

        // Verify element representation
        assertTrue(hierarchicalText.contains("[0] Button"))
        assertTrue(hierarchicalText.contains("text='Click me'"))
        assertTrue(hierarchicalText.contains("id='com.example:id/submit'"))
        assertTrue(hierarchicalText.contains("[clickable]"))
    }

    @Test
    fun testHierarchicalTextNestedElements() {
        // Create a nested structure:
        // Root (View)
        //   ├─ Child1 (Button)
        //   └─ Child2 (EditText)
        val child1 = ScreenElement(
            text = "Submit",
            contentDescription = null,
            className = "android.widget.Button",
            viewIdResourceName = "com.example:id/submit",
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 1
        )

        val child2 = ScreenElement(
            text = null,
            contentDescription = "Email input",
            className = "android.widget.EditText",
            viewIdResourceName = "com.example:id/email",
            bounds = Rect(0, 60, 200, 100),
            isClickable = true,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = true,
            isFocusable = true,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 1
        )

        val root = ScreenElement(
            text = null,
            contentDescription = null,
            className = "android.view.View",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 400, 400),
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = listOf(child1, child2),
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify structure
        assertTrue(hierarchicalText.contains("[0] View"))
        assertTrue(hierarchicalText.contains("[0.0] Button"))
        assertTrue(hierarchicalText.contains("[0.1] EditText"))

        // Verify properties
        assertTrue(hierarchicalText.contains("text='Submit'"))
        assertTrue(hierarchicalText.contains("desc='Email input'"))
        assertTrue(hierarchicalText.contains("id='com.example:id/submit'"))
        assertTrue(hierarchicalText.contains("id='com.example:id/email'"))

        // Verify indentation (child elements should have leading spaces)
        val lines = hierarchicalText.lines()
        val buttonLine = lines.first { it.contains("[0.0] Button") }
        assertTrue(buttonLine.startsWith("  ")) // Should be indented

        val editTextLine = lines.first { it.contains("[0.1] EditText") }
        assertTrue(editTextLine.startsWith("  ")) // Should be indented
    }

    @Test
    fun testHierarchicalTextDeeplyNested() {
        // Create a 3-level deep tree
        val grandchild = ScreenElement(
            text = "Leaf",
            contentDescription = null,
            className = "android.widget.TextView",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 50, 20),
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
            depth = 2
        )

        val child = ScreenElement(
            text = "Middle",
            contentDescription = null,
            className = "android.widget.LinearLayout",
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
            children = listOf(grandchild),
            depth = 1
        )

        val root = ScreenElement(
            text = "Root",
            contentDescription = null,
            className = "android.widget.FrameLayout",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 400, 400),
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = true,
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = listOf(child),
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify nested index paths
        assertTrue(hierarchicalText.contains("[0] FrameLayout"))
        assertTrue(hierarchicalText.contains("[0.0] LinearLayout"))
        assertTrue(hierarchicalText.contains("[0.0.0] TextView"))

        // Verify indentation levels
        val lines = hierarchicalText.lines()
        val rootLine = lines.first { it.contains("[0] FrameLayout") }
        val childLine = lines.first { it.contains("[0.0] LinearLayout") }
        val grandchildLine = lines.first { it.contains("[0.0.0] TextView") }

        // Count leading spaces
        assertFalse(rootLine.startsWith(" ")) // No indentation
        assertTrue(childLine.startsWith("  ")) // 2 spaces
        assertTrue(grandchildLine.startsWith("    ")) // 4 spaces
    }

    @Test
    fun testHierarchicalTextPasswordField() {
        // Password fields should be marked appropriately
        val passwordElement = ScreenElement(
            text = null,
            contentDescription = "Password",
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
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = passwordElement,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify password attribute is shown
        assertTrue(hierarchicalText.contains("password"))
        assertTrue(hierarchicalText.contains("editable"))
        // Verify attributes are in brackets
        assertTrue(hierarchicalText.contains("[") && hierarchicalText.contains("]"))
    }

    @Test
    fun testHierarchicalTextDisabledElement() {
        val disabledElement = ScreenElement(
            text = "Disabled Button",
            contentDescription = null,
            className = "android.widget.Button",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 100, 50),
            isClickable = false,
            isCheckable = false,
            isChecked = false,
            isEnabled = false, // Disabled
            isScrollable = false,
            isEditable = false,
            isFocusable = false,
            isFocused = false,
            isPassword = false,
            children = emptyList(),
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = disabledElement,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify disabled attribute is shown
        assertTrue(hierarchicalText.contains("disabled"))
    }

    @Test
    fun testHierarchicalTextAllAttributes() {
        // Test an element with all attributes
        val element = ScreenElement(
            text = "Complex Element",
            contentDescription = "Description",
            className = "android.widget.CheckBox",
            viewIdResourceName = "com.example:id/checkbox",
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
            isCheckable = true,
            isChecked = true,
            isEnabled = true,
            isScrollable = true,
            isEditable = false,
            isFocusable = true,
            isFocused = true,
            isPassword = false,
            children = emptyList(),
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify all attributes are present
        assertTrue(hierarchicalText.contains("clickable"))
        assertTrue(hierarchicalText.contains("checkable"))
        assertTrue(hierarchicalText.contains("checked"))
        assertTrue(hierarchicalText.contains("scrollable"))
        assertTrue(hierarchicalText.contains("focused"))
    }

    @Test
    fun testHierarchicalTextNoPackageName() {
        // Test handling when package name is null
        val element = createSimpleElement(depth = 0)
        val screen = ScreenRepresentation(
            windowType = WindowType.SYSTEM,
            packageName = null,
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Should not crash and should show window type
        assertTrue(hierarchicalText.contains("Window: SYSTEM"))
        assertFalse(hierarchicalText.contains("App:"))
    }

    @Test
    fun testHierarchicalTextSimplifiedClassNames() {
        // Test that class names are simplified (last segment only)
        val element = ScreenElement(
            text = "Test",
            contentDescription = null,
            className = "android.widget.Button",
            viewIdResourceName = null,
            bounds = Rect(0, 0, 100, 50),
            isClickable = true,
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

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Should show "Button" not "android.widget.Button"
        assertTrue(hierarchicalText.contains("[0] Button"))
        assertFalse(hierarchicalText.contains("android.widget.Button"))
    }

    @Test
    fun testHierarchicalTextEmptyTextFiltered() {
        // Test that empty strings are not included in output
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

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Should not contain empty text or desc attributes
        assertFalse(hierarchicalText.contains("text=''"))
        assertFalse(hierarchicalText.contains("desc=''"))
    }

    @Test
    fun testHierarchicalTextMultipleSiblings() {
        // Test a parent with multiple children at the same level
        val child1 = createSimpleElement(depth = 1, text = "Child 1")
        val child2 = createSimpleElement(depth = 1, text = "Child 2")
        val child3 = createSimpleElement(depth = 1, text = "Child 3")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2, child3))

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // Verify all siblings have correct indices
        assertTrue(hierarchicalText.contains("[0.0]"))
        assertTrue(hierarchicalText.contains("[0.1]"))
        assertTrue(hierarchicalText.contains("[0.2]"))

        // Verify texts
        assertTrue(hierarchicalText.contains("text='Child 1'"))
        assertTrue(hierarchicalText.contains("text='Child 2'"))
        assertTrue(hierarchicalText.contains("text='Child 3'"))
    }

    @Test
    fun testHierarchicalTextPasswordSecurityNoTextLeakage() {
        // Security test: verify that password field text is NEVER included even if present
        val passwordElement = ScreenElement(
            text = "supersecretpassword123", // Should be filtered out
            contentDescription = "Password input",
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
            depth = 0
        )

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = passwordElement,
            timestamp = System.currentTimeMillis()
        )

        val hierarchicalText = screen.toHierarchicalText()

        // CRITICAL SECURITY CHECK: Password text must not appear in output
        assertFalse(hierarchicalText.contains("supersecretpassword123"))
        assertFalse(hierarchicalText.contains("text="))

        // But password attribute should still be present
        assertTrue(hierarchicalText.contains("password"))

        // Content description is OK (it's typically like "Password field")
        assertTrue(hierarchicalText.contains("desc='Password input'"))
    }

    @Test
    fun testHierarchicalTextMaxDepthProtection() {
        // Test that deeply nested trees don't cause stack overflow
        // Create a pathologically deep tree (200 levels) that exceeds maxDepth (100)
        var deepestChild = createSimpleElement(depth = 200, text = "Level 200")
        for (i in 199 downTo 0) {
            deepestChild = createSimpleElement(depth = i, text = "Level $i", children = listOf(deepestChild))
        }

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = deepestChild,
            timestamp = System.currentTimeMillis()
        )

        // Should not throw StackOverflowError
        val hierarchicalText = screen.toHierarchicalText()

        // Should contain the max depth warning message
        assertTrue(hierarchicalText.contains("max depth reached"))

        // Should contain early levels
        assertTrue(hierarchicalText.contains("text='Level 0'"))

        // Should NOT contain the deepest levels (they're beyond maxDepth)
        assertFalse(hierarchicalText.contains("text='Level 200'"))
    }

    @Test
    fun testElementAddressParsing() {
        // Test valid address strings
        val address1 = ElementAddress.parse("0")
        assertNotNull(address1)
        assertEquals(listOf(0), address1?.path)

        val address2 = ElementAddress.parse("0.1.2")
        assertNotNull(address2)
        assertEquals(listOf(0, 1, 2), address2?.path)

        val address3 = ElementAddress.parse("0.0.0.0")
        assertNotNull(address3)
        assertEquals(listOf(0, 0, 0, 0), address3?.path)
    }

    @Test
    fun testElementAddressParsingInvalidFormat() {
        // Test invalid formats
        assertNull(ElementAddress.parse(""))
        assertNull(ElementAddress.parse("abc"))
        assertNull(ElementAddress.parse("0.1.x"))
        assertNull(ElementAddress.parse("0..1"))
        assertNull(ElementAddress.parse(".0.1"))
    }

    @Test
    fun testElementAddressParsingNegativeIndices() {
        // Negative indices should be rejected
        assertNull(ElementAddress.parse("-1"))
        assertNull(ElementAddress.parse("0.-1.2"))
    }

    @Test
    fun testElementAddressToString() {
        val address = ElementAddress(listOf(0, 1, 2))
        assertEquals("0.1.2", address.toString())

        val rootAddress = ElementAddress(listOf(0))
        assertEquals("0", rootAddress.toString())
    }

    @Test
    fun testElementAddressRoot() {
        assertEquals(listOf(0), ElementAddress.ROOT.path)
        assertEquals("0", ElementAddress.ROOT.toString())
    }

    @Test
    fun testFindElementByAddressRoot() {
        // Test finding the root element
        val element = createSimpleElement(depth = 0, text = "Root")
        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        val result = screen.findElement(ElementAddress.ROOT)
        assertTrue(result is ElementLookupResult.Found)
        val found = result as ElementLookupResult.Found
        assertEquals("Root", found.element.text)
        assertEquals("0", found.address.toString())
    }

    @Test
    fun testFindElementByAddressString() {
        // Test finding element using string address
        val child1 = createSimpleElement(depth = 1, text = "Child 1")
        val child2 = createSimpleElement(depth = 1, text = "Child 2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Find first child
        val result1 = screen.findElement("0.0")
        assertTrue(result1 is ElementLookupResult.Found)
        assertEquals("Child 1", (result1 as ElementLookupResult.Found).element.text)

        // Find second child
        val result2 = screen.findElement("0.1")
        assertTrue(result2 is ElementLookupResult.Found)
        assertEquals("Child 2", (result2 as ElementLookupResult.Found).element.text)
    }

    @Test
    fun testFindElementByAddressNested() {
        // Test finding deeply nested elements
        val grandchild1 = createSimpleElement(depth = 2, text = "GC1")
        val grandchild2 = createSimpleElement(depth = 2, text = "GC2")
        val child1 = createSimpleElement(depth = 1, text = "C1", children = listOf(grandchild1, grandchild2))
        val child2 = createSimpleElement(depth = 1, text = "C2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Find first grandchild
        val result1 = screen.findElement("0.0.0")
        assertTrue(result1 is ElementLookupResult.Found)
        assertEquals("GC1", (result1 as ElementLookupResult.Found).element.text)

        // Find second grandchild
        val result2 = screen.findElement("0.0.1")
        assertTrue(result2 is ElementLookupResult.Found)
        assertEquals("GC2", (result2 as ElementLookupResult.Found).element.text)

        // Find second child (no grandchildren)
        val result3 = screen.findElement("0.1")
        assertTrue(result3 is ElementLookupResult.Found)
        assertEquals("C2", (result3 as ElementLookupResult.Found).element.text)
    }

    @Test
    fun testFindElementInvalidPath() {
        // Test when path goes beyond tree depth
        val child = createSimpleElement(depth = 1, text = "Child")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child))

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Try to access child that doesn't exist
        val result1 = screen.findElement("0.1")
        assertTrue(result1 is ElementLookupResult.InvalidPath)
        val invalidPath1 = result1 as ElementLookupResult.InvalidPath
        assertEquals("0.1", invalidPath1.address.toString())
        assertEquals(listOf(0), invalidPath1.failedAt)

        // Try to access grandchild when there are no grandchildren
        val result2 = screen.findElement("0.0.0")
        assertTrue(result2 is ElementLookupResult.InvalidPath)
        val invalidPath2 = result2 as ElementLookupResult.InvalidPath
        assertEquals("0.0.0", invalidPath2.address.toString())
        assertEquals(listOf(0, 0), invalidPath2.failedAt)
    }

    @Test
    fun testFindElementInvalidFormat() {
        val element = createSimpleElement(depth = 0)
        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = element,
            timestamp = System.currentTimeMillis()
        )

        // Test invalid address strings
        val result1 = screen.findElement("abc")
        assertTrue(result1 is ElementLookupResult.InvalidFormat)
        assertEquals("abc", (result1 as ElementLookupResult.InvalidFormat).addressString)

        val result2 = screen.findElement("")
        assertTrue(result2 is ElementLookupResult.InvalidFormat)

        val result3 = screen.findElement("0.x.2")
        assertTrue(result3 is ElementLookupResult.InvalidFormat)
    }

    @Test
    fun testGetAddressRoot() {
        // Test getting address of root element
        val element = createSimpleElement(depth = 0, text = "Root")

        val address = element.getAddress()
        assertNotNull(address)
        assertEquals("0", address.toString())
    }

    @Test
    fun testGetAddressChildren() {
        // Test getting address of child elements
        val child1 = createSimpleElement(depth = 1, text = "Child 1")
        val child2 = createSimpleElement(depth = 1, text = "Child 2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val address1 = child1.getAddress(root)
        assertNotNull(address1)
        assertEquals("0.0", address1.toString())

        val address2 = child2.getAddress(root)
        assertNotNull(address2)
        assertEquals("0.1", address2.toString())
    }

    @Test
    fun testGetAddressNested() {
        // Test getting address of deeply nested elements
        val grandchild1 = createSimpleElement(depth = 2, text = "GC1")
        val grandchild2 = createSimpleElement(depth = 2, text = "GC2")
        val child1 = createSimpleElement(depth = 1, text = "C1", children = listOf(grandchild1, grandchild2))
        val child2 = createSimpleElement(depth = 1, text = "C2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val address1 = grandchild1.getAddress(root)
        assertNotNull(address1)
        assertEquals("0.0.0", address1.toString())

        val address2 = grandchild2.getAddress(root)
        assertNotNull(address2)
        assertEquals("0.0.1", address2.toString())

        val address3 = child2.getAddress(root)
        assertNotNull(address3)
        assertEquals("0.1", address3.toString())
    }

    @Test
    fun testGetAddressNotInTree() {
        // Test when element is not in the provided root's tree
        val orphan = createSimpleElement(depth = 1, text = "Orphan")
        val root = createSimpleElement(depth = 0, text = "Root")

        val address = orphan.getAddress(root)
        assertNull(address)
    }

    @Test
    fun testFindElementRoundTrip() {
        // Test that we can find an element, get its address, and find it again
        val child1 = createSimpleElement(depth = 1, text = "Child 1")
        val child2 = createSimpleElement(depth = 1, text = "Child 2")
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Find child2
        val result1 = screen.findElement("0.1")
        assertTrue(result1 is ElementLookupResult.Found)
        val foundElement = (result1 as ElementLookupResult.Found).element

        // Get its address
        val address = foundElement.getAddress(root)
        assertNotNull(address)
        assertEquals("0.1", address.toString())

        // Find it again using that address
        val result2 = screen.findElement(address!!)
        assertTrue(result2 is ElementLookupResult.Found)
        assertEquals("Child 2", (result2 as ElementLookupResult.Found).element.text)
    }

    @Test
    fun testElementAddressEquality() {
        val address1 = ElementAddress(listOf(0, 1, 2))
        val address2 = ElementAddress(listOf(0, 1, 2))
        val address3 = ElementAddress(listOf(0, 1, 3))

        // Test equality
        assertEquals(address1, address2)
        assertNotEquals(address1, address3)

        // Test hash code
        assertEquals(address1.hashCode(), address2.hashCode())
    }

    @Test
    fun testFindElementByAddressComplex() {
        // Test a more complex tree structure
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

        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Test all valid paths
        val tests = mapOf(
            "0" to "Root",
            "0.0" to "C1",
            "0.1" to "C2",
            "0.0.0" to "GC1",
            "0.0.1" to "GC2",
            "0.1.0" to "GC3"
        )

        tests.forEach { (addressString, expectedText) ->
            val result = screen.findElement(addressString)
            assertTrue("Failed to find $addressString", result is ElementLookupResult.Found)
            assertEquals(
                "Wrong element for $addressString",
                expectedText,
                (result as ElementLookupResult.Found).element.text
            )
        }

        // Test invalid paths
        val invalidPaths = listOf("0.2", "0.0.2", "0.1.1", "0.0.0.0")
        invalidPaths.forEach { addressString ->
            val result = screen.findElement(addressString)
            assertTrue("$addressString should be invalid", result is ElementLookupResult.InvalidPath)
        }
    }

    @Test
    fun testGetAddressComplex() {
        // Test getting addresses for all elements in a complex tree
        val grandchild1 = createSimpleElement(depth = 2, text = "GC1")
        val grandchild2 = createSimpleElement(depth = 2, text = "GC2")
        val grandchild3 = createSimpleElement(depth = 2, text = "GC3")
        val child1 = createSimpleElement(depth = 1, text = "C1", children = listOf(grandchild1, grandchild2))
        val child2 = createSimpleElement(depth = 1, text = "C2", children = listOf(grandchild3))
        val root = createSimpleElement(depth = 0, text = "Root", children = listOf(child1, child2))

        // Test all elements
        assertEquals("0", root.getAddress(root).toString())
        assertEquals("0.0", child1.getAddress(root).toString())
        assertEquals("0.1", child2.getAddress(root).toString())
        assertEquals("0.0.0", grandchild1.getAddress(root).toString())
        assertEquals("0.0.1", grandchild2.getAddress(root).toString())
        assertEquals("0.1.0", grandchild3.getAddress(root).toString())
    }

    @Test
    fun testFindElementDepthLimitProtection() {
        // Test that addresses with paths exceeding MAX_TREE_DEPTH are rejected
        val root = createSimpleElement(depth = 0, text = "Root")
        val screen = ScreenRepresentation(
            windowType = WindowType.APPLICATION,
            packageName = "com.example.app",
            activityName = null,
            rootElement = root,
            timestamp = System.currentTimeMillis()
        )

        // Create an address with 101 indices (exceeds MAX_TREE_DEPTH of 100)
        val deepPath = (0..100).toList()
        val deepAddress = ElementAddress(deepPath)

        val result = screen.findElement(deepAddress)
        assertTrue("Deep address should be rejected", result is ElementLookupResult.InvalidPath)
    }

    @Test
    fun testGetAddressDepthLimitProtection() {
        // Test that getAddress() handles pathologically deep trees gracefully
        // Build a chain of 105 elements (exceeds MAX_TREE_DEPTH of 100)
        var current = createSimpleElement(depth = 104, text = "Leaf")
        for (i in 103 downTo 0) {
            current = createSimpleElement(depth = i, text = "Node$i", children = listOf(current))
        }
        val root = current

        // Navigate deep into the tree to get the leaf
        var deepNode = root
        for (i in 0 until 105) {
            if (deepNode.children.isEmpty()) break
            deepNode = deepNode.children[0]
        }

        // Try to get address of the deep element (should fail gracefully due to depth limit)
        val address = deepNode.getAddress(root)

        // Should return null since depth limit is exceeded
        assertNull("Address lookup should fail gracefully for deep trees", address)
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
