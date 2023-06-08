package com.example.smartalarm.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver() : BroadcastReceiver() {
    //var model: SmartAlarmModel = null //TODO get SmartAlarms model i here

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("Extra_Message") ?: return
        val alarmID = intent?.getIntExtra("Extra_AlarmID", 0) ?: return
        val eventID = intent?.getIntExtra("Extra_EventID", 0) ?: return
        println("Alarm Triggered: $message $alarmID $eventID")
        //model.receiveMessage(message, alarmID, eventID)
    }
}