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

}


class SmartAlarmModel {
    var alarms = MutableStateFlow(
        listOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(5, "s")
        )
    )
}