package com.example.smartalarm.model

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.smartalarm.MainActivity
import com.example.smartalarm.SmartAlarmCalendar
import com.example.smartalarm.SmartAlarmCalendarEvent
import com.example.smartalarm.SmartAlarmParsedEvent
import java.util.*

enum class SmartAlarmStartType{
    Before,
    After,
    At
}


class SmartAlarmAlarm(model : SmartAlarmModel, id: Int, name: String) {
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