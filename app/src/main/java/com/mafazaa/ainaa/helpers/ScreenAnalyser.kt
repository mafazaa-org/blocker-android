package com.mafazaa.ainaa.helpers

import android.view.accessibility.AccessibilityNodeInfo
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScreenNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ScreenAnalyser is a utility object for analyzing the UI hierarchy of the current screen
 * using AccessibilityNodeInfo. It traverses the accessibility node tree, collects information
 * about each node, and determines properties such as whether the app name is present,
 * the total node count, and if the screen is likely a settings screen.
 */
object ScreenAnalyser {
    /**
     * Constant representing an unknown package name.
     */
    const val UNKNOWN_PACKAGE = "unknown"

    /**
     * Analyzes the accessibility node tree starting from the given root node.
     *
     * @param root The root AccessibilityNodeInfo of the current screen.
     * @param appName The name of the app to search for in the node tree.
     * @return A [com.mafazaa.ainaa.domain.models.ScreenAnalysis] object containing details about the screen's structure and content.
     */
    suspend fun analyzeScreen(root: AccessibilityNodeInfo, appName: String): ScreenAnalysis =
        withContext(Dispatchers.Default) {
            var hasAppName = false
            var nodesCount = 0

            /**
             * Recursively converts an AccessibilityNodeInfo node and its children into a ScreenNode tree.
             * Increments the node count and checks for the presence of the app name.
             */
            fun toScreenNode(node: AccessibilityNodeInfo): ScreenNode {
                nodesCount++
                if (!hasAppName && node.text?.contains(appName, true) == true) {
                    hasAppName = true
                }
                val children = mutableListOf<ScreenNode>()
                for (i in 0 until node.childCount) {
                    val childNode = node.getChild(i) ?: continue
                    children.add(toScreenNode(childNode))
                }
                return ScreenNode(
                    cls = node.className?.toString(),
                    text = node.text?.toString(),
                    id = node.viewIdResourceName,
                    desc = node.contentDescription?.toString(),
                    children = children
                )
            }

            val screenNode = toScreenNode(root)

            return@withContext ScreenAnalysis(
                pkg = root.packageName?.toString() ?: UNKNOWN_PACKAGE,
                appName = appName,
                nodesCount = nodesCount,
                hasAppName = hasAppName,
                isSettingsScreen = isLikelySettingsPackage(root.packageName?.toString()),
                root = screenNode
            )
        }

    /**
     * Determines if the given package name likely belongs to a system settings app.
     *
     * @param pkg The package name to check.
     * @return True if the package is likely a settings app, false otherwise.
     */
    private fun isLikelySettingsPackage(pkg: String?): Boolean {
        if (pkg == null) return false
        val settingsPkgs = listOf(
            "com.android.settings",              // AOSP
            "com.samsung.android.settings",      // Samsung
            "com.miui.securitycenter",           // MIUI (may vary)
            "com.huawei.systemmanager"           // EMUI (may vary)
            // add vendor-specific packages you target
        )
        return settingsPkgs.any { pkg.startsWith(it) } || pkg.contains(
            "settings",
            ignoreCase = true
        )

    }
}