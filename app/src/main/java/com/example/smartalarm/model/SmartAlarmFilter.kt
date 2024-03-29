package com.example.smartalarm.model

import org.json.JSONObject

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

fun smartAlarmFilter(alarm: SmartAlarmAlarm, json: JSONObject): SmartAlarmFilter {
    val filter = SmartAlarmFilter(alarm, SmartAlarmFilterType.valueOf(json.getString("filterType")))
    filter.active = json.getBoolean("active")
    filter.filter = json.getString("filter")

    return filter
}

class SmartAlarmFilter(val alarm: SmartAlarmAlarm, type: SmartAlarmFilterType) {
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
        return if (str.matches(filterAsRegex)) {
            SmartAlarmFilterMatch.Matches
        } else {
            SmartAlarmFilterMatch.Fails
        }
    }

    fun serialize(): JSONObject {
        val json = JSONObject()
        json.put("filterType", filterType.name)
        json.put("active", active)
        json.put("filter", filter)
        return json
    }
}