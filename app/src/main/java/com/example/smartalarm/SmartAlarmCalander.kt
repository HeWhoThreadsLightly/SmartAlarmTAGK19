package com.example.smartalarm

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
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

class SmartAlarmCalendar(private val context: Context) {
    // The version number of the calendar, incremented each time it is refreshed.
    var calendarVersion : Long = 0

    // The list of calendar events.
    var events : MutableList<SmartAlarmCalendarEvent> = mutableListOf()

    // This method refreshes the calendar events by querying the device's calendar.
    fun refrechCalendar() {
        // Increment the calendar version number.
        calendarVersion++

        // Get a ContentResolver object to query the calendar.
        val cr: ContentResolver = context.contentResolver

        // Define an array of columns to retrieve for each event.
        val projection: Array<String> = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.STATUS,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

        // Define a filter to only retrieve events that start after the current time.
        val selection = "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs: Array<String> = arrayOf("${System.currentTimeMillis()}")

        // Sort the results by start time in ascending order.
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        // Query the calendar using the filter and sort order.
        val cursor: Cursor? = cr.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        // Clear the list of events.
        events.clear()

        // Iterate över the cursor to create new SmartAlarmCalendarEvent objects for each event.
        cursor?.use {
            while (it.moveToNext()) {
                // Get the values for each column from the cursor.
                val summary: String = it.getString(it.getColumnIndex(CalendarContract.Events.TITLE))
                val description: String = it.getString(it.getColumnIndex(CalendarContract.Events.DESCRIPTION))
                val location: String = it.getString(it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION))
                val status: String = it.getString(it.getColumnIndex(CalendarContract.Events.STATUS))
                val startTimeMillis: Long = it.getLong(it.getColumnIndex(CalendarContract.Events.DTSTART))
                val endTimeMillis: Long = it.getLong(it.getColumnIndex(CalendarContract.Events.DTEND))

                // Create a new calendar event object and set its properties.
                val event = SmartAlarmCalendarEvent(summary, description, location, status)
                event.startTime = Date(startTimeMillis)
                event.endTime = Date(endTimeMillis)

                events.add(event)
            }
        }

        //TODO trigger update of cached results
    }
}

