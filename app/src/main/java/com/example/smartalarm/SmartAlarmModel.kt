package com.example.smartalarm

import kotlinx.coroutines.flow.MutableStateFlow
enum class SmartAlarmStartType{
    Before,
    After,
    At
}
enum class SmartAlarmFilterType{
    CalendarName,
    EventTitle,
    Description,
    StartTime,
    EndTime,
    Duration,
    Color
}

class SmartAlarmFilter(type : SmartAlarmFilterType){
    var filterType : SmartAlarmFilterType = type
    var active : Boolean = true
    var filter : String = ".*"
}

class SmartAlarmEvent(tmp : String){
    var placeholder : String = tmp
}

class SmartAlarmAlarm(i: Int, s: String) {
    val id: Int = i
    var name: String = s
    var startType: SmartAlarmStartType = SmartAlarmStartType.Before
    var filters : MutableList<SmartAlarmFilter> = mutableListOf(
        SmartAlarmFilter(SmartAlarmFilterType.CalendarName),
        SmartAlarmFilter(SmartAlarmFilterType.EventTitle),
        SmartAlarmFilter(SmartAlarmFilterType.Description),
        SmartAlarmFilter(SmartAlarmFilterType.StartTime),
        SmartAlarmFilter(SmartAlarmFilterType.EndTime),
        SmartAlarmFilter(SmartAlarmFilterType.Duration),
        SmartAlarmFilter(SmartAlarmFilterType.Color)
    )
    var events : MutableList<SmartAlarmEvent> = mutableListOf(
        SmartAlarmEvent("Set volume to 10%"),
        SmartAlarmEvent("Start playlist mk12321%"),
        SmartAlarmEvent("Raise volume to 80% over 15 min"),
    )

}


class SmartAlarmModel {
    var alarms = MutableStateFlow(
        listOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(5, "s")
        )
    )
}