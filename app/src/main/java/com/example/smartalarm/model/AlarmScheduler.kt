package com.example.smartalarm.model

import com.example.smartalarm.model.AlarmItem

interface AlarmScheduler {
    fun scehdule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}