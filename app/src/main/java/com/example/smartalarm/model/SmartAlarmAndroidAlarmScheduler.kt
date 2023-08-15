package com.example.smartalarm.model


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class SmartAlarmAndroidAlarmScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager
)  {


    fun schedule(item: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
            putExtra("EXTRA_ALARM_ID", item.alarmID)
            putExtra("EXTRA_EVENT_ID", item.eventID)
        }


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            item.timeEpochMilli,
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        )
    }

     fun cancel(item: AlarmItem) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                item.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            )
        )
    }
}