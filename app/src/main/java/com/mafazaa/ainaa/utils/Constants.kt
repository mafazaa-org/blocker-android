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
        ), ScriptCode(
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
            if (screen.nodesCount  > 50)
                return false; 
    return  containsText(screen.root, "fgs_manager_app_item_label");
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ),
        // New script: settings app info screen for `com.android.settings` (Samsung)
        ScriptCode(
            "settings app info screen samsung", """
                (function() {
                  try {
                    if (!screen.root) return false;
                    // node count observed for this layout
                    if (screen.nodesCount !== 16) return false;
                    return  screen.hasAppName && screen.isSettingsScreen ;
                
                  } catch (e) {
                    return false;
                  }
                })();
            """.trimIndent()
        ),
        // New script: uninstall dialog from com.android.launcher3 with 9 nodes
        ScriptCode(
            "uninstall screen realme", """
(function() {
  try {
    if (!screen.root) return false;
    if (screen.nodesCount !== 9) return false;
    if (screen.app !== "com.android.launcher3") return false;
    if (!screen.hasAppName) return false;
    if (screen.isSettingsScreen) return false;
    if (!containsText(screen.root, "Uninstall")) return false;
    if (!containsText(screen.root, "App data, including files and settings")) return false;
    const hasCancel = containsText(screen.root, "Cancel") || !!findById(screen.root, "android:id/button2");
    const hasUninstall = containsText(screen.root, "Uninstall") || !!findById(screen.root, "android:id/button1");
    if (!hasCancel || !hasUninstall) return false;
    return true;
  } catch (e) {
    return false;
  }
})();
            """.trimIndent()
        ),
        // New script: uninstall dialog from com.google.android.packageinstaller with 6 nodes
        ScriptCode(
            "uninstall screen samsung", """
(function() {
  try {
    if (!screen.root) return false;
    if (screen.nodesCount !== 6) return false;
    if (screen.app !== "com.google.android.packageinstaller") return false;
    if (!screen.hasAppName) return false;
    if (screen.isSettingsScreen) return false;
    if (!containsText(screen.root, "Do you want to uninstall this app?")) return false;
    const hasTitle = containsText(screen.root, "عَيْنًا سَلْسَبِيلًا") || containsText(screen.root, "alertTitle") || !!findById(screen.root, "android:id/alertTitle");
    if (!hasTitle) return false;
    const hasCancel = containsText(screen.root, "Cancel") || !!findById(screen.root, "android:id/button2");
    const hasOk = containsText(screen.root, "OK") || !!findById(screen.root, "android:id/button1");
    if (!hasCancel || !hasOk) return false;

    return true;
  } catch (e) {
    return false;
  }
})();
            """.trimIndent()
        ),
        ScriptCode(
            "uninstall popup samsung", """          
(function() {
  try {
    if (!screen.root) return false;
    const children = screen.root.children;
    if (!children || children.length < 3) return false;
    const title = children[0];
    const message = children[1];
    const panel = children[2];
    if (!title || title.cls !== "android.widget.TextView") return false;
    if (!message || message.cls !== "android.widget.TextView") return false;
    if (!panel || panel.cls !== "android.widget.ScrollView") return false;
    const panelChildren = panel.children || [];
    const buttonCount = panelChildren.filter(c => c.cls === "android.widget.Button").length;
    return buttonCount >= 2;
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        )
    )
}