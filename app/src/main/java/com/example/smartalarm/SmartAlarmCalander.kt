package com.example.smartalarm

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // The list of calendar events.
    var events : List<SmartAlarmCalendarEvent> = listOf()

    // This method refreshes the calendar events by querying the device's calendar.
    private fun fetchFeed():List<SmartAlarmCalendarEvent> {
        var latestEvents = mutableListOf<SmartAlarmCalendarEvent>()
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

        fun getStringOrNull(it : Cursor, column : String):String?{
            var index = it.getColumnIndex(column)
            if(index == -1){
                return null
            }
            return it.getString(index)
        }
        fun getLongOrNull(it : Cursor, column : String):Long?{
            var index = it.getColumnIndex(column)
            if(index == -1){
                return null
            }
            return it.getLong(index)
        }
        // Iterate over the cursor to create new SmartAlarmCalendarEvent objects for each event.
        cursor?.use {
            Log.d("TAG", "Fetched calendar")
            it.columnNames.forEach { Log.d("TAG", "Column $it") }
            while (it.moveToNext()) {
                Log.d("TAG", "Fetched Event ${it}")
                // Get the values for each column from the cursor.
                val summary: String? = getStringOrNull(it, CalendarContract.Events.TITLE)
                val description: String? = getStringOrNull(it, CalendarContract.Events.DESCRIPTION)
                val location: String? = getStringOrNull(it, CalendarContract.Events.EVENT_LOCATION)
                val status: String? = getStringOrNull(it, CalendarContract.Events.STATUS)
                val startTimeMillis: Long? = getLongOrNull(it, CalendarContract.Events.DTSTART)
                val endTimeMillis: Long? = getLongOrNull(it, CalendarContract.Events.DTEND)
                // Create a new calendar event object and set its properties.
                if(summary != null && description != null && location != null && status != null && startTimeMillis != null && endTimeMillis != null){

                    val event = SmartAlarmCalendarEvent(summary, description, location, status)
                    event.startTime = Date(startTimeMillis)
                    event.endTime = Date(endTimeMillis)

                    latestEvents.add(event)
                }else{
                    Log.d("TAG", "Event was missing columns")
                }
            }
        }

        return  latestEvents
    }
    fun update(){
        events = fetchFeed()
    }
}

