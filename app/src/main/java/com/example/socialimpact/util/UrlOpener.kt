package com.example.socialimpact.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Utility to open URLs using Chrome Custom Tabs.
 */
object UrlOpener {
    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        
        try {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            intent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback to external browser if Custom Tabs fails
            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }
}
