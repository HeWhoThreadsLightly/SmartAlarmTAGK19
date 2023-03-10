package com.example.smartalarm

import android.icu.text.SimpleDateFormat
import java.text.ParseException
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

val ICSdateFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")
val displayDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss eee 'V'ww")

class SmartAlarmCalendarEvent(summary : String, description: String, location: String, status : String){
    var startTime  : Date = ICSdateFormat.parse("20230222T120000Z")
    var endTime : Date = ICSdateFormat.parse("20230222T120000Z")
    var summary : String = summary
    var description : String = description
    var location : String = location
    var status : String = status
    fun duration(): String {
        val diff: Long = endTime.getTime() - startTime.getTime()
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return "$minutes"
    }
    fun endTimeString(): String {
        return displayDateFormat.format(endTime)
    }
    fun startTimeString(): String {
        return displayDateFormat.format(startTime)
    }
}

class SmartAlarmParsedEvent(alarm : SmartAlarmAlarm, event: SmartAlarmCalendarEvent){
    val event : SmartAlarmCalendarEvent = event
    var startTime : Date = parseStartDate(alarm)
    private fun parseStartDate(alarm : SmartAlarmAlarm): Date {
        when(alarm.startType){
            SmartAlarmStartType.Before -> {
                var time = event.startTime.time - alarm.startMinutes*60*1000
                return Date(time)
            }
            SmartAlarmStartType.After -> {
                var time = event.startTime.time + alarm.startMinutes*60*1000
                return Date(time)
            }
            SmartAlarmStartType.At -> {
                val dayDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
                val instantDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
                var newDate : String = dayDateFormat.format(event.startTime)
                val hours = (alarm.startMinutes / 60) % 24
                val minutes = alarm.startMinutes % 60
                newDate += " "  + hours.toString().padStart(2, '0') + ":" + minutes.toString().padStart(2, '0')
                return instantDateFormat.parse(newDate)
            }
        }
    }
    fun updateStartDate(alarm: SmartAlarmAlarm){
        startTime = parseStartDate(alarm)
    }
}

class SmartAlarmCalendar {
    var calendarVersion : Long = 0
    var events : MutableList<SmartAlarmCalendarEvent> =
        mutableListOf<SmartAlarmCalendarEvent>(
            SmartAlarmCalendarEvent("Test", "test ui", "Home", "CONFIRMED"),
            SmartAlarmCalendarEvent("TODO", "add new items to ui", "Home", "CONFIRMED"),
            SmartAlarmCalendarEvent("TODO", "add more todos to app", "School", "CONFIRMED"),
            SmartAlarmCalendarEvent("Test", "test how regex can be used", "School", "CONFIRMED"),
            SmartAlarmCalendarEvent("todo", "work on app", "School", "CONFIRMED")
        )
    fun refrechCalendar(){
        calendarVersion++
        events = mutableListOf()
        //TODO fetch new calendar data
        //TODO trigger update of cached results
    }
}
