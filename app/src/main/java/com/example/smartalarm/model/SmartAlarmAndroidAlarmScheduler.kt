package com.example.smartalarm.model


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class SmartAlarmAndroidAlarmScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    //private val alarmManager = context.getSystemService(AlarmManager::class.java)//TODO caches here

    override fun scehdule(item: AlarmItem) {
        //val c = android.content.Context.ALARM_SERVICE
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", item.message)
            putExtra("EXTRA_ALARM_ID", item.alarmID)
            putExtra("EXTRA_EVENT_ID", item.eventID)
            // V. I think this should call SmartAlarmAction.begin() here.
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

    override fun cancel(item: AlarmItem) {
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