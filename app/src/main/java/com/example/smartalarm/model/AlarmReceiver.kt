package com.example.smartalarm.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.smartalarm.InitModel
import com.example.smartalarm.MainActivity

class AlarmReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: return
        val alarmID = intent.getIntExtra("EXTRA_ALARM_ID", 0)
        val eventID = intent.getIntExtra("EXTRA_EVENT_ID", 0)
        Log.d("TAG", "Alarm Triggered: $message $alarmID $eventID")

        if (context != null) {
            // Handle the received data here instead of re-broadcasting it
            // (e.g., start a specific activity, update UI, or trigger some action).
            // Example: Start a specific activity with the received data
            val startIntent = Intent(context, MainActivity::class.java)
            startIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startIntent.putExtra("EXTRA_MESSAGE", message)
            startIntent.putExtra("EXTRA_ALARM_ID", alarmID)
            startIntent.putExtra("EXTRA_EVENT_ID", eventID)
            context.startActivity(startIntent)
        }
    }
}