package com.example.smartalarm.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.smartalarm.InitModel

class AlarmReceiver() : BroadcastReceiver() {
    //var model: SmartAlarmModel = null //TODO get SmartAlarms model i here

    override fun onReceive(context: Context, intent: Intent) {

        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val alarmID = intent?.getIntExtra("EXTRA_ALARM_ID", 0) ?: return
        val eventID = intent?.getIntExtra("EXTRA_EVENT_ID", 0) ?: return
        Log.d("TAG", "Alarm Triggered: $message $alarmID $eventID")
        if (context == null) return
        if (intent == null) return
        /*
        val startIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return

        startIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
           // Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

        startIntent.putExtra("EXTRA_MESSAGE", message)
        startIntent.putExtra("Extra_AlarmID", alarmID)
        startIntent.putExtra("Extra_EventID", eventID)
        context.startActivity(startIntent)
        // */
        var extras : Bundle? = intent.extras ?: return;
        val alarmBroadcastIntent : Intent = Intent("broadCastName");

        alarmBroadcastIntent.putExtra("EXTRA_MESSAGE", message)
        alarmBroadcastIntent.putExtra("EXTRA_ALARM_ID", alarmID)
        alarmBroadcastIntent.putExtra("EXTRA_EVENT_ID", eventID)
        context.sendBroadcast(alarmBroadcastIntent)
    }
}