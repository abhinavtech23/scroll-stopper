package com.feedshield.android.core.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import com.feedshield.android.core.util.Logger

/**
 * Lightweight, high-performance UI Node Inspector for accessibility tree analysis.
 * Safely inspects node hierarchies with recursion bounds and prevents memory leaks.
 */
object NodeInspector {

    private const val TAG = "FeedShield.NodeInspector"
    private const val MAX_TRAVERSAL_DEPTH = 30

    /**
     * Checks if the node tree contains a view with a resource ID matching any of the given [idSuffixes].
     * Example: "clips_viewer_view_pager", "reel_viewer_container"
     */
    fun hasNodeWithIdSuffix(
        root: AccessibilityNodeInfo?,
        idSuffixes: Set<String>,
        maxDepth: Int = MAX_TRAVERSAL_DEPTH
    ): Boolean {
        if (root == null) return false
        return findFirstMatching(root, maxDepth = maxDepth) { node ->
            val viewId = node.viewIdResourceName ?: return@findFirstMatching false
            idSuffixes.any { suffix -> viewId.endsWith(suffix, ignoreCase = true) }
        } != null
    }

    /**
     * Finds the first node satisfying the [predicate] within the specified [maxDepth].
     * Note: Callers are responsible for node recycling if returning instances, but this utility handles
     * internal traversal safely.
     */
    fun findFirstMatching(
        node: AccessibilityNodeInfo?,
        currentDepth: Int = 0,
        maxDepth: Int = MAX_TRAVERSAL_DEPTH,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (node == null || currentDepth > maxDepth) return null

        try {
            if (predicate(node)) {
                return node
            }

            val childCount = node.childCount
            for (i in 0 until childCount) {
                val child = node.getChild(i) ?: continue
                val match = findFirstMatching(child, currentDepth + 1, maxDepth, predicate)
                if (match != null) {
                    return match
                }
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Error traversing node at depth $currentDepth: ${e.message}")
        }

        return null
    }

    /**
     * Performs a non-blocking inspection scan to collect resource IDs present in the current view hierarchy.
     * Useful for debugging new app UI updates without recording sensitive text content.
     */
    fun dumpViewHierarchyStructure(
        node: AccessibilityNodeInfo?,
        depth: Int = 0,
        maxDepth: Int = 10,
        resultBuilder: StringBuilder = StringBuilder()
    ): String {
        if (node == null || depth > maxDepth) return resultBuilder.toString()

        val indent = "  ".repeat(depth)
        val className = node.className?.toString() ?: "UnknownClass"
        val viewId = node.viewIdResourceName ?: "no_id"
        val isScrollable = if (node.isScrollable) " [scrollable]" else ""
        
        resultBuilder.appendLine("$indent- $className ($viewId)$isScrollable")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dumpViewHierarchyStructure(child, depth + 1, maxDepth, resultBuilder)
        }

        return resultBuilder.toString()
    }
}
