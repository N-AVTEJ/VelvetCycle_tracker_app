package com.example.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "NotificationReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("notif_id", 0)
        val title = intent.getStringExtra("notif_title") ?: "VelvetCycle"
        val body = intent.getStringExtra("notif_body") ?: "Your cycle health update."

        Log.d(TAG, "onReceive triggered for alarm/notification ID: $id. Title: \"$title\", Body: \"$body\"")

        // Log permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            val isGranted = permissionStatus == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Notification Permission POST_NOTIFICATIONS Status: ${if (isGranted) "GRANTED" else "DENIED"}")
        } else {
            Log.d(TAG, "Notification Permission: GRANTED (Android version < 13)")
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Log and verify notification channel existence
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
            if (channel != null) {
                Log.d(TAG, "Notification Channel ${NotificationHelper.CHANNEL_ID} verified: Importance = ${channel.importance}")
            } else {
                Log.e(TAG, "Notification Channel ${NotificationHelper.CHANNEL_ID} NOT found. Attempting to create it now.")
                NotificationHelper.createNotificationChannel(context)
            }
        }

        try {
            Log.d(TAG, "Attempting to post notification ID: $id using NotificationManager.")
            notificationManager.notify(id, builder.build())
            Log.d(TAG, "Notification ID: $id successfully dispatched to NotificationManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error delivering notification ID: $id", e)
        }
    }
}
