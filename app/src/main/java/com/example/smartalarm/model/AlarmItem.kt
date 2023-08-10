package com.example.smartalarm.model


import org.json.JSONObject

fun getAlarmItemFromJsonObject(json: JSONObject): AlarmItem {
    val alarm = AlarmItem(
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
        val json = JSONObject()
        json.put("timeEpochMilli", timeEpochMilli)
        json.put("message", message)
        json.put("alarmID", alarmID)
        json.put("eventID", eventID)
        json.put("step", step)
        return json
    }

    var step: Int = 0
}
