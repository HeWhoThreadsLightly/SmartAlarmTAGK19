package com.example.smartalarm

interface AlarmScheduler {
    fun scehdule(item: AlarmItem)
    fun cancel(item: AlarmItem)
}