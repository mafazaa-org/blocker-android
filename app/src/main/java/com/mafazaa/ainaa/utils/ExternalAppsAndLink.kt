package com.mafazaa.ainaa.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object ExternalAppsAndLink {

    /**
     * Open Facebook app or web page
     * @param context Android context
     * @param pageNameOrUrl The Facebook page name or URL (leave empty to just open the app)
     */
    fun openFacebook(context: Context, pageNameOrUrl: String = "") {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (pageNameOrUrl.isNotEmpty()) {
                    data = "https://www.facebook.com/$pageNameOrUrl".toUri()
                    setPackage("com.facebook.katana")
                } else {
                    data = "fb://".toUri()
                    setPackage("com.facebook.katana")
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser if app is not installed
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = if (pageNameOrUrl.isNotEmpty()) {
                    "https://www.facebook.com/$pageNameOrUrl".toUri()
                } else {
                    "https://www.facebook.com".toUri()
                }
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Open WhatsApp app or chat
     * @param context Android context
     * @param phoneNumberOrChatId The phone number (with country code) or chat ID (leave empty to just open the app)
     */
    fun openWhatsApp(context: Context, phoneNumberOrChatId: String = "") {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = if (phoneNumberOrChatId.isNotEmpty()) {
                    "https://wa.me/$phoneNumberOrChatId".toUri()
                } else {
                    "https://www.whatsapp.com".toUri()
                }
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser if app is not installed
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = if (phoneNumberOrChatId.isNotEmpty()) {
                    "https://wa.me/$phoneNumberOrChatId".toUri()
                } else {
                    "https://www.whatsapp.com".toUri()
                }
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Open YouTube app or video
     * @param context Android context
     * @param videoIdOrChannelUrl The video ID or channel URL (leave empty to just open the app)
     */
    fun openYouTube(context: Context, videoIdOrChannelUrl: String = "") {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = if (videoIdOrChannelUrl.isNotEmpty()) {
                    // Check if it's a video ID or a full URL
                    if (videoIdOrChannelUrl.startsWith("http")) {
                        videoIdOrChannelUrl.toUri()
                    } else {
                        "https://www.youtube.com/watch?v=$videoIdOrChannelUrl".toUri()
                    }
                } else {
                    "https://www.youtube.com".toUri()
                }
                setPackage("com.google.android.youtube")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser if app is not installed
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = if (videoIdOrChannelUrl.isNotEmpty()) {
                    if (videoIdOrChannelUrl.startsWith("http")) {
                        videoIdOrChannelUrl.toUri()
                    } else {
                        "https://www.youtube.com/watch?v=$videoIdOrChannelUrl".toUri()
                    }
                } else {
                    "https://www.youtube.com".toUri()
                }
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Open external link in the default browser
     * @param context Android context
     * @param url The URL to open (e.g., "https://www.example.com")
     */
    fun openLinkInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Log error if no browser is available
            e.printStackTrace()
        }
    }
}
