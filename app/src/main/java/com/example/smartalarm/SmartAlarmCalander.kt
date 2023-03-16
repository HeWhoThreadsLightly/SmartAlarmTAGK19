package com.example.smartalarm

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.provider.CalendarContract
import android.util.Log
import java.time.Duration
import java.util.*


val ICSdateFormat: SimpleDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss")
val displayDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss eee 'V'ww")

var SmartAlarmCalendarFilterMap = mapOf<SmartAlarmFilterType, String>(
    /*
        The Calendars#_ID of the calendar the event belongs to. Column name.
        Type: INTEGER
        Constant Value: "calendar_id"
     */
    SmartAlarmFilterType.CalendarName to CalendarContract.Events.CALENDAR_ID,  //Type: INTEGER Constant Value: "calendar_id"
    /*
        Email of the organizer (owner) of the event. Column name.
        Type: STRING
        Constant Value: "organizer"
     */
    SmartAlarmFilterType.Organizer to CalendarContract.Events.ORGANIZER,
    /*
        The title of the event. Column name.
        Type: TEXT
        Constant Value: "title"
     */
    SmartAlarmFilterType.Title to CalendarContract.Events.TITLE,
    /*
        Where the event takes place. Column name.
        Type: TEXT
        Constant Value: "eventLocation"
     */
    SmartAlarmFilterType.Location to CalendarContract.Events.EVENT_LOCATION,
    /*
        The description of the event. Column name.
        Type: TEXT
        Constant Value: "description"
     */
    SmartAlarmFilterType.Description to CalendarContract.Events.DESCRIPTION,
    /*
        The color as an 8-bit ARGB integer value.
        Colors should specify alpha as fully opaque (eg 0xFF993322)
        as the alpha may be ignored or modified for display.
        It is reccomended that colors be usable with light (near white) text.
        Apps should not depend on that assumption, however. Column name.

        A secondary color for the individual event. This should only be updated by the sync adapter for a given account.
        Type: INTEGER
        Constant Value: "eventColor"
     */
    SmartAlarmFilterType.ColorRGB to CalendarContract.Events.EVENT_COLOR,
    /*
        The time the event starts in UTC millis since epoch. Column name.
        Type: INTEGER (long; millis since epoch)
        Constant Value: "dtstart"
     */
    SmartAlarmFilterType.StartTime to CalendarContract.Events.DTSTART,
    /*
        The time the event ends in UTC millis since epoch. Column name.
        Type: INTEGER (long; millis since epoch)
        Constant Value: "dtend"
     */
    SmartAlarmFilterType.EndTime to CalendarContract.Events.DTEND,
    /*
        The duration of the event in RFC2445 format. Column name.
        Type: TEXT (duration in RFC2445 format)
        Constant Value: "duration"
     */
    SmartAlarmFilterType.Duration to CalendarContract.Events.DURATION,
    /*
        Is the event all day (time zone independent). Column name.
        Type: INTEGER (boolean)
        Constant Value: "allDay"
     */
    SmartAlarmFilterType.All_Day to CalendarContract.Events.ALL_DAY,
    /*
        The recurrence rule for the event. Column name.
        Type: TEXT
        Constant Value: "rrule"
     */
    SmartAlarmFilterType.RecurrenceRule to CalendarContract.Events.RRULE,
    /*
        The recurrence dates for the event. Column name.
        Type: TEXT
        Constant Value: "rdate"
     */
    SmartAlarmFilterType.RecurrenceDates to CalendarContract.Events.RDATE,
    /*
        The recurrence exception rule for the event. Column name.
        Type: TEXT
        Constant Value: "exrule"
     */
    SmartAlarmFilterType.RecurrenceExceptionRule to CalendarContract.Events.EXRULE,
    /*
        The recurrence exception dates for the event. Column name.
        Type: TEXT
        Constant Value: "exdate"
     */
    SmartAlarmFilterType.RecurrenceExceptionDates to CalendarContract.Events.EXDATE,
    /*
        If this event counts as busy time or is still free time that can be scheduled over. Column name.
        Type: INTEGER (One of AVAILABILITY_BUSY(0), AVAILABILITY_FREE(1), AVAILABILITY_TENTATIVE(2))
        Constant Value: "availability"
     */
    SmartAlarmFilterType.Availability to CalendarContract.Events.AVAILABILITY,
)


class SmartAlarmCalendarEvent(startTime : Date, endTime : Date, eventData : EnumMap<SmartAlarmFilterType, String>){
    var startTime  : Date = startTime // = ICSdateFormat.parse("20230222T120000Z")
    var endTime : Date = endTime // = ICSdateFormat.parse("20230222T120000Z")
    var eventData : EnumMap<SmartAlarmFilterType, String> = eventData

    fun print(): String {
        var str = "EVent\n"
        eventData.forEach(){ (key, value) ->
            str += "\t{$key: $value},\n"
        }
        return str;
    }
}

fun SmartAlarmCalendarEvent(cursor: Cursor, calendarsNames: Map<Long, String>): SmartAlarmCalendarEvent?{
    var startTime  : Date? = null // ICSdateFormat.parse("20230222T120000Z")
    var endTime : Date? = null// = ICSdateFormat.parse("20230222T120000Z")
    var eventData : EnumMap<SmartAlarmFilterType, String> = EnumMap(SmartAlarmFilterType.values().associateWith { null })

    fun getStringRep(type: SmartAlarmFilterType):String? {
        var index = cursor.getColumnIndex(SmartAlarmCalendarFilterMap[type])
        if(index == -1){
            return null
        }
        when(type){
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
                when (allDay){
                    0 -> {return "Not all day"}
                    1 -> {return "All day"}
                    else ->{
                        Log.d("TAG", "ALL_DAY boolean has non binary value $allDay")
                        return null
                    }
                }
            }
            SmartAlarmFilterType.Availability -> {
                val avalibility = cursor.getInt(index)
                when (avalibility){
                    0 -> {return "Busy"}
                    1 -> {return "Free"}
                    2 -> {return "Tentative"}
                    else ->{
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
    if(startTime == null || endTime == null){
        return null
    }
    //Compute values for null items
    if(eventData[SmartAlarmFilterType.Duration] == null){
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
        if(days != 0L){
            str += "" + days + "D"
        }
        if(hours != 0L || minutes != 0L || seconds != 0L) {
            str += "T"
        }
        if(hours != 0L){
            str += "" + hours + "H"
        }
        if(minutes != 0L){
            str += "" + minutes + "M"
        }
        if(seconds != 0L){
            str += "" + seconds + "S"
        }

        eventData[SmartAlarmFilterType.Duration] = str
    }
    return SmartAlarmCalendarEvent(startTime!!, endTime!!, eventData)
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

    private fun fetchCalendarNames():Map<Long, String>{
        var calendarsNames = mutableMapOf<Long, String>()

        // Get a ContentResolver object to query the calendar.
        val cr: ContentResolver = context.contentResolver

        // Define an array of columns to retrieve for each calendar.
        val projection: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME
        )

        // Query the calendar using the filter and sort order.
        Log.d("TAG", "creating cursor")
        val cursor: Cursor? = cr.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        // Iterate over the cursor to create new SmartAlarmCalendarEvent objects for each event.
        Log.d("TAG", "Trying to use cursor for calendar")
        cursor?.use {cu->
            Log.d("TAG", "Fetched calendar")
            cu.columnNames.forEach { Log.d("TAG", "Column $it") }
            val indexName = cu.getColumnIndex(CalendarContract.Calendars.NAME)
            val indexID = cu.getColumnIndex(CalendarContract.Calendars._ID)
            if(indexName != -1 && indexID != -1){
                while (cu.moveToNext()) {
                    Log.d("TAG", "Fetched calendar $cu")
                    // Try to create a new calendar event object and set its properties.
                    calendarsNames.put(cu.getLong(indexID), cu.getString(indexName))
                }
            }
        }
        return calendarsNames
    }
    // This method refreshes the calendar events by querying the device's calendar.
    private fun fetchFeed():List<SmartAlarmCalendarEvent> {
        var latestEvents = mutableListOf<SmartAlarmCalendarEvent>()
        var calendarsNames = fetchCalendarNames()

        // Get a ContentResolver object to query the calendar.
        val cr: ContentResolver = context.contentResolver

        // Define an array of columns to retrieve for each event.
        val projection: Array<String> = SmartAlarmCalendarFilterMap.values.toTypedArray()

        // Define a filter to only retrieve events that start after the current time.
        val selection = null //Match all dates  "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs = null // Match all dates: Array<String> = arrayOf("${System.currentTimeMillis()}")

        // Sort the results by start time in ascending order.
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        // Query the calendar using the filter and sort order.
        Log.d("TAG", "creating cursor")
        val cursor: Cursor? = cr.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        // Iterate over the cursor to create new SmartAlarmCalendarEvent objects for each event.
        Log.d("TAG", "Trying to use cursor")
        cursor?.use {cu->
            Log.d("TAG", "Fetched calendar")
            cu.columnNames.forEach { Log.d("TAG", "Column $it") }
            while (cu.moveToNext()) {
                Log.d("TAG", "Fetched Event $cu")
                // Try to create a new calendar event object and set its properties.
                val newEvent = SmartAlarmCalendarEvent(cu, calendarsNames)
                if(newEvent != null ){
                    Log.d("TAG", "Created new event with data {${newEvent.print()}}")
                    latestEvents.add(newEvent)
                }else{
                    Log.d("TAG", "Event was missing columns")
                }
            }
        }

        return latestEvents
    }
    fun update(){
        events = fetchFeed()
    }
}

