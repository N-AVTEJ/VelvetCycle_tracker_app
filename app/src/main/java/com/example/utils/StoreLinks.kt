package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast

object StoreLinks {
    const val BLINKIT_KEY = "blinkit"
    const val ZEPTO_KEY = "zepto"

    // Package names
    private const val BLINKIT_PACKAGE = "com.grofers.customerapp"
    private const val ZEPTO_PACKAGE = "com.zeptoconsumerapp"

    /**
     * Checks if a package is installed on the device.
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: Exception) {
            Log.d("StoreLinks", "Package $packageName is not installed: ${e.localizedMessage}")
            false
        }
    }

    fun openStore(context: Context, storeKey: String) {
        Log.d("StoreLinks", "openStore called with: $storeKey")
        
        // Define URLs and deep links
        val (packageName, webUrl, deepLinkUrl) = when (storeKey) {
            BLINKIT_KEY -> Triple(
                BLINKIT_PACKAGE,
                "https://blinkit.com/s/?q=sanitary%20pads",
                "blinkit://search?query=sanitary%20pads"
            )
            ZEPTO_KEY -> Triple(
                ZEPTO_PACKAGE,
                "https://www.zeptonow.com/search?query=sanitary%20pads",
                "zepto://search?query=sanitary%20pads"
            )
            else -> return
        }

        val isInstalled = isAppInstalled(context, packageName)
        Log.d("StoreLinks", "$storeKey is installed: $isInstalled")

        if (isInstalled) {
            var launched = false

            // 1. Try custom deep link scheme with package constraint
            try {
                val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl)).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(deepLinkIntent)
                launched = true
                Log.d("StoreLinks", "Successfully launched custom deep link: $deepLinkUrl")
            } catch (e: Exception) {
                Log.e("StoreLinks", "Error launching custom deep link $deepLinkUrl: ${e.localizedMessage}")
            }

            // 2. Fallback to web URL with package constraint (App Link)
            if (!launched) {
                try {
                    val appLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                        setPackage(packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(appLinkIntent)
                    launched = true
                    Log.d("StoreLinks", "Successfully launched app link with package: $webUrl")
                } catch (e: Exception) {
                    Log.e("StoreLinks", "Error launching app link with package $webUrl: ${e.localizedMessage}")
                }
            }

            // 3. Fallback to default browser
            if (!launched) {
                openInBrowser(context, webUrl)
            }
        } else {
            // App is not installed, open directly in user's default browser
            openInBrowser(context, webUrl)
        }
    }

    private fun openInBrowser(context: Context, url: String) {
        try {
            Log.d("StoreLinks", "Opening in default browser: $url")
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (error: Exception) {
            Log.e("StoreLinks", "Error opening browser for $url: ${error.localizedMessage}")
            Toast.makeText(context, "Cannot open store. Please visit $url manually.", Toast.LENGTH_LONG).show()
        }
    }
}

