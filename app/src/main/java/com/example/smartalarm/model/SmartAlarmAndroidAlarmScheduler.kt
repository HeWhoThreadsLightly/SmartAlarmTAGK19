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
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("Extra_Message", item.message)
            putExtra("Extra_AlarmID", item.alarmID)
            putExtra("Extra_EventID", item.eventID)
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