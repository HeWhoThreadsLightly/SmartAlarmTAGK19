package com.example.smartalarm

import kotlinx.coroutines.flow.MutableStateFlow
enum class SmartAlarmStartType{
    before,
    after,
    at
}
enum class SmartAlarmFilterType{
    calanderName,
    eventTitle,
    discription,
    startTime,
    endTime,
    duration,
    color
}

class SmartAlarmFilter(type : SmartAlarmFilterType){
    var filterType : SmartAlarmFilterType = type
    var active : Boolean = true
    var filter : String = ".*"
}

class SmartAlarmAlarm(i: Int, s: String) {
    val id: Int = i
    var name: String = s
    var startType: SmartAlarmStartType = SmartAlarmStartType.before
    var filters : MutableList<SmartAlarmFilter> = mutableListOf(
        SmartAlarmFilter(SmartAlarmFilterType.calanderName),
        SmartAlarmFilter(SmartAlarmFilterType.eventTitle),
        SmartAlarmFilter(SmartAlarmFilterType.discription),
        SmartAlarmFilter(SmartAlarmFilterType.startTime),
        SmartAlarmFilter(SmartAlarmFilterType.endTime),
        SmartAlarmFilter(SmartAlarmFilterType.duration),
        SmartAlarmFilter(SmartAlarmFilterType.color)
    )

}


class SmartAlarmModel {
    var alarms = MutableStateFlow(
        listOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(5, "s")
        )
    )
}