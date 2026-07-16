package com.example.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object StoreLinks {
    const val BLINKIT_KEY = "blinkit"
    const val ZEPTO_KEY = "zepto"

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openStore(context: Context, storeKey: String) {
        val (name, packageName, playStoreUrl, browserUrl) = when (storeKey) {
            BLINKIT_KEY -> {
                listOf(
                    "Blinkit",
                    "com.grofers.customerapp",
                    "https://play.google.com/store/apps/details?id=com.grofers.customerapp",
                    "https://blinkit.com/s/?q=sanitary+pads"
                )
            }
            ZEPTO_KEY -> {
                listOf(
                    "Zepto",
                    "com.zeptoconsumerapp",
                    "https://play.google.com/store/apps/details?id=com.zeptoconsumerapp",
                    "https://www.zeptonow.com/search?query=sanitary+pads"
                )
            }
            else -> return
        }

        try {
            if (isAppInstalled(context, packageName)) {
                // On Android use browser URL which redirects to app if installed
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                // Fallback: open Play Store to install the app
                AlertDialog.Builder(context)
                    .setTitle("Open $name")
                    .setMessage("$name app is not installed. Open Play Store to install it?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Install") { _, _ ->
                        try {
                            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(playStoreIntent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(browserIntent)
                        }
                    }
                    .show()
            }
        } catch (error: Exception) {
            // Final fallback — always works
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open store. Please try manually.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
