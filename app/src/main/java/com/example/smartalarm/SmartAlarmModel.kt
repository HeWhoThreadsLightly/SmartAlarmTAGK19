package com.example.smartalarm

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.SystemClock
import android.provider.CalendarContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import java.util.*

enum class SmartAlarmStartType{
    Before,
    After,
    At
}
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
var SmartAlarmActionGlobalNexID = 0
open class SmartAlarmAction(var model: SmartAlarmModel){
    val id: Int = SmartAlarmActionGlobalNexID++

    open fun begin(){
        Log.d("TAG", "Base start smart alarm action called")
    }
    open fun stop(){
        Log.d("TAG", "Base stop smart alarm action called")

    }
}

class ActionPlayYoutube (model: SmartAlarmModel, id : String): SmartAlarmAction(model){
    var video = id
    override fun begin(){
        Log.d("TAG", "trying to start youtube video $video" )

        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$video"))
        val webIntent : Intent = Uri.parse("http://www.youtube.com/watch?v=$video").let {
            Intent(Intent.ACTION_VIEW, it)
        }

        var i =    Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$video")
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

class ActionDelay(model: SmartAlarmModel, private val delaySeconds: Long) : SmartAlarmAction(model) {

    override fun begin() {
        Log.d("TAG", "start of ${delaySeconds}s delay")
        SystemClock.sleep(delaySeconds * 1000)
        Log.d("TAG", "${delaySeconds}s delay finished")
    }

    override fun stop() {
        // implementation of stop method
    }
}

class SetVolume(model: SmartAlarmModel, private val volume: Int) : SmartAlarmAction(model) {

    override fun begin() {
        setVolume(model.context, volume)
    }

    override fun stop() {}

    private fun setVolume(context: Context, volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        for (stream in AudioManager.STREAM_VOICE_CALL..AudioManager.STREAM_NOTIFICATION) {
            val maxVolume = audioManager.getStreamMaxVolume(stream)
            val newVolume = (maxVolume * volume) / 100
            audioManager.setStreamVolume(stream, newVolume, 0)
        }
    }
}





class SmartAlarmAlarm(model :SmartAlarmModel, id: Int, name: String) {
    val model: SmartAlarmModel = model
    val id: Int = id
    var name: String = name
    var filtersUpdated : Boolean = true
    var matchingEvents : MutableList<SmartAlarmParsedEvent> = mutableListOf()
    var startType: SmartAlarmStartType = SmartAlarmStartType.Before
        set(value){
            updateEventStartTimes()
            field = value
        }
    var startMinutes : Long = 5
        set(value){
            updateEventStartTimes()
            field = value
        }
    var filters : EnumMap<SmartAlarmFilterType, SmartAlarmFilter> = EnumMap(SmartAlarmFilterType.values().associateWith { SmartAlarmFilter(this, it) })
    var actions : MutableList<SmartAlarmAction> = mutableListOf(
        SmartAlarmAction(model),
        SetVolume(model, 10),
        ActionDelay(model,10),
        ActionPlayYoutube(model,"ZqJfqIwpXZ8"),
        ActionDelay(model,10)
    )
    private fun applyFiltersToEvent(event : SmartAlarmCalendarEvent): Boolean {
        filters.values.forEach(){
            if(!it.filter(event)){
                return false
            }
        }
        return true
    }
    fun updateDisplayedNextAlarmTime(){
        //TODO may need to push the alarm update to ui or event service
    }
    fun updateMatchingEvents(){
        var matching = mutableListOf<SmartAlarmParsedEvent>()
        model.calendar.events.forEach{
            if(applyFiltersToEvent(it)){
                matching.add(SmartAlarmParsedEvent(this, it))
            }
        }
        matchingEvents = matching
        updateDisplayedNextAlarmTime()
    }

    fun updateEventStartTimes(){
        matchingEvents.forEach(){
            it.updateStartDate(this)
        }
        updateDisplayedNextAlarmTime()
    }
    fun update(){
        updateMatchingEvents()
    }
}

class SmartAlarmModel(
    context: MainActivity
) {
    var context : MainActivity = context
    var requestPermissionLauncher =  context.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            updateInternal()
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }
    var calendar: SmartAlarmCalendar = SmartAlarmCalendar(context)
    var alarms : MutableList<SmartAlarmAlarm> =
        mutableListOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(this,5, "Example"),
            SmartAlarmAlarm(this,6, "Example2")
        )
    private fun updateInternal(){
        calendar.update()
        alarms.forEach(){
            it.update()
        }
    }
    fun update(){
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                updateInternal()
            }
            shouldShowRequestPermissionRationale(context, Manifest.permission.READ_CALENDAR) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CALENDAR)
            }
        }
    }
}