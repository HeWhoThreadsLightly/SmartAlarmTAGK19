package com.example.smartalarm.model

import android.os.Bundle

data class AlarmItem(
    val timeEpochMilli: Long,
    val message: String,
    val alarmID: Int,
    val eventID: Int,
){
    var step: Int = 0
}
