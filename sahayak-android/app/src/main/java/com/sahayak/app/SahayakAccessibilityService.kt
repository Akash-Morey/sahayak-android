package com.sahayak.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads the live Android UI tree even when FLAG_SECURE blocks pixel screenshots
 * in banking/UPI apps (like PhonePe). Detects PIN screens to trigger voice-only privacy handoff.
 */
class SahayakAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: SahayakAccessibilityService? = null
            private set

        @Volatile
        var latestUiTreeJson: String = "[]"
            private set

        @Volatile
        var currentPackage: String = ""
            private set

        @Volatile
        var isPinScreenDetected: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        currentPackage = root.packageName?.toString().orEmpty()

        val nodes = JSONArray()
        var pinDetected = false
        collectNodes(root, nodes) { text ->
            val lower = text.lowercase()
            if (lower.contains("upi pin") || lower.contains("enter pin") || lower.contains("mpin")) {
                pinDetected = true
            }
        }
        latestUiTreeJson = nodes.toString()
        isPinScreenDetected = pinDetected
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun findTargetBounds(hint: String): Rect? {
        val root = rootInActiveWindow ?: return null
        return searchNodeRect(root, hint.lowercase())
    }

    private fun searchNodeRect(node: AccessibilityNodeInfo, hintLower: String): Rect? {
        val text = (node.text?.toString() ?: "") + " " + (node.contentDescription?.toString() ?: "")
        if (text.lowercase().contains(hintLower)) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (!rect.isEmpty) return rect
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = searchNodeRect(child, hintLower)
            if (found != null) return found
        }
        return null
    }

    private fun collectNodes(
        node: AccessibilityNodeInfo,
        out: JSONArray,
        onText: (String) -> Unit
    ) {
        val label = node.text?.toString() ?: node.contentDescription?.toString()
        if (!label.isNullOrBlank() && node.isClickable) {
            onText(label)
            val rect = Rect()
            node.getBoundsInScreen(rect)
            out.put(JSONObject().apply {
                put("label", label)
                put("x", rect.centerX())
                put("y", rect.centerY())
                put("w", rect.width())
                put("h", rect.height())
            })
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectNodes(it, out, onText) }
        }
    }
}
