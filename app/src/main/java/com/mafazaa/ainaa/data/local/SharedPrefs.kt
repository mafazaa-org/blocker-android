package com.mafazaa.ainaa.data.local

import android.content.SharedPreferences


class SharedPrefs(sharedPreferences: SharedPreferences) {
    /**
     * Set of blocked app package names.
     */
    var blockedApps by sharedPreferences.delegates.stringSet()

    var dnsProtectionLevel by sharedPreferences.delegates.protectionLevel()

    /**
     * The version code of the last downloaded update.
     */
    var downloadedVersion by sharedPreferences.delegates.int(0)

    /**
     * The last version of the app that was installed.
     * To check if the app was updated, compare this with the current version.
     */
    var lastVersion by sharedPreferences.delegates.int(0)
}

/**
 * Returns a new set with the specified string added.
 *
 * @receiver The original set.
 * @param string The string to add.
 * @return A new set containing all elements of the original set plus the new string.
 */
fun Set<String>.add(string: String): Set<String> {
    val m = this.toMutableSet()
    m.add(string)
    return m.toSet()
}

/**
 * Returns a new set with the specified string removed.
 *
 * @receiver The original set.
 * @param string The string to remove.
 * @return A new set containing all elements of the original set except the specified string.
 */
fun Set<String>.remove(string: String): Set<String> {
    val m = this.toMutableSet()
    m.remove(string)
    return m.toSet()
}