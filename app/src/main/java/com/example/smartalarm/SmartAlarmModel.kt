package com.example.smartalarm

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

import kotlinx.coroutines.flow.MutableStateFlow
import android.content.Context
import android.media.AudioManager

enum class SmartAlarmStartType{
    Before,
    After,
    At
}
enum class SmartAlarmFilterType{
    CalendarName,
    Summary,
    Description,
    StartTime,
    EndTime,
    Duration,
    Color,
    Status
}

class SmartAlarmFilter(alarm: SmartAlarmAlarm, type : SmartAlarmFilterType){
    fun filter(event: SmartAlarmCalendarEvent): Boolean {
        if(!active){
            return true
        }
        when(filterType){
            SmartAlarmFilterType.CalendarName -> TODO()
            SmartAlarmFilterType.Summary -> {
                return event.summary.matches(filter.toRegex())
            }
            SmartAlarmFilterType.Description -> {
                return event.description.matches(filter.toRegex())
            }
            SmartAlarmFilterType.StartTime -> {
                return event.startTimeString().matches(filter.toRegex())
            }
            SmartAlarmFilterType.EndTime ->{
                return event.endTimeString().matches(filter.toRegex())
            }
            SmartAlarmFilterType.Duration -> {
               return event.duration().matches(filter.toRegex())
            }
            SmartAlarmFilterType.Color -> TODO()
            SmartAlarmFilterType.Status -> {
                return event.status.matches(filter.toRegex())
            }
        }
    }
    val alarm: SmartAlarmAlarm = alarm
    var filterType : SmartAlarmFilterType = type
    var active : Boolean = true
        set(value){
            active = value
            alarm.filtersUpdated = true
        }
    var filter : String = ".*"
        set(value){
            filter = value
            alarm.filtersUpdated = true
        }
}

open class SmartAlarmAction(var model: SmartAlarmModel){
    open fun begin(){
        Log.d("TAG", "Base start smart alarm action called")
    }
    open fun stop(){
        Log.d("TAG", "Base stop smart alarm action called")

    }
}

class ActionPlayYoutube (model: SmartAlarmModel, id : String): SmartAlarmAction(model){
    var id = id
    override fun begin(){
        Log.d("TAG", "trying to start youtube video $id" )

        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent : Intent = Uri.parse("http://www.youtube.com/watch?v=$id").let {
            Intent(Intent.ACTION_VIEW, it)
        }

        var i =    Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            startActivity(model.context, appIntent, null)
        } catch (ex: ActivityNotFoundException) {
            try {
                startActivity(model.context, webIntent, null)
            }catch (ex: ActivityNotFoundException){
                Log.d("TAG", "Failed to start youtube video" )//TODO make UI popup
            }
        }
    }
    override fun stop(){

    }
}

class ActionDelay(model: SmartAlarmModel, placehloder : String) : SmartAlarmAction(model){
    var placeholder : String = placehloder
    override fun begin(){
        Log.d("TAG", "start of $placeholder delay" )
    }
    override fun stop(){

    }
}

class SetVolume(model: SmartAlarmModel, private val volume: Int) : SmartAlarmAction(model) {
        override fun begin() {
            setVolume(model.context, volume)
        }

        override fun stop() {}

        private fun setVolume(context: Context, volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val newVolume = (maxVolume * volume) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, 0)
    }
}

class SmartAlarmAlarm(model :SmartAlarmModel, id: Int, name: String) {
    val model: SmartAlarmModel = model
    val id: Int = id
    var name: String = name
    var filtersUpdated : Boolean = true
    var matchingEventsInvalid : Boolean = true
    var matchingEventsUpdated : Boolean = true
    var basedOnCalendarVersion : Long = 0
    var matchingEvents : MutableList<SmartAlarmParsedEvent> = mutableListOf()
    var startType: SmartAlarmStartType = SmartAlarmStartType.Before
        set(value){
            matchingEventsInvalid = true
            updateCache()
        }
    var startMinutes : Long = 5
        set(value){
            matchingEventsInvalid = true
            updateCache()
        }
    var filters : MutableList<SmartAlarmFilter> = mutableListOf(
        SmartAlarmFilter(this, SmartAlarmFilterType.CalendarName),
        SmartAlarmFilter(this, SmartAlarmFilterType.Summary),
        SmartAlarmFilter(this, SmartAlarmFilterType.Description),
        SmartAlarmFilter(this, SmartAlarmFilterType.StartTime),
        SmartAlarmFilter(this, SmartAlarmFilterType.EndTime),
        SmartAlarmFilter(this, SmartAlarmFilterType.Duration),
        SmartAlarmFilter(this, SmartAlarmFilterType.Color)
    )
    var actions : MutableList<SmartAlarmAction> = mutableListOf(
        SmartAlarmAction(model),
        SetVolume(model, 10),
        ActionDelay(model,"10 seconds"),
        ActionPlayYoutube(model,"ZqJfqIwpXZ8"),
        ActionDelay(model,"10 seconds")
    )
    private fun applyFiltersToEvent(event : SmartAlarmCalendarEvent): Boolean {
        filters.forEach(){
            if(!it.filter(event)){
                return false
            }
        }
        return true
    }
    fun applyFilters(){
        matchingEvents = mutableListOf()
        model.calendar.events.forEach{
            if(applyFiltersToEvent(it)){
                matchingEvents.add(SmartAlarmParsedEvent(this, it))
            }
        }
    }
    fun updateCache(){
        if(basedOnCalendarVersion != model.calendar.calendarVersion || filtersUpdated){
            matchingEventsUpdated = true
            basedOnCalendarVersion = model.calendar.calendarVersion
            applyFilters()
        }
        if(matchingEventsInvalid){
            matchingEventsInvalid = false
            matchingEventsUpdated = true
            matchingEvents.forEach(){
                it.updateStartDate(this)
            }
        }
        if(matchingEventsUpdated){
            //TODO schedule next start time
        }
    }
}

class SmartAlarmModel(context: MainActivity) {
    var context : MainActivity = context
    var calendar: SmartAlarmCalendar = SmartAlarmCalendar()
    var alarms : MutableList<SmartAlarmAlarm> =
        mutableListOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(this,5, "Example"),
            SmartAlarmAlarm(this,6, "Example2")
        )

}