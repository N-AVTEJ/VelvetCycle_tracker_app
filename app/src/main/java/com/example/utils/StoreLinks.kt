package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object StoreLinks {
    const val BLINKIT_KEY = "blinkit"
    const val ZEPTO_KEY = "zepto"

    fun openStore(context: Context, storeKey: String) {
        val (appUrl, webUrl) = when (storeKey) {
            BLINKIT_KEY -> Pair("blinkit://search?q=sanitary+pads", "https://blinkit.com/s/?q=sanitary+pads")
            ZEPTO_KEY -> Pair("zepto://search?query=sanitary+pads", "https://www.zeptonow.com/search?query=sanitary+pads")
            else -> return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
