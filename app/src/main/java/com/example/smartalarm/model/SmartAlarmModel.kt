package com.example.smartalarm.model

import android.Manifest
import android.app.AlarmManager

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.smartalarm.MainActivity
import com.example.smartalarm.SmartAlarmCalendar
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

enum class SmartAlarmStartType {
    Before,
    After,
    At
}

var globalNextID: Int = 0
var configAddNotMatchingToFilterList: Boolean = true
fun SmartAlarmAlarm(model: SmartAlarmModel, json: JSONObject): SmartAlarmAlarm {
    var alarm = SmartAlarmAlarm(model, json.getInt("id"), json.getString("name"))

    alarm.startType = SmartAlarmStartType.valueOf(json.getString("startType"))
    alarm.startMinutes = json.getInt("startMinutes")


    var filtersJ = json.getJSONArray("filters")
    for (i in 0 until filtersJ.length()) {
        var filter = SmartAlarmFilter(alarm, filtersJ.getJSONObject(i))
        alarm.filters[filter.filterType] = filter
    }

    var actionsJ = json.getJSONArray("actions")
    for (i in 0 until actionsJ.length()) {
        alarm.actions.add(SmartAlarmAction(alarm, actionsJ.getJSONObject(i)))
    }

    var alarmsJ = json.getJSONArray("alarms")
    for (i in 0 until alarmsJ.length()) {
        var alarmItem = AlarmItem(alarmsJ.getJSONObject(i))
        alarm.alarmEvents[alarmItem.eventID] = alarmItem
    }

    return alarm
}

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
    var startMinutes: Int = 5
        set(value) {
            updateEventStartTimes()
            field = value
        }
    var filters: EnumMap<SmartAlarmFilterType, SmartAlarmFilter> =
        EnumMap(SmartAlarmFilterType.values().associateWith { SmartAlarmFilter(this, it) })
    var actions: MutableList<SmartAlarmAction> = mutableListOf(
        //SetVolume(model, 10),
        //ActionDelay(model, 10),
        //ActionPlayYoutube(model, "ZqJfqIwpXZ8"),
        //ActionDelay(model, 10)
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
                if (instant >= System.currentTimeMillis()) {

                    var alarmItem = AlarmItem(
                        instant,
                        "Alarm sequence scheduled start from $name",
                        id,
                        globalNextID++
                    )
                    alarmEvents[alarmItem.eventID] = alarmItem
                    model.scheduler.schedule(alarmItem)
                }
            }
        }
    }

    fun runAlarmSequence(step: Int) {

        for (i in step until actions.count()) {
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
                    alarmItem.step = i + 1
                    alarmEvents[alarmItem.eventID] = alarmItem
                    model.scheduler.schedule(alarmItem)
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
            if (it.startTime.toInstant().toEpochMilli() >= System.currentTimeMillis()) {
                var event = SmartAlarmParsedEvent(this, it, filters)

                if (event.matchesAll == SmartAlarmFilterMatch.Matches || configAddNotMatchingToFilterList) {
                    parsed.add(event)
                }
            }
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

    fun triggerEvent(eventID: Int) {
        val alarmEvent = alarmEvents[eventID] ?: return
        alarmEvents.remove(eventID)
        runAlarmSequence(alarmEvent.step)

    }

    fun invalidate() {
        model.navController.popBackStack()
        model.navController.navigate("ViewOne/${id}")
    }

    fun serialize(): JSONObject {
        var json = JSONObject()
        json.put("name", name)
        json.put("id", id)
        json.put("startType", startType.name)
        json.put("startMinutes", startMinutes)

        var filtersJ = JSONArray()
        filters.forEach() {
            val obj = it.value.serialize()
            //Log.d("TAG", "saving ${obj.toString(4)}")
            filtersJ.put(obj)
        }
        json.put("filters", filtersJ)

        var actionsJ = JSONArray()
        actions.forEach() {
            val obj = it.serialize()
            //Log.d("TAG", "saving ${obj.toString(4)}")
            actionsJ.put(obj)
        }
        json.put("actions", actionsJ)

        var alarmsJ = JSONArray()
        alarmEvents.forEach() {
            val obj = it.value.serialize()
            //Log.d("TAG", "saving ${obj.toString(4)}")
            alarmsJ.put(obj)
        }
        json.put("alarms", alarmsJ)

        return json
    }
}

fun SmartAlarmModel(
    context: MainActivity,
    alarmManager: AlarmManager,
    JSONstr: String
): SmartAlarmModel {

    var json = JSONObject(JSONstr)
    globalNextID = json.getInt("globalNextID")
    var model = SmartAlarmModel(context, alarmManager)
    var alarmsJ = json.getJSONArray("alarms")

    //Log.d("TAG", "parsed alarms as ${alarmsJ.toString(4)}")
    for (i in 0 until alarmsJ.length()) {

        //Log.d("TAG", "Alarm index $i")

        //Log.d("TAG", "Alarm $i is ${alarmsJ.getJSONObject(i)}")
        model.alarms.add(SmartAlarmAlarm(model, alarmsJ.getJSONObject(i)))
    }
    return model
}

class SmartAlarmModel(
    context: MainActivity,
    alarmManager: AlarmManager
) {
    var context: MainActivity = context
    var scheduler: SmartAlarmAndroidAlarmScheduler =
        SmartAlarmAndroidAlarmScheduler(context, alarmManager)
    var receiver: AlarmReceiver = AlarmReceiver()
    lateinit var refreshAction : AlarmItem
    lateinit var navController: NavHostController
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
            //SmartAlarmAlarm(this, globalNextID++, "Example"),
            //SmartAlarmAlarm(this, globalNextID++, "Example2")
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

    fun startAutoUpdateCalendar(){
        Log.d("TAG", "Calendar auto refresh started")
        refreshAction = AlarmItem(System.currentTimeMillis() + 15 * 60 * 60 * 1000, "Calendar auto refresh", -1, 0)
        scheduler.schedule(refreshAction)
        update()
    }

    fun handleReceivedMessage(message: String, alarmID: Int, eventID: Int) {
        if(alarmID == -1){
            refreshAction = AlarmItem(System.currentTimeMillis() + 15 * 60 * 60 * 1000, "Calendar auto refresh", -1, 0)
            scheduler.schedule(refreshAction)
            update()
            return
        }
        alarms.forEach() {
            if (it.id == alarmID) {
                it.triggerEvent(eventID)
                return
            }
        }
    }

    fun serialize(): String {
        var json: JSONObject = JSONObject()
        json.put("globalNextID", globalNextID)

        var alarmsJ = JSONArray()
        alarms.forEach() {
            val obj = it.serialize()
            //Log.d("TAG", "saving ${obj.toString(4)}")
            alarmsJ.put(it.serialize())
        }
        json.put("alarms", alarmsJ)

        return json.toString(4)
    }

    fun remove(alarm: SmartAlarmAlarm) {

        alarm.alarmEvents.forEach() { (id, alarmItem) ->
            scheduler.cancel(alarmItem)
        }
        alarm.alarmEvents.clear()

        alarms.remove(alarm)

    }
}