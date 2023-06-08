package com.example.smartalarm.model

enum class SmartAlarmFilterType {

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


enum class SmartAlarmFilterMatch {
    Unknown,
    Matches,
    Fails
}

class SmartAlarmFilter(alarm: SmartAlarmAlarm, type: SmartAlarmFilterType) {
    val alarm: SmartAlarmAlarm = alarm
    var filterType: SmartAlarmFilterType = type
    var active: Boolean = listOf(
        SmartAlarmFilterType.CalendarName,
        SmartAlarmFilterType.Title,
        SmartAlarmFilterType.Description,
        SmartAlarmFilterType.StartTime
    ).contains(type)
        set(value) {
            alarm.filtersUpdated = true
            field = value
        }
    var filter: String = ".*"
        set(value) {
            filterAsRegex = value.toRegex()
            alarm.filtersUpdated = true
            field = value
        }
    private var filterAsRegex: Regex = ".*".toRegex()
    fun filter(event: SmartAlarmCalendarEvent): SmartAlarmFilterMatch {
        if (!active) {
            return SmartAlarmFilterMatch.Unknown
        }
        var str = event.eventData[filterType]
        if (str == null) {
            str = ""
        }
        if (str.matches(filterAsRegex)) {
            return SmartAlarmFilterMatch.Matches
        } else {
            return SmartAlarmFilterMatch.Fails
        }
    }
}