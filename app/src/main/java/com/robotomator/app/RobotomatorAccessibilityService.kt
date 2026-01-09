package com.robotomator.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Robotomator's core AccessibilityService implementation.
 *
 * This service registers with the Android system to receive accessibility events
 * and gain the ability to interact with UI elements across all apps.
 *
 * Key capabilities:
 * - Receive notifications when screens change (onAccessibilityEvent)
 * - Perform global actions (back, home, recents, etc.)
 * - Interact with UI elements (tap, type, scroll, etc.)
 * - Read screen content via accessibility node tree
 */
class RobotomatorAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "RobotomatorA11yService"

        /**
         * Maximum depth to traverse in the accessibility tree.
         * Prevents stack overflow from malformed or extremely deep trees.
         */
        private const val MAX_TREE_DEPTH = 50

        /**
         * Maximum length of text input to prevent DoS attacks.
         * 10,000 characters is sufficient for most legitimate use cases.
         */
        private const val MAX_TEXT_INPUT_LENGTH = 10_000

        /**
         * Atomic reference to the active service instance.
         * Used by other components to invoke actions on the service.
         * Thread-safe using AtomicReference for compare-and-swap operations.
         */
        private val instanceRef = java.util.concurrent.atomic.AtomicReference<RobotomatorAccessibilityService?>(null)

        /**
         * Gets the active service instance if connected.
         */
        fun getInstance(): RobotomatorAccessibilityService? = instanceRef.get()

        /**
         * Tracks whether the service is currently connected and active.
         * This can be checked by other components to determine if automation is available.
         */
        val isServiceConnected: Boolean
            get() = instanceRef.get() != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef.set(this)
        Log.i(TAG, "Robotomator Accessibility Service connected!")
        Log.i(TAG, "Service info: ${serviceInfo}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Log accessibility events for debugging (don't log event text to avoid PII exposure)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Accessibility event received: type=${event.eventType}, " +
                    "class=${event.className}, package=${event.packageName}")
        }

        // Notify listeners about this event
        notifyListeners(event)

        // TODO: Future iterations will:
        // - Build and cache screen representations
        // - Detect screen changes for script execution
        // - Monitor for unexpected modals/dialogs
        // - Feed events into the execution engine
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt the feedback this service is providing
        Log.w(TAG, "Service interrupted")
        // TODO: Future iterations will:
        // - Pause any running automations
        // - Notify execution engine of interruption
    }

    override fun onDestroy() {
        clearAllListeners()
        instanceRef.set(null)
        Log.i(TAG, "Robotomator Accessibility Service disconnected")
        super.onDestroy()

        // TODO: Future iterations will:
        // - Clean up any running automations
        // - Release cached screen representations
        // - Notify UI of service disconnection
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        clearAllListeners()
        instanceRef.set(null)
        Log.i(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    // ===== Global Actions API =====

    /**
     * Represents the available global actions that can be performed.
     */
    enum class GlobalAction(val actionId: Int) {
        /** Navigate back - equivalent to pressing the back button */
        BACK(GLOBAL_ACTION_BACK),

        /** Navigate to home screen - equivalent to pressing the home button */
        HOME(GLOBAL_ACTION_HOME),

        /** Open recent apps/multitasking view - equivalent to pressing recents button */
        RECENTS(GLOBAL_ACTION_RECENTS),

        /** Open notification shade - pull down notifications */
        NOTIFICATIONS(GLOBAL_ACTION_NOTIFICATIONS),

        /** Open quick settings panel */
        QUICK_SETTINGS(GLOBAL_ACTION_QUICK_SETTINGS),

        /** Open power dialog - long press power button equivalent */
        POWER_DIALOG(GLOBAL_ACTION_POWER_DIALOG);

        companion object {
            /**
             * Gets a GlobalAction by its actionId, or null if not found.
             */
            fun fromActionId(actionId: Int): GlobalAction? =
                values().find { it.actionId == actionId }
        }
    }

    /**
     * Result of attempting to perform a global action.
     */
    sealed class GlobalActionResult {
        /** Action was successfully performed */
        object Success : GlobalActionResult()

        /** Action failed - service not connected */
        object ServiceNotConnected : GlobalActionResult()

        /** Action failed - system denied the action */
        data class SystemDenied(val action: GlobalAction) : GlobalActionResult()

        /** Action failed - unknown error */
        data class Error(val action: GlobalAction, val message: String) : GlobalActionResult()
    }

    /**
     * Performs a global action.
     *
     * @param action The global action to perform
     * @return Result indicating success or failure
     */
    fun performGlobalAction(action: GlobalAction): GlobalActionResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot perform global action ${action.name}: Service not connected")
            return GlobalActionResult.ServiceNotConnected
        }

        return try {
            val success = performGlobalAction(action.actionId)
            if (success) {
                Log.d(TAG, "Successfully performed global action: ${action.name}")
                GlobalActionResult.Success
            } else {
                Log.w(TAG, "System denied global action: ${action.name}")
                GlobalActionResult.SystemDenied(action)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException performing global action ${action.name}: ${e.message}", e)
            GlobalActionResult.Error(action, e.message ?: "Service in invalid state")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException performing global action ${action.name}: ${e.message}", e)
            GlobalActionResult.Error(action, "Permission denied: ${e.message}")
        }
    }

    // ===== Element Interactions API =====

    /**
     * Result of attempting to find an element.
     */
    sealed class FindElementResult {
        /** Element was found */
        data class Found(val node: android.view.accessibility.AccessibilityNodeInfo) : FindElementResult()

        /** Element was not found */
        object NotFound : FindElementResult()

        /** Service not connected */
        object ServiceNotConnected : FindElementResult()

        /** Error occurred while searching */
        data class Error(val message: String) : FindElementResult()
    }

    /**
     * Simple element selector for finding UI elements.
     * In future iterations, this will be expanded to support CSS-like selectors.
     */
    data class ElementSelector(
        val text: String? = null,
        val resourceId: String? = null,
        val className: String? = null,
        val contentDescription: String? = null
    ) {
        init {
            require(text != null || resourceId != null || className != null || contentDescription != null) {
                "At least one selector criterion must be provided"
            }
        }
    }

    /**
     * Result of attempting to interact with an element.
     */
    sealed class InteractionResult {
        /** Interaction was successful */
        object Success : InteractionResult()

        /** Element not found */
        object ElementNotFound : InteractionResult()

        /** Element found but action failed */
        data class ActionFailed(val reason: String) : InteractionResult()

        /** Service not connected */
        object ServiceNotConnected : InteractionResult()

        /** Error occurred */
        data class Error(val message: String) : InteractionResult()
    }

    /**
     * Scroll direction for scroll operations.
     */
    enum class ScrollDirection {
        UP, DOWN, LEFT, RIGHT, FORWARD, BACKWARD
    }

    /**
     * Finds an element matching the given selector.
     *
     * @param selector The selector criteria
     * @return Result containing the found node or error
     */
    fun findElement(selector: ElementSelector): FindElementResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot find element: Service not connected")
            return FindElementResult.ServiceNotConnected
        }

        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.w(TAG, "Cannot find element: No active window")
            return FindElementResult.NotFound
        }

        return try {
            val result = findElementRecursive(rootNode, selector)

            if (result != null) {
                Log.d(TAG, "Found element matching selector: $selector")
                FindElementResult.Found(result)
            } else {
                Log.d(TAG, "Element not found for selector: $selector")
                FindElementResult.NotFound
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException finding element: ${e.message}", e)
            FindElementResult.Error(e.message ?: "Node became stale")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException finding element: ${e.message}", e)
            FindElementResult.Error("Permission denied: ${e.message}")
        } finally {
            rootNode.recycle()
        }
    }

    /**
     * Recursively searches for an element matching the selector.
     *
     * IMPORTANT: The caller is responsible for recycling the returned node.
     * All other nodes are recycled internally.
     */
    private fun findElementRecursive(
        node: android.view.accessibility.AccessibilityNodeInfo,
        selector: ElementSelector,
        depth: Int = 0
    ): android.view.accessibility.AccessibilityNodeInfo? {
        // Enforce depth limit to prevent stack overflow
        if (depth >= MAX_TREE_DEPTH) {
            Log.w(TAG, "Reached maximum tree depth ($MAX_TREE_DEPTH) during element search")
            return null
        }

        // Check if current node matches
        if (matchesSelector(node, selector)) {
            return node
        }

        // Search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findElementRecursive(child, selector, depth + 1)
            if (result != null) {
                // Found a match in the subtree
                // If the result is the child itself, return it without recycling
                // Otherwise, recycle the child as we're returning a deeper node
                if (child != result) {
                    child.recycle()
                }
                return result
            }
            // No match found in this subtree, recycle the child
            child.recycle()
        }

        return null
    }

    /**
     * Checks if a node matches the selector criteria.
     * All specified selector criteria must match for the method to return true.
     */
    private fun matchesSelector(
        node: android.view.accessibility.AccessibilityNodeInfo,
        selector: ElementSelector
    ): Boolean {
        // Check text criterion
        if (selector.text != null) {
            val nodeText = node.text?.toString()
            if (nodeText != selector.text) {
                return false
            }
        }

        // Check resource ID criterion
        if (selector.resourceId != null) {
            val nodeResourceId = node.viewIdResourceName
            if (nodeResourceId != selector.resourceId) {
                return false
            }
        }

        // Check class name criterion
        if (selector.className != null) {
            val nodeClassName = node.className?.toString()
            if (nodeClassName != selector.className) {
                return false
            }
        }

        // Check content description criterion
        if (selector.contentDescription != null) {
            val nodeContentDesc = node.contentDescription?.toString()
            if (nodeContentDesc != selector.contentDescription) {
                return false
            }
        }

        return true
    }

    /**
     * Performs a tap/click action on an element.
     *
     * @param selector The selector to find the element
     * @return Result indicating success or failure
     */
    fun performTap(selector: ElementSelector): InteractionResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot perform tap: Service not connected")
            return InteractionResult.ServiceNotConnected
        }

        return when (val findResult = findElement(selector)) {
            is FindElementResult.Found -> {
                try {
                    val success = findResult.node.performAction(
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
                    )

                    if (success) {
                        Log.d(TAG, "Successfully tapped element: $selector")
                        InteractionResult.Success
                    } else {
                        Log.w(TAG, "Failed to tap element: $selector")
                        InteractionResult.ActionFailed("Click action returned false")
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException tapping element: ${e.message}", e)
                    InteractionResult.Error(e.message ?: "Node became stale")
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException tapping element: ${e.message}", e)
                    InteractionResult.Error("Permission denied: ${e.message}")
                } finally {
                    findResult.node.recycle()
                }
            }
            is FindElementResult.NotFound -> {
                Log.w(TAG, "Cannot tap: Element not found for selector: $selector")
                InteractionResult.ElementNotFound
            }
            is FindElementResult.ServiceNotConnected -> InteractionResult.ServiceNotConnected
            is FindElementResult.Error -> InteractionResult.Error(findResult.message)
        }
    }

    /**
     * Performs a long press action on an element.
     *
     * @param selector The selector to find the element
     * @return Result indicating success or failure
     */
    fun performLongPress(selector: ElementSelector): InteractionResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot perform long press: Service not connected")
            return InteractionResult.ServiceNotConnected
        }

        return when (val findResult = findElement(selector)) {
            is FindElementResult.Found -> {
                try {
                    val success = findResult.node.performAction(
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK
                    )

                    if (success) {
                        Log.d(TAG, "Successfully long pressed element: $selector")
                        InteractionResult.Success
                    } else {
                        Log.w(TAG, "Failed to long press element: $selector")
                        InteractionResult.ActionFailed("Long click action returned false")
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException long pressing element: ${e.message}", e)
                    InteractionResult.Error(e.message ?: "Node became stale")
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException long pressing element: ${e.message}", e)
                    InteractionResult.Error("Permission denied: ${e.message}")
                } finally {
                    findResult.node.recycle()
                }
            }
            is FindElementResult.NotFound -> {
                Log.w(TAG, "Cannot long press: Element not found for selector: $selector")
                InteractionResult.ElementNotFound
            }
            is FindElementResult.ServiceNotConnected -> InteractionResult.ServiceNotConnected
            is FindElementResult.Error -> InteractionResult.Error(findResult.message)
        }
    }

    /**
     * Types text into an element (must be focusable/editable).
     *
     * @param selector The selector to find the element
     * @param text The text to type
     * @return Result indicating success or failure
     */
    fun performType(selector: ElementSelector, text: String): InteractionResult {
        // Validate text length to prevent DoS
        if (text.length > MAX_TEXT_INPUT_LENGTH) {
            Log.w(TAG, "Cannot perform type: Text too long (${text.length} > $MAX_TEXT_INPUT_LENGTH)")
            return InteractionResult.Error("Text exceeds maximum length of $MAX_TEXT_INPUT_LENGTH characters")
        }

        if (!isServiceConnected) {
            Log.w(TAG, "Cannot perform type: Service not connected")
            return InteractionResult.ServiceNotConnected
        }

        return when (val findResult = findElement(selector)) {
            is FindElementResult.Found -> {
                try {
                    val node = findResult.node

                    // First, focus the element
                    val focusSuccess = node.performAction(
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_FOCUS
                    )

                    if (!focusSuccess) {
                        Log.w(TAG, "Failed to focus element for typing: $selector")
                        return InteractionResult.ActionFailed("Could not focus element")
                    }

                    // Then, set the text
                    val arguments = android.os.Bundle().apply {
                        putCharSequence(
                            android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            text
                        )
                    }

                    val success = node.performAction(
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,
                        arguments
                    )

                    if (success) {
                        Log.d(TAG, "Successfully typed text into element: $selector")
                        InteractionResult.Success
                    } else {
                        Log.w(TAG, "Failed to type text into element: $selector")
                        InteractionResult.ActionFailed("Set text action returned false")
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException typing into element: ${e.message}", e)
                    InteractionResult.Error(e.message ?: "Node became stale")
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException typing into element: ${e.message}", e)
                    InteractionResult.Error("Permission denied: ${e.message}")
                } finally {
                    findResult.node.recycle()
                }
            }
            is FindElementResult.NotFound -> {
                Log.w(TAG, "Cannot type: Element not found for selector: $selector")
                InteractionResult.ElementNotFound
            }
            is FindElementResult.ServiceNotConnected -> InteractionResult.ServiceNotConnected
            is FindElementResult.Error -> InteractionResult.Error(findResult.message)
        }
    }

    /**
     * Performs a scroll action on an element or the screen.
     *
     * @param direction The direction to scroll
     * @param selector Optional selector to find a specific scrollable element.
     *                 If null, scrolls the first scrollable element in the active window.
     * @return Result indicating success or failure
     */
    fun performScroll(
        direction: ScrollDirection,
        selector: ElementSelector? = null
    ): InteractionResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot perform scroll: Service not connected")
            return InteractionResult.ServiceNotConnected
        }

        return try {
            val node = if (selector != null) {
                when (val findResult = findElement(selector)) {
                    is FindElementResult.Found -> findResult.node
                    is FindElementResult.NotFound -> {
                        Log.w(TAG, "Cannot scroll: Element not found for selector: $selector")
                        return InteractionResult.ElementNotFound
                    }
                    is FindElementResult.ServiceNotConnected -> return InteractionResult.ServiceNotConnected
                    is FindElementResult.Error -> return InteractionResult.Error(findResult.message)
                }
            } else {
                // Find first scrollable element
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.w(TAG, "Cannot scroll: No active window")
                    return InteractionResult.ElementNotFound
                }

                try {
                    val scrollable = findFirstScrollable(rootNode)
                    if (scrollable == null) {
                        Log.w(TAG, "Cannot scroll: No scrollable element found")
                        return InteractionResult.ElementNotFound
                    }
                    scrollable
                } finally {
                    rootNode.recycle()
                }
            }

            try {
                val action = when (direction) {
                    ScrollDirection.UP, ScrollDirection.BACKWARD ->
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.DOWN, ScrollDirection.FORWARD ->
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    ScrollDirection.LEFT ->
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                    ScrollDirection.RIGHT ->
                        android.view.accessibility.AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                }

                val success = node.performAction(action)

                if (success) {
                    Log.d(TAG, "Successfully scrolled $direction")
                    InteractionResult.Success
                } else {
                    Log.w(TAG, "Failed to scroll $direction")
                    InteractionResult.ActionFailed("Scroll action returned false")
                }
            } finally {
                node.recycle()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException scrolling: ${e.message}", e)
            InteractionResult.Error(e.message ?: "Node became stale")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scrolling: ${e.message}", e)
            InteractionResult.Error("Permission denied: ${e.message}")
        }
    }

    /**
     * Finds the first scrollable element in the node tree.
     *
     * IMPORTANT: The caller is responsible for recycling the returned node.
     * All other nodes are recycled internally.
     */
    private fun findFirstScrollable(
        node: android.view.accessibility.AccessibilityNodeInfo,
        depth: Int = 0
    ): android.view.accessibility.AccessibilityNodeInfo? {
        // Enforce depth limit to prevent stack overflow
        if (depth >= MAX_TREE_DEPTH) {
            Log.w(TAG, "Reached maximum tree depth ($MAX_TREE_DEPTH) during scrollable search")
            return null
        }

        if (node.isScrollable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFirstScrollable(child, depth + 1)
            if (result != null) {
                // Found a scrollable element in the subtree
                // If the result is the child itself, return it without recycling
                // Otherwise, recycle the child as we're returning a deeper node
                if (child != result) {
                    child.recycle()
                }
                return result
            }
            // No scrollable element found in this subtree, recycle the child
            child.recycle()
        }

        return null
    }

    // ===== Screen Content Reading API =====

    /**
     * Reads the current screen content and returns a structured representation.
     *
     * This is the primary API for capturing what's on screen at any moment.
     * By default, it reads the active application window, but can be configured
     * to read system windows, input methods, etc.
     *
     * @param filterWindowType Optional window type to filter for (null = read active window only)
     * @return Result containing the screen representation or error
     */
    fun readScreenContent(filterWindowType: WindowType? = null): ScreenReadResult {
        if (!isServiceConnected) {
            Log.w(TAG, "Cannot read screen: Service not connected")
            return ScreenReadResult.ServiceNotConnected
        }

        return try {
            // For now, we only support reading the active window
            // Future iterations will support reading multiple windows with filtering
            val rootNode = rootInActiveWindow
            if (rootNode == null) {
                Log.w(TAG, "Cannot read screen: No active window")
                return ScreenReadResult.NoActiveWindow
            }

            try {
                val rootElement = traverseNodeTree(rootNode, depth = 0)
                val packageName = rootNode.packageName?.toString()
                val activityName = null // ActivityName not directly available from node

                // Determine window type (for now, assume APPLICATION)
                // Future iterations will properly detect window type from AccessibilityWindowInfo
                val detectedWindowType = filterWindowType ?: WindowType.APPLICATION

                ScreenReadResult.Success(
                    ScreenRepresentation(
                        windowType = detectedWindowType,
                        packageName = packageName,
                        activityName = activityName,
                        rootElement = rootElement,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } finally {
                rootNode.recycle()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException reading screen: ${e.message}", e)
            ScreenReadResult.Error(e.message ?: "Screen reading failed")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException reading screen: ${e.message}", e)
            ScreenReadResult.Error("Permission denied: ${e.message}")
        }
    }

    /**
     * Traverses the accessibility node tree recursively and builds a ScreenElement tree.
     *
     * IMPORTANT: This method does NOT recycle nodes - the caller is responsible for
     * recycling the root node. All child nodes are recycled internally.
     *
     * @param node The node to traverse
     * @param depth The current depth in the tree (0 for root)
     * @return ScreenElement representing this node and its children
     */
    private fun traverseNodeTree(
        node: android.view.accessibility.AccessibilityNodeInfo,
        depth: Int
    ): ScreenElement {
        // Enforce depth limit to prevent stack overflow
        if (depth >= MAX_TREE_DEPTH) {
            Log.w(TAG, "Reached maximum tree depth ($MAX_TREE_DEPTH), stopping traversal")
            return extractNodeProperties(node, depth, emptyList())
        }

        // Extract properties from current node
        val children = mutableListOf<ScreenElement>()

        // Traverse children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                try {
                    val childElement = traverseNodeTree(child, depth + 1)
                    children.add(childElement)
                } finally {
                    child.recycle()
                }
            }
        }

        return extractNodeProperties(node, depth, children)
    }

    /**
     * Extracts properties from an AccessibilityNodeInfo into a ScreenElement.
     *
     * @param node The node to extract properties from
     * @param depth The depth of this node in the tree
     * @param children The already-processed child elements
     * @return ScreenElement with all properties extracted
     */
    private fun extractNodeProperties(
        node: android.view.accessibility.AccessibilityNodeInfo,
        depth: Int,
        children: List<ScreenElement>
    ): ScreenElement {
        return ScreenElement(
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            viewIdResourceName = node.viewIdResourceName,
            bounds = android.graphics.Rect().apply { node.getBoundsInScreen(this) },
            isClickable = node.isClickable,
            isCheckable = node.isCheckable,
            isChecked = node.isChecked,
            isEnabled = node.isEnabled,
            isScrollable = node.isScrollable,
            isEditable = node.isEditable,
            isFocusable = node.isFocusable,
            isFocused = node.isFocused,
            isPassword = node.isPassword,
            children = children,
            depth = depth
        )
    }

    // ===== Event Monitoring API =====

    /**
     * Types of screen events that can be monitored.
     */
    enum class ScreenEventType {
        /** Active window changed (app switch, dialog open/close) */
        WINDOW_CHANGE,

        /** Content within the current window changed */
        CONTENT_CHANGE,

        /** Any event (no filtering) */
        ALL
    }

    /**
     * Represents a screen change event.
     *
     * @property eventType The type of event
     * @property packageName The package name of the app where the event occurred
     * @property windowTitle The title of the window (if available)
     * @property timestamp When the event occurred
     * @property accessibilityEventType The raw Android accessibility event type
     * @property additionalInfo Additional context about what changed
     */
    data class ScreenEvent(
        val eventType: ScreenEventType,
        val packageName: String?,
        val windowTitle: String?,
        val timestamp: Long = System.currentTimeMillis(),
        val accessibilityEventType: Int,
        val additionalInfo: String? = null
    )

    /**
     * Listener interface for receiving screen events.
     */
    fun interface ScreenEventListener {
        /**
         * Called when a screen event occurs.
         *
         * IMPORTANT: This is called on the main thread. Keep processing fast or
         * delegate to a background thread to avoid blocking the accessibility service.
         *
         * @param event The screen event
         */
        fun onScreenEvent(event: ScreenEvent)
    }

    /**
     * Filter criteria for screen events.
     */
    data class EventFilter(
        val eventTypes: Set<ScreenEventType> = setOf(ScreenEventType.ALL),
        val packageNames: Set<String> = emptySet() // Empty = all packages
    ) {
        /**
         * Checks if this filter matches the given event.
         */
        fun matches(event: ScreenEvent): Boolean {
            // Check event type
            if (!eventTypes.contains(ScreenEventType.ALL) && !eventTypes.contains(event.eventType)) {
                return false
            }

            // Check package name
            if (packageNames.isNotEmpty()) {
                if (event.packageName == null || !packageNames.contains(event.packageName)) {
                    return false
                }
            }

            return true
        }
    }

    /**
     * Represents a registered event listener with its filter.
     */
    private data class RegisteredListener(
        val listener: ScreenEventListener,
        val filter: EventFilter
    )

    /**
     * Thread-safe list of registered listeners.
     * We use CopyOnWriteArrayList for thread-safety without explicit synchronization,
     * as reads are frequent but writes (add/remove listener) are rare.
     */
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<RegisteredListener>()

    /**
     * Registers a listener to receive screen events.
     *
     * @param listener The listener to register
     * @param filter Optional filter to limit which events are received (default: all events)
     * @return A subscription that can be used to unregister the listener
     */
    fun addEventListener(
        listener: ScreenEventListener,
        filter: EventFilter = EventFilter()
    ): EventSubscription {
        val registered = RegisteredListener(listener, filter)
        listeners.add(registered)

        Log.d(TAG, "Event listener registered with filter: $filter")

        return object : EventSubscription {
            override fun unsubscribe() {
                val removed = listeners.remove(registered)
                if (removed) {
                    Log.d(TAG, "Event listener unregistered")
                }
            }
        }
    }

    /**
     * Subscription handle for event listeners.
     * Call unsubscribe() to stop receiving events.
     */
    interface EventSubscription {
        fun unsubscribe()
    }

    /**
     * Notifies all registered listeners about an accessibility event.
     * This method is called from onAccessibilityEvent on the main thread.
     */
    private fun notifyListeners(event: AccessibilityEvent) {
        if (listeners.isEmpty()) {
            return
        }

        // Convert AccessibilityEvent to ScreenEvent
        val screenEvent = convertToScreenEvent(event)

        // Notify all matching listeners
        for (registered in listeners) {
            if (registered.filter.matches(screenEvent)) {
                try {
                    registered.listener.onScreenEvent(screenEvent)
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "IllegalStateException in event listener: ${e.message}", e)
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException in event listener: ${e.message}", e)
                } catch (e: RuntimeException) {
                    // Catch other runtime exceptions from listener code to prevent one bad listener from breaking others
                    Log.e(TAG, "RuntimeException in event listener: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Converts an Android AccessibilityEvent to our ScreenEvent representation.
     */
    private fun convertToScreenEvent(event: AccessibilityEvent): ScreenEvent {
        val eventType = when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> ScreenEventType.WINDOW_CHANGE

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> ScreenEventType.CONTENT_CHANGE

            else -> ScreenEventType.CONTENT_CHANGE // Default to content change for other events
        }

        val packageName = event.packageName?.toString()

        // Extract window title from event if available
        val windowTitle = if (event.text.isNotEmpty()) {
            event.text.joinToString(" ")
        } else {
            event.contentDescription?.toString()
        }

        // Build additional info string
        val additionalInfo = buildString {
            event.className?.let { append("class=$it ") }
            if (event.itemCount > 0) {
                append("items=${event.itemCount} ")
            }
        }.trim().takeIf { it.isNotEmpty() }

        return ScreenEvent(
            eventType = eventType,
            packageName = packageName,
            windowTitle = windowTitle,
            timestamp = System.currentTimeMillis(),
            accessibilityEventType = event.eventType,
            additionalInfo = additionalInfo
        )
    }

    /**
     * Removes all registered event listeners.
     * Useful for cleanup when the service is shutting down.
     */
    private fun clearAllListeners() {
        val count = listeners.size
        listeners.clear()
        if (count > 0) {
            Log.d(TAG, "Cleared $count event listeners")
        }
    }

    // TODO: Future methods to be implemented:
    // - waitForElement(selector: ElementSelector, timeout: Duration): Boolean
    // - performSwipe(from: Point, to: Point)
    // - getElementBounds(selector: ElementSelector): Rect?
}
