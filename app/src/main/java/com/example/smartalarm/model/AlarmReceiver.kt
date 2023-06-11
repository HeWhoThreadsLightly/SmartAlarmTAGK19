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

        val message = intent?.getStringExtra("Extra_Message") ?: return
        val alarmID = intent?.getIntExtra("Extra_AlarmID", 0) ?: return
        val eventID = intent?.getIntExtra("Extra_EventID", 0) ?: return
        Log.d("TAG", "Alarm Triggered: $message $alarmID $eventID")
        if (context == null) return
        if (intent == null) return
        /*
        val startIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return

        startIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
           // Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

        startIntent.putExtra("Extra_Message", message)
        startIntent.putExtra("Extra_AlarmID", alarmID)
        startIntent.putExtra("Extra_EventID", eventID)
        context.startActivity(startIntent)
        // */
        var extras : Bundle? = intent.extras ?: return;
        var i : Intent = Intent("broadCastName");

        i.putExtra("Extra_Message", message)
        i.putExtra("Extra_AlarmID", alarmID)
        i.putExtra("Extra_EventID", eventID)
        context.sendBroadcast(i)
    }
}