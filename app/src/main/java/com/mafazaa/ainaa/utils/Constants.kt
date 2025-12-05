package com.mafazaa.ainaa.utils

import com.mafazaa.ainaa.domain.models.ScriptCode

object Constants {
    const val VPN_ADDRESS = "10.0.0.2"
    const val SUPPORT_URL = "https://ainaa.mafazaa.com/support_us"
    const val JOIN_URL = "https://www.mafazaa.com/join"
    const val SUPPORT_CONTACT_URL = "https://ainaa.mafazaa.com/support"
    const val SAFE_SEARCH_URL = "https://google.com/safesearch"

    /**
     * See [com.mafazaa.ainaa.data.remote.KtorRepo.getLatestVersion]
     */
    const val releaseApkName = "ainaa"
    const val maxNodes = 1000//todo if screen analysis exceeds this value stop analyzing

    /**
     * Default script codes to detect disabling attempts
     */
    val defaultCodes: List<ScriptCode> = listOf(
        ScriptCode(
            "uninstall screen xiaomi", """          
            (function() {
              try {
                var pkg = (screen && screen.pkg) || null;
                return screen.hasAppName && pkg === "com.google.android.packageinstaller" &&screen.root.children.length==6 ;
              } catch (e) {
                return false;
              }
            })();
        """.trimIndent()
        ),
        ScriptCode(
            "Overlay screen xiaomi", """          
(function() {               
    try {
        return screen.hasAppName && screen.isSettingsScreen && screen.nodesCount==15;
    } catch (e) {
        return false;
    }
})();
        """.trimIndent()
        ), ScriptCode(
            "app info screen xiaomi", """          
(function() {     
            try {
            if (!screen.hasAppName || !screen.isSettingsScreen )
                return false;
ls= ["App info", "App details"];
var hasHint=false;
for (h in ls) {
    hasHint= hasHint || containsText(screen.root, h);
}
      
    return hasHint;
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ), ScriptCode(
            "battery pop up xiaomi", """          
(function() {     
            try {
        if (!screen.root) return false;
        const children = screen.root.children;
        if (!children || children.length !== 5) return false;
        return children[0].cls === "android.widget.TextView" 
            && children[1].cls === "android.widget.TextView" 
            && children[2].cls === "android.widget.CheckBox" 
            && children[3].cls === "android.widget.Button"   
            && children[4].cls === "android.widget.Button"   
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ), ScriptCode(
            "background apps dialog xiaomi", """          
(function() {     
            try {
            if (!screen.hasAppName )
                return false;
    
    return  containsText(screen.root, "fgs_manager_app_item_label");
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ),ScriptCode(
            "device admin screen xiaomi", """
(function() {
    try {
        if (!screen.hasAppName || !screen.isSettingsScreen) {
            return false;
        }
        
        // Check for device admin related text
        var deviceAdminKeywords = [
            "Device admin",
            "Device administrator", 
            "Device admin apps",
            "Deactivate",
            "This device admin app has access to"
        ];
        
        var hasDeviceAdminText = false;
        for (var i = 0; i < deviceAdminKeywords.length; i++) {
            if (containsText(screen.root, deviceAdminKeywords[i])) {
                hasDeviceAdminText = true;
                break;
            }
        }
        
        return hasDeviceAdminText;
    } catch (e) {
        return false;
    }
})();
        """.trimIndent()
        ),
        ScriptCode(
            "accessibility revoke screen xiaomi", """
(function() {
    try {
        if (!screen.isSettingsScreen || !screen.hasAppName) {
            return false;
        }

        // Accessibility context keywords
        var accessibilityKeywords = [
            "Accessibility",
            "Accessibility service"
        ];

        // Revocation / disabling intent keywords
        var revokeKeywords = [
            "Disable",
            "Turn off",
            "Stop",
            "Deactivate",
            "Off",
            "Use service"
        ];

        var hasAccessibilityContext = false;
        for (var i = 0; i < accessibilityKeywords.length; i++) {
            if (containsText(screen.root, accessibilityKeywords[i])) {
                hasAccessibilityContext = true;
                break;
            }
        }
        if (!hasAccessibilityContext) return false;

        var hasRevokeIntent = false;
        for (var j = 0; j < revokeKeywords.length; j++) {
            if (containsText(screen.root, revokeKeywords[j])) {
                hasRevokeIntent = true;
                break;
            }
        }

        return hasRevokeIntent;
    } catch (e) {
        return false;
    }
})();
    """.trimIndent()
        ),
        ScriptCode(
            "developer options screen xiaomi", """
(function() {
    try {
        if (!screen.isSettingsScreen) {
            return false;
        }

        // Developer options titles commonly seen
        var devTitles = [
            "Developer options",
            "Developer settings",
            "Developer",
            "Additional settings" // MIUI path
        ];
        var hasDevTitle = false;
        for (var i = 0; i < devTitles.length; i++) {
            if (containsText(screen.root, devTitles[i])) {
                hasDevTitle = true;
                break;
            }
        }
        if (!hasDevTitle) return false;

        // Risky toggles often used to bypass protection
        var riskyToggles = [
            "USB debugging",
            "Install via USB",
            "Revoke USB debugging authorizations",
            "Allow mock locations",
            "OEM unlocking",
            "MIUI optimization",
            "Stay awake"
        ];
        var hasRisky = false;
        for (var j = 0; j < riskyToggles.length; j++) {
            if (containsText(screen.root, riskyToggles[j])) {
                hasRisky = true;
                break;
            }
        }

        // Flag when developer options with risky items is visible
        return hasRisky;
    } catch (e) {
        return false;
    }
})();
    """.trimIndent()
        )
    )
}