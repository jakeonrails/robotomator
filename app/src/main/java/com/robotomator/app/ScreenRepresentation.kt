package com.robotomator.app

import android.graphics.Rect

/**
 * Represents the content of a screen at a point in time.
 *
 * This is the primary data structure for capturing what the accessibility service
 * sees when reading screen content.
 *
 * @property windowType The type of window being represented
 * @property packageName The package name of the app displaying this screen
 * @property activityName The activity name if available
 * @property rootElement The root element of the accessibility tree
 * @property timestamp The time when this representation was captured
 */
data class ScreenRepresentation(
    val windowType: WindowType,
    val packageName: String?,
    val activityName: String?,
    val rootElement: ScreenElement,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Types of windows that can be represented.
 */
enum class WindowType {
    /** Regular application window */
    APPLICATION,

    /** System window (dialogs, toasts, etc.) */
    SYSTEM,

    /** Input method window (keyboard) */
    INPUT_METHOD,

    /** Unknown window type */
    UNKNOWN
}

/**
 * Represents a single UI element in the accessibility tree.
 *
 * This class captures all the relevant properties of an AccessibilityNodeInfo
 * in a memory-safe way (no need to recycle).
 *
 * @property text The text content of the element
 * @property contentDescription The content description for accessibility
 * @property className The class name of the view
 * @property viewIdResourceName The resource ID (e.g., "com.example:id/button")
 * @property bounds The screen bounds of the element
 * @property isClickable Whether the element is clickable
 * @property isCheckable Whether the element can be checked/unchecked
 * @property isChecked Whether the element is currently checked
 * @property isEnabled Whether the element is enabled
 * @property isScrollable Whether the element is scrollable
 * @property isEditable Whether the element accepts text input
 * @property isFocusable Whether the element can receive focus
 * @property isFocused Whether the element currently has focus
 * @property isPassword Whether the element is a password field
 * @property children The child elements
 * @property depth The depth in the tree (0 for root)
 */
data class ScreenElement(
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val viewIdResourceName: String?,
    val bounds: Rect,
    val isClickable: Boolean,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val isEnabled: Boolean,
    val isScrollable: Boolean,
    val isEditable: Boolean,
    val isFocusable: Boolean,
    val isFocused: Boolean,
    val isPassword: Boolean,
    val children: List<ScreenElement>,
    val depth: Int
) {
    /**
     * Returns a human-readable description of this element.
     */
    fun describe(): String {
        val parts = mutableListOf<String>()

        text?.takeIf { it.isNotBlank() }?.let { parts.add("text='$it'") }
        contentDescription?.takeIf { it.isNotBlank() }?.let { parts.add("desc='$it'") }
        viewIdResourceName?.let { parts.add("id='$it'") }
        className?.let { parts.add("class='$it'") }

        val attributes = mutableListOf<String>()
        if (isClickable) attributes.add("clickable")
        if (isCheckable) attributes.add("checkable")
        if (isChecked) attributes.add("checked")
        if (isScrollable) attributes.add("scrollable")
        if (isEditable) attributes.add("editable")
        if (isFocused) attributes.add("focused")
        if (isPassword) attributes.add("password")
        if (!isEnabled) attributes.add("disabled")

        if (attributes.isNotEmpty()) {
            parts.add("[${attributes.joinToString(", ")}]")
        }

        return parts.joinToString(" ")
    }

    /**
     * Returns the total number of elements in this tree (including this element).
     */
    fun totalElementCount(): Int {
        return 1 + children.sumOf { it.totalElementCount() }
    }

    /**
     * Flattens the tree into a list of elements in depth-first order.
     */
    fun flatten(): List<ScreenElement> {
        return listOf(this) + children.flatMap { it.flatten() }
    }
}

/**
 * Result of attempting to read screen content.
 */
sealed class ScreenReadResult {
    /** Screen was successfully read */
    data class Success(val screen: ScreenRepresentation) : ScreenReadResult()

    /** No active window available */
    object NoActiveWindow : ScreenReadResult()

    /** Service not connected */
    object ServiceNotConnected : ScreenReadResult()

    /** Error occurred while reading */
    data class Error(val message: String) : ScreenReadResult()
}

/**
 * Converts this screen representation to a hierarchical text format optimized for LLM consumption.
 *
 * The format uses indentation to show hierarchy and includes key element properties
 * in a concise, parseable format. This representation is designed to:
 * - Be easily understood by language models
 * - Minimize token usage while preserving important information
 * - Show the logical structure of the UI hierarchy
 *
 * Example output:
 * ```
 * App: com.example.app (MainActivity)
 * Window: APPLICATION
 *
 * [0] View
 *   [1] Button text='Submit' id='com.example:id/submit' [clickable, enabled]
 *   [2] EditText desc='Password field' [editable, password, focusable]
 * ```
 *
 * @return A multi-line string representation of the screen hierarchy
 */
fun ScreenRepresentation.toHierarchicalText(): String {
    val builder = StringBuilder()

    // Add header information
    if (packageName != null) {
        builder.append("App: $packageName")
        if (activityName != null) {
            builder.append(" ($activityName)")
        }
        builder.append("\n")
    }

    builder.append("Window: $windowType\n")
    builder.append("\n")

    // Add the element tree
    rootElement.appendHierarchicalText(builder, indent = 0, indexPath = mutableListOf(0))

    return builder.toString()
}

/**
 * Appends this element and its children to the builder in hierarchical text format.
 *
 * Uses indentation to show nesting level. Each element is represented on its own line
 * with an index path (for addressing), followed by key properties.
 *
 * @param builder The StringBuilder to append to
 * @param indent The current indentation level (number of spaces = indent * 2)
 * @param indexPath The path of indices from root to this element
 * @param maxDepth Maximum depth to traverse (prevents stack overflow on pathological trees)
 */
private fun ScreenElement.appendHierarchicalText(
    builder: StringBuilder,
    indent: Int,
    indexPath: MutableList<Int>,
    maxDepth: Int = 100
) {
    // Depth limit protection against pathologically deep trees
    if (indent >= maxDepth) {
        repeat(indent) { builder.append("  ") }
        builder.append("[${indexPath.joinToString(".")}] ... (max depth reached)\n")
        return
    }
    // Add indentation
    repeat(indent) { builder.append("  ") }

    // Add index path for addressability
    builder.append("[${indexPath.joinToString(".")}] ")

    // Add class name (simplified)
    val simpleClassName = className?.substringAfterLast('.') ?: "View"
    builder.append(simpleClassName)

    // Add key identifying properties
    val properties = mutableListOf<String>()

    // Never include text content for password fields (security)
    text?.takeIf { it.isNotBlank() && !isPassword }?.let {
        properties.add("text='$it'")
    }

    contentDescription?.takeIf { it.isNotBlank() }?.let {
        properties.add("desc='$it'")
    }

    viewIdResourceName?.let {
        properties.add("id='$it'")
    }

    // Add properties inline
    if (properties.isNotEmpty()) {
        builder.append(" ")
        builder.append(properties.joinToString(" "))
    }

    // Add interaction attributes in brackets
    val attributes = mutableListOf<String>()
    if (isClickable) attributes.add("clickable")
    if (isCheckable) attributes.add("checkable")
    if (isChecked) attributes.add("checked")
    if (isScrollable) attributes.add("scrollable")
    if (isEditable) attributes.add("editable")
    if (isFocused) attributes.add("focused")
    if (isPassword) attributes.add("password")
    if (!isEnabled) attributes.add("disabled")

    if (attributes.isNotEmpty()) {
        builder.append(" [${attributes.joinToString(", ")}]")
    }

    builder.append("\n")

    // Recursively add children with updated index path
    children.forEachIndexed { index, child ->
        indexPath.add(index)
        child.appendHierarchicalText(builder, indent + 1, indexPath, maxDepth)
        indexPath.removeAt(indexPath.size - 1)
    }
}
