package com.mafazaa.ainaa.utils

import com.mafazaa.ainaa.domain.models.ScriptCode

object Constants {
    const val VPN_ADDRESS = "10.0.0.2"
    const val SUPPORT_URL = "https://ainaa.mafazaa.com/support_us"
    const val JOIN_URL = "https://www.mafazaa.com/join"
    const val SUPPORT_CONTACT_URL = "https://ainaa.mafazaa.com/support"
    const val SAFE_SEARCH_URL = "https://google.com/safesearch"
    val socialMediaPackages = listOf(
        "com.facebook.katana",        // Facebook
        "com.facebook.lite",          // Facebook Lite
        "com.instagram.android",      // Instagram
        "com.facebook.orca",          // Messenger
        "com.whatsapp",               // WhatsApp
        "com.whatsapp.w4b",           // WhatsApp Business
        "com.zhiliaoapp.musically",   // TikTok (older/global)
        "com.ss.android.ugc.trill",   // TikTok (regional)
        "com.snapchat.android",       // Snapchat
        "org.telegram.messenger",     // Telegram
        "org.thunderdog.challegram",  // Telegram X (old)
        "com.reddit.frontpage",       // Reddit
        "com.instagram.barcelona",    // Threads
        "com.twitter.android",        // X / Twitter
        "com.discord",                // Discord
        "com.linkedin.android",       // LinkedIn
        "com.pinterest"               // Pinterest
    )

    val browserPackages = listOf(
        "com.android.chrome",                 // Chrome
        "com.chrome.beta",                    // Chrome Beta
        "com.chrome.dev",                     // Chrome Dev
        "com.chrome.canary",                  // Chrome Canary
        "com.google.android.googlequicksearchbox", // Google app browser
        "com.sec.android.app.sbrowser",       // Samsung Internet
        "org.mozilla.firefox",                // Firefox
        "org.mozilla.firefox_beta",           // Firefox Beta
        "com.opera.browser",                  // Opera
        "com.opera.mini.native",              // Opera Mini
        "com.microsoft.emmx",                 // Microsoft Edge
        "com.brave.browser",                  // Brave
        "com.duckduckgo.mobile.android",      // DuckDuckGo
        "com.vivaldi.browser"                 // Vivaldi
    )

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
        ScriptCode(
            "settings app info screen samsung", """
                (function() {
                  try {
                    if (!screen.root) return false;
                    // node count observed for this layout
                    if (screen.pkg === "com.samsung.accessibility") return true;
                    if (screen.nodesCount !== 16) return false;
                    return  screen.hasAppName && screen.isSettingsScreen ;
                
                  } catch (e) {
                    return false;
                  }
                })();
            """.trimIndent()
        ), ScriptCode(
            "device admin xiaomi", """          
(function() {     
            try {
           return screen.hasAppName && screen.isSettingsScreen && screen.nodesCount==13;
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ), ScriptCode(
            "background apps dialog xiaomi", """          
(function() {     
            try {
           return screen.hasAppName && screen.isSettingsScreen && screen.nodesCount==18;
  } catch (e) {
    return false;
  }
})();
        """.trimIndent()
        ),

        ScriptCode(
            "safe zone screen realme", """
(function() {
  try {
    if (!screen.root) return false;
    if (screen.nodesCount !== 13) return false;
    if (screen.pkg !== "com.android.systemui") return false;
    //check the size is 2
    return  screen.root.children. length === 2;
  } catch (e) {
    return false;
  }
})();
            """.trimIndent()
        ),
        ScriptCode(
            "device admin realme", """
(function() {
    try {
        if (!screen.root) return false;
        return screen.hasAppName && screen.isSettingsScreen &&(screen.nodesCount !==18) ;
    } catch (e) {
        return false;
    }
})();
            """.trimIndent()
        ),
        ScriptCode(
            "device admin samsung", """
(function() {
    try {
        if (!screen.root) return false;
        return screen.hasAppName && screen.isSettingsScreen &&(screen.nodesCount !== 8 ||screen.nodesCount !==18) ;
    } catch (e) {
        return false;
    }
})();
            """.trimIndent()
        ),
        ScriptCode(
            "uninstall screen realme", """
(function() {
  try {
    if (!screen.root) return false;
    if (screen.nodesCount !== 9) return false;
    if (!screen.hasAppName) return false;
    return true;
  } catch (e) {
    return false;
  }
})();
            """.trimIndent()
        ), ScriptCode(
            "uninstall popup samsung", """          
(function() {
  try {
    if (!screen.root) return false;
    if (!screen.hasAppName) return false;
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