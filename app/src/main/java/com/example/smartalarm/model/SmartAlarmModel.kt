package com.example.smartalarm.model

import android.Manifest
import android.app.AlarmManager

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.smartalarm.MainActivity
import com.example.smartalarm.SmartAlarmCalendar
import java.util.*

enum class SmartAlarmStartType {
    Before,
    After,
    At
}

var globalNextID: Int = 0

class SmartAlarmAlarm(model: SmartAlarmModel, id: Int, name: String) {
    val model: SmartAlarmModel = model
    val id: Int = id
    var name: String = name
    var filtersUpdated: Boolean = true
    var parsedEvents: MutableList<SmartAlarmParsedEvent> = mutableListOf()
    var alarmEvents: MutableMap<Int, AlarmItem> = mutableMapOf()
    var startType: SmartAlarmStartType = SmartAlarmStartType.Before
        set(value) {
            updateEventStartTimes()
            field = value
        }
    var startMinutes: Long = 5
        set(value) {
            updateEventStartTimes()
            field = value
        }
    var filters: EnumMap<SmartAlarmFilterType, SmartAlarmFilter> =
        EnumMap(SmartAlarmFilterType.values().associateWith { SmartAlarmFilter(this, it) })
    var actions: MutableList<SmartAlarmAction> = mutableListOf(
        SetVolume(model, 10),
        ActionDelay(model, 10),
        ActionPlayYoutube(model, "ZqJfqIwpXZ8"),
        ActionDelay(model, 10)
    )

    fun activeFilters(): List<SmartAlarmFilter> {
        return filters.values.filter { it.active }
    }
    private fun updateDisplayedNextAlarmTime() {
        alarmEvents.forEach() { (id, alarmItem) ->
            model.scheduler.cancel(alarmItem)
        }
        alarmEvents.clear()
        parsedEvents.forEach() {
            if (it.matchesAll == SmartAlarmFilterMatch.Matches) {
                var instant = it.startTime.toInstant().toEpochMilli()
                var alarmItem = AlarmItem(
                    instant,
                    "Alarm sequence scheduled start from $name",
                    id,
                    globalNextID++
                )
                alarmEvents[alarmItem.eventID] = alarmItem
                model.scheduler.scehdule(alarmItem)
            }
        }
    }

    fun runAlarmSequence(step: Int) {

        for (i in step..actions.count()) {
            var action = actions[i]
            if (action is ActionDelay) {
                if (i != actions.count() - 1) {//if not last action
                    var instant = System.currentTimeMillis() + action.delaySeconds * 1000;
                    var alarmItem = AlarmItem(
                        instant,
                        "Alarm sequence ${action.delaySeconds} second delay",
                        id,
                        globalNextID++
                    )
                    alarmItem.step = i+1
                    alarmEvents[alarmItem.eventID] = alarmItem
                    model.scheduler.scehdule(alarmItem)
                    return
                }
            } else {
                action.begin()
            }

        }
    }

    private fun updateMatchingEvents() {
        var parsed = mutableListOf<SmartAlarmParsedEvent>()
        model.calendar.events.forEach {
            parsed.add(SmartAlarmParsedEvent(this, it, filters))
        }

        parsedEvents = parsed
        updateDisplayedNextAlarmTime()
    }

    private fun updateEventStartTimes() {
        parsedEvents.forEach() {
            it.updateStartDate(this)
        }
        updateDisplayedNextAlarmTime()
    }

    fun update() {
        updateMatchingEvents()
    }
    fun triggerEvent(eventID: Int){
        val alarmEvent = alarmEvents[eventID] ?: return
        alarmEvents.remove(eventID)
        runAlarmSequence(alarmEvent.step)

    }
}

class SmartAlarmModel(
    context: MainActivity,
    alarmManager: AlarmManager
) {
    var context: MainActivity = context
    var scheduler: SmartAlarmAndroidAlarmScheduler = SmartAlarmAndroidAlarmScheduler(context, alarmManager)
    var receiver: AlarmReceiver = AlarmReceiver()
    var requestPermissionLauncher = context.registerForActivityResult(
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
    var alarms: MutableList<SmartAlarmAlarm> =
        mutableListOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(this, globalNextID++, "Example"),
            SmartAlarmAlarm(this, globalNextID++, "Example2")
        )

    private fun updateInternal() {
        calendar.update()
        alarms.forEach() {
            it.update()
        }
    }

    fun update() {
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
                    Manifest.permission.READ_CALENDAR
                )
            }
        }
    }

    fun receiveMessage(message: String, alarmID: Int, eventID: Int) {
        alarms.forEach(){
            if(it.id == alarmID){
                it.triggerEvent(eventID)
                return
            }
        }
    }
}