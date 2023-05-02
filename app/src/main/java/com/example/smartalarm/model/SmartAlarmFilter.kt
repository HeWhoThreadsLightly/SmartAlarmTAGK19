package com.example.smartalarm.model

import com.example.smartalarm.SmartAlarmCalendarEvent

enum class SmartAlarmFilterType{

    CalendarName,
    Organizer,
    Title,
    Location,
    Description,
    ColorRGB,
    StartTime,
    EndTime,
    Duration,
    All_Day,
    RecurrenceRule,
    RecurrenceDates,
    RecurrenceExceptionRule,
    RecurrenceExceptionDates,
    Availability
}


class SmartAlarmFilter(alarm: SmartAlarmAlarm, type : SmartAlarmFilterType){
    val alarm: SmartAlarmAlarm = alarm
    var filterType : SmartAlarmFilterType = type
    var active : Boolean = true
        set(value){
            alarm.filtersUpdated = true
            field = value
        }
    var filter : String = ".*"
        set(value){
            filterAsRegex = value.toRegex()
            alarm.filtersUpdated = true
            field = value
        }
    private var filterAsRegex : Regex = ".*".toRegex()
    fun filter(event: SmartAlarmCalendarEvent): Boolean {
        if(!active){
            return true
        }
        var str = event.eventData[filterType]
        if(str == null){
            str = ""
        }
        return str.matches(filterAsRegex)
    }
}