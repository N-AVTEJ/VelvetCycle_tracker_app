package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object StoreLinks {
    const val BLINKIT_KEY = "blinkit"
    const val ZEPTO_KEY = "zepto"

    private val STORES = mapOf(
        BLINKIT_KEY to "https://blinkit.com/s/?q=sanitary+pads",
        ZEPTO_KEY to "https://www.zeptonow.com/search?query=sanitary+pads"
    )

    fun openStore(context: Context, storeKey: String) {
        Log.d("StoreLinks", "openStore called: $storeKey")
        val url = STORES[storeKey]
        if (url == null) {
            Log.e("StoreLinks", "Unknown store key: $storeKey")
            return
        }

        try {
            val colorParams = androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                .setToolbarColor(android.graphics.Color.parseColor("#D4537E"))
                .build()
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorParams)
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
            Log.d("StoreLinks", "Successfully launched CustomTabs for $url")
        } catch (e: Exception) {
            Log.e("StoreLinks", "CustomTabs failed: ${e.localizedMessage}")
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                Log.d("StoreLinks", "Successfully launched browser intent for $url")
            } catch (err: Exception) {
                Log.e("StoreLinks", "Browser intent failed: ${err.localizedMessage}")
                Toast.makeText(context, "Cannot open. Visit $url manually.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

