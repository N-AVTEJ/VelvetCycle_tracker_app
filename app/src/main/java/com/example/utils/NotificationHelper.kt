package com.example.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "VelvetCycleReminders"
    const val CHANNEL_NAME = "VelvetCycle Reminders"

    const val ID_PERIOD_REMINDER = 1001
    const val ID_OVULATION_ALERT = 1002
    const val ID_DAILY_LOG = 1003
    const val ID_PAD_REMINDER = 1004
    const val ID_DAY_BEFORE_MORNING = 1005
    const val ID_DAY_BEFORE_EVENING = 1006

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "VelvetCycle notifications for period, ovulation, and daily logs."
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllNotifications(context: Context, storageHelper: StorageHelper) {
        createNotificationChannel(context)
        
        // Log current permission status
        val permissionStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (status == android.content.pm.PackageManager.PERMISSION_GRANTED) "GRANTED" else "DENIED"
        } else {
            "GRANTED (Android version < 13)"
        }
        Log.d(TAG, "Scheduling notifications. POST_NOTIFICATIONS permission status: $permissionStatus")

        val lastPeriodStart = storageHelper.lastPeriodStart
        val cycleLength = storageHelper.cycleLength
        
        Log.d(TAG, "User cycle info: lastPeriodStart = $lastPeriodStart, cycleLength = $cycleLength")

        // 1. Period Reminder: 2 days before predicted next period date at 9:00 AM
        if (storageHelper.periodReminderEnabled) {
            val nextPeriodDate = CycleEngine.getNextPeriodDateProjected(lastPeriodStart, cycleLength)
            var reminderDate = nextPeriodDate.minusDays(2)
            var triggerTime = getTriggerTimeMillis(reminderDate, 9, 0)
            
            if (triggerTime <= System.currentTimeMillis()) {
                // If 2 days before the current projected period is already in the past, schedule for the next cycle
                val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
                reminderDate = nextNextPeriodDate.minusDays(2)
                triggerTime = getTriggerTimeMillis(reminderDate, 9, 0)
            }
            
            Log.d(TAG, "Calculated period reminder date: $reminderDate at 9:00 AM (Trigger Time: $triggerTime ms)")
            scheduleAlarm(
                context = context,
                id = ID_PERIOD_REMINDER,
                triggerTimeMs = triggerTime,
                title = "Period coming soon 🌸",
                body = "Your period is expected in 2 days. Take care of yourself."
            )
        } else {
            Log.d(TAG, "Period reminder is disabled. Cancelling alarm.")
            cancelAlarm(context, ID_PERIOD_REMINDER)
        }

        // 2. Ovulation Alert: on ovulation day at 9:00 AM
        if (storageHelper.ovulationAlertEnabled) {
            val nextPeriodDate = CycleEngine.getNextPeriodDateProjected(lastPeriodStart, cycleLength)
            var ovulationDate = nextPeriodDate.minusDays(14)
            var triggerTime = getTriggerTimeMillis(ovulationDate, 9, 0)
            
            if (triggerTime <= System.currentTimeMillis()) {
                // If current cycle ovulation is in the past, schedule for the next cycle's ovulation day
                val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
                ovulationDate = nextNextPeriodDate.minusDays(14)
                triggerTime = getTriggerTimeMillis(ovulationDate, 9, 0)
            }
            
            Log.d(TAG, "Calculated ovulation alert date: $ovulationDate at 9:00 AM (Trigger Time: $triggerTime ms)")
            scheduleAlarm(
                context = context,
                id = ID_OVULATION_ALERT,
                triggerTimeMs = triggerTime,
                title = "Ovulation day 🌿",
                body = "Today is your ovulation day. You are at peak fertility and energy."
            )
        } else {
            Log.d(TAG, "Ovulation alert is disabled. Cancelling alarm.")
            cancelAlarm(context, ID_OVULATION_ALERT)
        }

        // 3. Daily Log Reminder: every day at user-set time (default 9:00 PM / 21:00)
        if (storageHelper.dailyLogReminderEnabled) {
            val timeParts = storageHelper.dailyLogReminderTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 21
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            
            var triggerTime = getTriggerTimeMillis(LocalDate.now(), hour, minute)
            if (triggerTime < System.currentTimeMillis()) {
                // If past for today, schedule for tomorrow
                triggerTime = getTriggerTimeMillis(LocalDate.now().plusDays(1), hour, minute)
            }
            
            Log.d(TAG, "Calculated Daily Log reminder repeating trigger: Hour = $hour, Minute = $minute (Trigger Time: $triggerTime ms)")
            scheduleRepeatingAlarm(
                context = context,
                id = ID_DAILY_LOG,
                triggerTimeMs = triggerTime,
                intervalMs = AlarmManager.INTERVAL_DAY,
                title = "How are you feeling today? 💕",
                body = "Take 30 seconds to log your day in VelvetCycle."
            )
        } else {
            Log.d(TAG, "Daily log reminder is disabled. Cancelling alarm.")
            cancelAlarm(context, ID_DAILY_LOG)
        }

        // 4. Pad Reminder: 2 days before predicted next period at 9:05 AM (offset slightly from period reminder)
        if (storageHelper.padReminderEnabled) {
            val nextPeriodDate = CycleEngine.getNextPeriodDateProjected(lastPeriodStart, cycleLength)
            var reminderDate = nextPeriodDate.minusDays(2)
            var triggerTime = getTriggerTimeMillis(reminderDate, 9, 5)
            
            if (triggerTime <= System.currentTimeMillis()) {
                // If 2 days before current projected period is already in the past, schedule for the next cycle
                val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
                reminderDate = nextNextPeriodDate.minusDays(2)
                triggerTime = getTriggerTimeMillis(reminderDate, 9, 5)
            }
            
            Log.d(TAG, "Calculated pad reminder date: $reminderDate at 9:05 AM (Trigger Time: $triggerTime ms)")
            scheduleAlarm(
                context = context,
                id = ID_PAD_REMINDER,
                triggerTimeMs = triggerTime,
                title = "Stock up on pads 🛍️",
                body = "Your period is in 2 days. Make sure you have pads ready."
            )
        } else {
            Log.d(TAG, "Pad reminder is disabled. Cancelling alarm.")
            cancelAlarm(context, ID_PAD_REMINDER)
        }

        // 5. Day Before Period notifications
        if (storageHelper.periodReminderEnabled) {
            val lang = storageHelper.appLanguage
            val nextPeriodDate = CycleEngine.getNextPeriodDateProjected(lastPeriodStart, cycleLength)
            val dayBeforeDate = nextPeriodDate.minusDays(1)

            // Morning at 8:00 AM
            var morningTriggerTime = getTriggerTimeMillis(dayBeforeDate, 8, 0)
            if (morningTriggerTime <= System.currentTimeMillis()) {
                val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
                morningTriggerTime = getTriggerTimeMillis(nextNextPeriodDate.minusDays(1), 8, 0)
            }
            
            Log.d(TAG, "Calculated morning day-before period alert: ${dayBeforeDate} at 8:00 AM (Trigger Time: $morningTriggerTime ms)")
            scheduleAlarm(
                context = context,
                id = ID_DAY_BEFORE_MORNING,
                triggerTimeMs = morningTriggerTime,
                title = com.example.constants.Translations.t("notif_day_before_morning_title", lang),
                body = com.example.constants.Translations.t("notif_day_before_morning_body", lang)
            )

            // Evening at 9:00 PM (21:00)
            var eveningTriggerTime = getTriggerTimeMillis(dayBeforeDate, 21, 0)
            if (eveningTriggerTime <= System.currentTimeMillis()) {
                val nextNextPeriodDate = nextPeriodDate.plusDays(cycleLength.toLong())
                eveningTriggerTime = getTriggerTimeMillis(nextNextPeriodDate.minusDays(1), 21, 0)
            }
            
            Log.d(TAG, "Calculated evening day-before period alert: ${dayBeforeDate} at 9:00 PM (Trigger Time: $eveningTriggerTime ms)")
            scheduleAlarm(
                context = context,
                id = ID_DAY_BEFORE_EVENING,
                triggerTimeMs = eveningTriggerTime,
                title = com.example.constants.Translations.t("notif_day_before_evening_title", lang),
                body = com.example.constants.Translations.t("notif_day_before_evening_body", lang)
            )
        } else {
            Log.d(TAG, "Period reminder is disabled. Cancelling day-before alerts.")
            cancelAlarm(context, ID_DAY_BEFORE_MORNING)
            cancelAlarm(context, ID_DAY_BEFORE_EVENING)
        }
        
        Log.d(TAG, "All notifications successfully scheduled/updated with timezone safety.")
    }

    fun cancelAllNotifications(context: Context) {
        cancelAlarm(context, ID_PERIOD_REMINDER)
        cancelAlarm(context, ID_OVULATION_ALERT)
        cancelAlarm(context, ID_DAILY_LOG)
        cancelAlarm(context, ID_PAD_REMINDER)
        cancelAlarm(context, ID_DAY_BEFORE_MORNING)
        cancelAlarm(context, ID_DAY_BEFORE_EVENING)
        Log.d(TAG, "All notifications cancelled.")
    }

    fun rescheduleOnCycleUpdate(context: Context, storageHelper: StorageHelper) {
        cancelAllNotifications(context)
        scheduleAllNotifications(context, storageHelper)
    }

    private fun getTriggerTimeMillis(date: LocalDate, hour: Int, minute: Int): Long {
        val localDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun scheduleAlarm(
        context: Context,
        id: Int,
        triggerTimeMs: Long,
        title: String,
        body: String
    ) {
        Log.d(TAG, "scheduleAlarm invoked for ID: $id. Trigger time: $triggerTimeMs ms (${java.time.Instant.ofEpochMilli(triggerTimeMs)})")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notif_id", id)
            putExtra("notif_title", title)
            putExtra("notif_body", body)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
            Log.d(TAG, "Successfully registered single alarm ID: $id using AlarmManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error registering alarm ID: $id using AlarmManager", e)
        }
    }

    private fun scheduleRepeatingAlarm(
        context: Context,
        id: Int,
        triggerTimeMs: Long,
        intervalMs: Long,
        title: String,
        body: String
    ) {
        Log.d(TAG, "scheduleRepeatingAlarm invoked for ID: $id. Trigger time: $triggerTimeMs ms. Interval: $intervalMs ms")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notif_id", id)
            putExtra("notif_title", title)
            putExtra("notif_body", body)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                intervalMs,
                pendingIntent
            )
            Log.d(TAG, "Successfully registered repeating alarm ID: $id using AlarmManager.")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error registering repeating alarm ID: $id using AlarmManager", e)
        }
    }

    private fun cancelAlarm(context: Context, id: Int) {
        Log.d(TAG, "cancelAlarm invoked for ID: $id")
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Successfully cancelled and dismantled alarm ID: $id.")
            } else {
                Log.d(TAG, "No existing active alarm found for ID: $id when attempting to cancel.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm ID: $id", e)
        }
    }

    fun scheduleTestNotificationIfNeeded(context: Context, storageHelper: StorageHelper) {
        if (!storageHelper.notificationTestSent) {
            storageHelper.notificationTestSent = true
            val triggerTimeMs = System.currentTimeMillis() + 10_000L
            scheduleAlarm(
                context = context,
                id = 9999,
                triggerTimeMs = triggerTimeMs,
                title = "VelvetCycle notifications are working! 💕",
                body = "You will now get period reminders, ovulation alerts, and pad reminders."
            )
            Log.d(TAG, "Scheduled test notification 10 seconds from now.")
        }
    }
}
