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
 * Maximum depth for tree traversal operations to prevent stack overflow.
 * Android UI hierarchies rarely exceed 20-30 levels; 100 provides ample safety margin.
 */
private const val MAX_TREE_DEPTH = 100

/**
 * Represents the address of an element in the screen hierarchy.
 *
 * An element address is a dot-separated path of indices from the root element
 * to the target element, similar to an XPath but using simple integer indices.
 *
 * Examples:
 * - "0" = root element
 * - "0.1" = second child of root
 * - "0.1.2" = third child of second child of root
 *
 * @property path The list of indices from root to this element
 */
data class ElementAddress(val path: List<Int>) {
    /**
     * Returns the string representation of this address.
     * Example: [0, 1, 2] -> "0.1.2"
     */
    override fun toString(): String = path.joinToString(".")

    companion object {
        /**
         * Parses an address string into an ElementAddress.
         * Example: "0.1.2" -> ElementAddress([0, 1, 2])
         *
         * @param addressString The address string to parse
         * @return The parsed ElementAddress, or null if invalid format
         */
        fun parse(addressString: String): ElementAddress? {
            if (addressString.isBlank()) return null

            return try {
                val indices = addressString.split(".").map { it.toInt() }
                if (indices.any { it < 0 }) null else ElementAddress(indices)
            } catch (e: NumberFormatException) {
                null
            }
        }

        /**
         * Creates an ElementAddress for the root element.
         */
        val ROOT = ElementAddress(listOf(0))
    }
}

/**
 * Result of attempting to find an element by address.
 */
sealed class ElementLookupResult {
    /** Element was found successfully */
    data class Found(val element: ScreenElement, val address: ElementAddress) : ElementLookupResult()

    /** Address path is invalid (goes beyond tree depth) */
    data class InvalidPath(val address: ElementAddress, val failedAt: List<Int>) : ElementLookupResult()

    /** Address string could not be parsed */
    data class InvalidFormat(val addressString: String) : ElementLookupResult()
}

/**
 * Finds an element by its address in the screen hierarchy.
 *
 * @param address The address of the element to find
 * @return The lookup result indicating success or failure reason
 */
fun ScreenRepresentation.findElement(address: ElementAddress): ElementLookupResult {
    // Special handling: address path starts with 0 for the root element
    // We need to verify the first index is 0, then process the rest
    if (address.path.isEmpty()) {
        return ElementLookupResult.InvalidPath(address, emptyList())
    }

    if (address.path.first() != 0) {
        return ElementLookupResult.InvalidPath(address, emptyList())
    }

    // Depth limit protection - reject paths that are suspiciously deep
    if (address.path.size > MAX_TREE_DEPTH) {
        return ElementLookupResult.InvalidPath(address, emptyList())
    }

    // If the address is just "0", return the root element
    if (address.path.size == 1) {
        return ElementLookupResult.Found(rootElement, address)
    }

    // Otherwise, traverse from the root using the remaining path
    return rootElement.findByPath(address.path.drop(1), address, listOf(0))
}

/**
 * Finds an element by its address string.
 *
 * @param addressString The address string to parse and look up (e.g., "0.1.2")
 * @return The lookup result indicating success or failure reason
 */
fun ScreenRepresentation.findElement(addressString: String): ElementLookupResult {
    val address = ElementAddress.parse(addressString)
        ?: return ElementLookupResult.InvalidFormat(addressString)
    return findElement(address)
}

/**
 * Internal helper to traverse the tree following the given path.
 *
 * @param path The remaining path to traverse (does NOT include the current element's index)
 * @param originalAddress The full original address for error reporting
 * @param currentPath The path we've successfully traversed so far
 */
private fun ScreenElement.findByPath(
    path: List<Int>,
    originalAddress: ElementAddress,
    currentPath: List<Int>
): ElementLookupResult {
    // Base case: empty path means we've arrived at the target
    if (path.isEmpty()) {
        return ElementLookupResult.Found(this, originalAddress)
    }

    val nextIndex = path.first()
    val remainingPath = path.drop(1)

    // Check if the index is valid for this element's children
    if (nextIndex >= children.size) {
        // Return the path we successfully traversed
        return ElementLookupResult.InvalidPath(originalAddress, currentPath)
    }

    // Recurse into the child
    val nextPath = currentPath + nextIndex
    return children[nextIndex].findByPath(remainingPath, originalAddress, nextPath)
}

/**
 * Returns the address of this element within its tree.
 *
 * Note: This requires the element to maintain its position in the hierarchy.
 * The address is computed based on the depth-first traversal order.
 *
 * @param rootElement The root element to compute path from (typically the screen root)
 * @return The element address, or null if this element is not in the given root's tree
 */
fun ScreenElement.getAddress(rootElement: ScreenElement = this): ElementAddress? {
    // If this element is the root, return root address
    if (this === rootElement) {
        return ElementAddress(listOf(0))
    }

    // Search for this element in the tree and build the path (with depth limit)
    return rootElement.findPath(this, mutableListOf(0), maxDepth = MAX_TREE_DEPTH)
}

/**
 * Internal helper to search for a target element and build its path.
 *
 * @param target The element to find
 * @param currentPath The path built so far
 * @param maxDepth Maximum depth to traverse (prevents stack overflow)
 */
private fun ScreenElement.findPath(
    target: ScreenElement,
    currentPath: MutableList<Int>,
    maxDepth: Int = MAX_TREE_DEPTH
): ElementAddress? {
    // Check if we found the target
    if (this === target) {
        return ElementAddress(currentPath.toList())
    }

    // Depth limit protection
    if (currentPath.size >= maxDepth) {
        return null
    }

    // Recurse into children
    children.forEachIndexed { index, child ->
        currentPath.add(index)
        val result = child.findPath(target, currentPath, maxDepth)
        if (result != null) {
            return result
        }
        currentPath.removeAt(currentPath.size - 1)
    }

    return null
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
    maxDepth: Int = MAX_TREE_DEPTH
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
