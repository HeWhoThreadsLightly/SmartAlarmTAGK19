package com.example.smartalarm.model

import android.os.Bundle
import android.util.Log
import org.json.JSONObject

fun getAlarmItemFromJsonObject(json: JSONObject): AlarmItem {
    var alarm = AlarmItem(
        json.getLong("timeEpochMilli"),
        json.getString("message"),
        json.getInt("alarmID"),
        json.getInt("eventID")
    )
    alarm.step = json.getInt("step")
    return alarm
}

data class AlarmItem(
    val timeEpochMilli: Long,
    val message: String,
    val alarmID: Int,
    val eventID: Int,
) {
    fun serialize(): JSONObject {
        var json = JSONObject()
        json.put("timeEpochMilli", timeEpochMilli)
        json.put("message", message)
        json.put("alarmID", alarmID)
        json.put("eventID", eventID)
        json.put("step", step)
        //Log.d("TAG, ","Saving alarmItem ${json.toString(4)}")
        return json
    }

    var step: Int = 0
}
