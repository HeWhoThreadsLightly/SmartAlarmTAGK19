package com.example.smartalarm.model

import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.util.Log
import java.util.*

class SmartAlarmCalendarEvent(
    startTime: Date,
    endTime: Date,
    eventData: EnumMap<SmartAlarmFilterType, String>
) {
    var startTime: Date = startTime // = ICSdateFormat.parse("20230222T120000Z")
    var endTime: Date = endTime // = ICSdateFormat.parse("20230222T120000Z")
    var eventData: EnumMap<SmartAlarmFilterType, String> = eventData

    fun print(): String {
        var str = "EVent\n"
        eventData.forEach() { (key, value) ->
            str += "\t{$key: $value},\n"
        }
        return str;
    }
}

fun SmartAlarmCalendarEvent(
    cursor: Cursor,
    calendarsNames: Map<Long, String>
): SmartAlarmCalendarEvent? {
    var startTime: Date? = null // ICSdateFormat.parse("20230222T120000Z")
    var endTime: Date? = null// = ICSdateFormat.parse("20230222T120000Z")
    var eventData: EnumMap<SmartAlarmFilterType, String> =
        EnumMap(SmartAlarmFilterType.values().associateWith { null })

    fun getStringRep(type: SmartAlarmFilterType): String? {
        var index = cursor.getColumnIndex(SmartAlarmCalendarFilterMap[type])
        val displayDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss eee 'V'ww")

        if (index == -1) {
            return null
        }
        when (type) {
            SmartAlarmFilterType.CalendarName -> {
                return calendarsNames[cursor.getLong(index)]
            }
            SmartAlarmFilterType.ColorRGB -> {
                return "0x" + cursor.getLong(index).toULong().toString(16)
            }
            SmartAlarmFilterType.StartTime -> {
                val dateInMillisSinceEpoch = cursor.getLong(index)
                startTime = Date(dateInMillisSinceEpoch)
                return displayDateFormat.format(startTime)
            }
            SmartAlarmFilterType.EndTime -> {
                val dateInMillisSinceEpoch = cursor.getLong(index)
                endTime = Date(dateInMillisSinceEpoch)
                return displayDateFormat.format(endTime)
            }
            SmartAlarmFilterType.All_Day -> {
                val allDay = cursor.getInt(index)
                when (allDay) {
                    0 -> {
                        return "Not all day"
                    }
                    1 -> {
                        return "All day"
                    }
                    else -> {
                        Log.d("TAG", "ALL_DAY boolean has non binary value $allDay")
                        return null
                    }
                }
            }
            SmartAlarmFilterType.Availability -> {
                val avalibility = cursor.getInt(index)
                when (avalibility) {
                    0 -> {
                        return "Busy"
                    }
                    1 -> {
                        return "Free"
                    }
                    2 -> {
                        return "Tentative"
                    }
                    else -> {
                        Log.d("TAG", "Unknown Availability type detected $avalibility")
                        return null
                    }
                }
            }
            else -> {//For data that is already a string
                return cursor.getString(index)
            }
        }
    }

    SmartAlarmFilterType.values().forEach {
        eventData.set(it, getStringRep(it))
    }
    if (startTime == null || endTime == null) {
        return null
    }
    //Compute values for null items
    if (eventData[SmartAlarmFilterType.Duration] == null) {
        //replace missing duration with one derived from start and end times formatted in duration in RFC2445 format
        val diff: Long = endTime!!.time - startTime!!.time

        var seconds = diff / 1000
        var minutes = seconds / 60
        var hours = minutes / 60
        var days = hours / 24
        seconds %= 60
        minutes %= 60
        hours %= 24


        var str = "P"
        if (days != 0L) {
            str += "" + days + "D"
        }
        if (hours != 0L || minutes != 0L || seconds != 0L) {
            str += "T"
        }
        if (hours != 0L) {
            str += "" + hours + "H"
        }
        if (minutes != 0L) {
            str += "" + minutes + "M"
        }
        if (seconds != 0L) {
            str += "" + seconds + "S"
        }

        eventData[SmartAlarmFilterType.Duration] = str
    }
    return SmartAlarmCalendarEvent(startTime!!, endTime!!, eventData)
}

class SmartAlarmParsedEvent(
    alarm: SmartAlarmAlarm,
    event: SmartAlarmCalendarEvent,
    filters: EnumMap<SmartAlarmFilterType, SmartAlarmFilter>
) {
    val event: SmartAlarmCalendarEvent = event
    val filterResults: EnumMap<SmartAlarmFilterType, SmartAlarmFilterMatch> =
        EnumMap(SmartAlarmFilterType.values().associateWith { null })
    var matchesAll: SmartAlarmFilterMatch = updateFilters(filters)
    var startTime: Date = parseStartDate(alarm)
    private fun parseStartDate(alarm: SmartAlarmAlarm): Date {
        when (alarm.startType) {
            SmartAlarmStartType.Before -> {
                var time = event.startTime.time - alarm.startMinutes * 60 * 1000
                return Date(time)
            }
            SmartAlarmStartType.After -> {
                var time = event.startTime.time + alarm.startMinutes * 60 * 1000
                return Date(time)
            }
            SmartAlarmStartType.At -> {
                val dayDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
                val instantDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
                var newDate: String = dayDateFormat.format(event.startTime)
                val hours = (alarm.startMinutes / 60) % 24
                val minutes = alarm.startMinutes % 60
                newDate += " " + hours.toString().padStart(2, '0') + ":" + minutes.toString()
                    .padStart(2, '0')
                return instantDateFormat.parse(newDate)
            }
        }
    }

    fun updateStartDate(alarm: SmartAlarmAlarm) {
        startTime = parseStartDate(alarm)
    }

    fun updateFilters(filters: EnumMap<SmartAlarmFilterType, SmartAlarmFilter>): SmartAlarmFilterMatch {
        filterResults.clear()
        Log.d("TAG", "Filtering: ${event.eventData[SmartAlarmFilterType.Title]} ")
        filters.values.forEach() {
            val res = it.filter(event)
            filterResults[it.filterType] = res
            Log.d(
                "TAG",
                "FilteringStep: ${it.filterType} data: ${event.eventData[it.filterType]} result: ${res.name}"
            )

        }

        if (filterResults.containsValue(SmartAlarmFilterMatch.Fails)) {
            matchesAll = SmartAlarmFilterMatch.Fails
        } else {
            matchesAll = SmartAlarmFilterMatch.Matches
        }
        Log.d("TAG", "Filtering result: ${matchesAll.name}")

        return matchesAll
    }
}