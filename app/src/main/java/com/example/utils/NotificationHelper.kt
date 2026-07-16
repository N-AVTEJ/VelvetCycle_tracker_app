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
        
        val lastPeriodStart = storageHelper.lastPeriodStart
        val cycleLength = storageHelper.cycleLength

        // 1. Period Reminder: 2 days before predicted next period date at 9:00 AM
        if (storageHelper.periodReminderEnabled) {
            val nextPeriodDate = lastPeriodStart.plusDays(cycleLength.toLong())
            val reminderDate = nextPeriodDate.minusDays(2)
            val triggerTime = getTriggerTimeMillis(reminderDate, 9, 0)
            
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    context = context,
                    id = ID_PERIOD_REMINDER,
                    triggerTimeMs = triggerTime,
                    title = "Period coming soon 🌸",
                    body = "Your period is expected in 2 days. Take care of yourself."
                )
            }
        } else {
            cancelAlarm(context, ID_PERIOD_REMINDER)
        }

        // 2. Ovulation Alert: on ovulation day at 9:00 AM
        if (storageHelper.ovulationAlertEnabled) {
            val ovulationDate = lastPeriodStart.plusDays((cycleLength - 14).toLong())
            val triggerTime = getTriggerTimeMillis(ovulationDate, 9, 0)
            
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    context = context,
                    id = ID_OVULATION_ALERT,
                    triggerTimeMs = triggerTime,
                    title = "Ovulation day 🌿",
                    body = "Today is your ovulation day. You are at peak fertility and energy."
                )
            }
        } else {
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

            scheduleRepeatingAlarm(
                context = context,
                id = ID_DAILY_LOG,
                triggerTimeMs = triggerTime,
                intervalMs = AlarmManager.INTERVAL_DAY,
                title = "How are you feeling today? 💕",
                body = "Take 30 seconds to log your day in VelvetCycle."
            )
        } else {
            cancelAlarm(context, ID_DAILY_LOG)
        }

        // 4. Pad Reminder: 2 days before predicted next period at 9:05 AM (offset slightly from period reminder)
        if (storageHelper.padReminderEnabled) {
            val nextPeriodDate = lastPeriodStart.plusDays(cycleLength.toLong())
            val reminderDate = nextPeriodDate.minusDays(2)
            val triggerTime = getTriggerTimeMillis(reminderDate, 9, 5)
            
            if (triggerTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    context = context,
                    id = ID_PAD_REMINDER,
                    triggerTimeMs = triggerTime,
                    title = "Stock up on pads 🛍️",
                    body = "Your period is in 2 days. Make sure you have pads ready."
                )
            }
        } else {
            cancelAlarm(context, ID_PAD_REMINDER)
        }

        // 5. Day Before Period notifications
        if (storageHelper.periodReminderEnabled) {
            val lang = storageHelper.appLanguage
            val nextPeriodDate = CycleEngine.getNextPeriodDateProjected(lastPeriodStart, cycleLength)
            val dayBeforeDate = nextPeriodDate.minusDays(1)

            // Morning at 8:00 AM
            val morningTriggerTime = getTriggerTimeMillis(dayBeforeDate, 8, 0)
            if (morningTriggerTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    context = context,
                    id = ID_DAY_BEFORE_MORNING,
                    triggerTimeMs = morningTriggerTime,
                    title = com.example.constants.Translations.t("notif_day_before_morning_title", lang),
                    body = com.example.constants.Translations.t("notif_day_before_morning_body", lang)
                )
            } else {
                cancelAlarm(context, ID_DAY_BEFORE_MORNING)
            }

            // Evening at 9:00 PM (21:00)
            val eveningTriggerTime = getTriggerTimeMillis(dayBeforeDate, 21, 0)
            if (eveningTriggerTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    context = context,
                    id = ID_DAY_BEFORE_EVENING,
                    triggerTimeMs = eveningTriggerTime,
                    title = com.example.constants.Translations.t("notif_day_before_evening_title", lang),
                    body = com.example.constants.Translations.t("notif_day_before_evening_body", lang)
                )
            } else {
                cancelAlarm(context, ID_DAY_BEFORE_EVENING)
            }
        } else {
            cancelAlarm(context, ID_DAY_BEFORE_MORNING)
            cancelAlarm(context, ID_DAY_BEFORE_EVENING)
        }
        
        Log.d(TAG, "All notifications successfully scheduled/updated.")
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
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm ID: $id", e)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling repeating alarm ID: $id", e)
        }
    }

    private fun cancelAlarm(context: Context, id: Int) {
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
