package com.mafazaa.ainaa.domain.models

import com.google.gson.Gson

data class ScreenAnalysis(
    /**
     * the package name of the app currently in foreground
     */
    val pkg: String?,
    /**
     * our app name . i added it so i can pass it to the script engine (js)
     */
    val appName: String,
    val nodesCount: Int,
    /**
     * true if any of the nodes text contains our app name
     * a utility field to help the script engine (js) to work faster
     * instead of searching for the app name in all the nodes texts
     */
    val hasAppName: Boolean,
    /**
     * true if the current screen belongs to a settings app
     * a utility field also
     */
    val isSettingsScreen: Boolean,
    val root: ScreenNode,
) {
    fun toJson():String {
        val gson = Gson()
        return gson.toJson(this)
    }
    override fun toString(): String {
        return "nodes:$nodesCount, app:$pkg, has our app name:$hasAppName,is a settings screen:$isSettingsScreen" +
                "\n" + root.toString()
    }
}