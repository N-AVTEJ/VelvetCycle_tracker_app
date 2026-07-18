package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object StoreLinks {
    const val BLINKIT_KEY = "blinkit"
    const val ZEPTO_KEY = "zepto"

    fun openStore(context: Context, storeKey: String) {
        Log.d("StoreLinks", "openStore called with: $storeKey")
        val url = when (storeKey) {
            BLINKIT_KEY -> "https://blinkit.com/s/?q=sanitary+pads"
            ZEPTO_KEY -> "https://www.zeptonow.com/search?query=sanitary+pads"
            else -> return
        }

        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (error: Exception) {
            Toast.makeText(context, "Cannot open store. Please visit $url manually.", Toast.LENGTH_LONG).show()
        }
    }
}
