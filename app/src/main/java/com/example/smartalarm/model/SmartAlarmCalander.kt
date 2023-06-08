package com.example.smartalarm

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import com.example.smartalarm.model.*


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


class SmartAlarmCalendar(private val context: Context) {

    // The list of calendar events.
    var events: List<SmartAlarmCalendarEvent> = listOf()

    private fun fetchCalendarNames(): Map<Long, String> {
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
        cursor?.use { cu ->
            Log.d("TAG", "Fetched calendar")
            cu.columnNames.forEach { Log.d("TAG", "Column $it") }
            val indexName = cu.getColumnIndex(CalendarContract.Calendars.NAME)
            val indexID = cu.getColumnIndex(CalendarContract.Calendars._ID)
            if (indexName != -1 && indexID != -1) {
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
    private fun fetchFeed(): List<SmartAlarmCalendarEvent> {
        var latestEvents = mutableListOf<SmartAlarmCalendarEvent>()
        var calendarsNames = fetchCalendarNames()

        // Get a ContentResolver object to query the calendar.
        val cr: ContentResolver = context.contentResolver

        // Define an array of columns to retrieve for each event.
        val projection: Array<String> = SmartAlarmCalendarFilterMap.values.toTypedArray()

        // Define a filter to only retrieve events that start after the current time.
        val selection = null //Match all dates  "${CalendarContract.Events.DTSTART} >= ?"
        val selectionArgs =
            null // Match all dates: Array<String> = arrayOf("${System.currentTimeMillis()}")

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
        cursor?.use { cu ->
            Log.d("TAG", "Fetched calendar")
            cu.columnNames.forEach { Log.d("TAG", "Column $it") }
            while (cu.moveToNext()) {
                Log.d("TAG", "Fetched Event $cu")
                // Try to create a new calendar event object and set its properties.
                val newEvent = SmartAlarmCalendarEvent(cu, calendarsNames)
                if (newEvent != null) {
                    Log.d("TAG", "Created new event with data {${newEvent.print()}}")
                    latestEvents.add(newEvent)
                } else {
                    Log.d("TAG", "Event was missing columns")
                }
            }
        }

        return latestEvents
    }

    fun update() {
        events = fetchFeed()
    }
}

