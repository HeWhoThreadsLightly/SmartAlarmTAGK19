package com.example.smartalarm

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
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

open class SmartAlarmAction(tmp : String){
    var placeholder : String = tmp
    open fun begin(){
        Log.d("TAG", "Base start smart alarm action called")
    }
    open fun stop(){
        Log.d("TAG", "Base stop smart alarm action called")

    }
}

class ActionPlayYoutube (tmp : String): SmartAlarmAction(tmp){
    fun watchYoutubeVideo(id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            //startActivity(appIntent)//TODO make it call
        } catch (ex: ActivityNotFoundException) {
            //startActivity(webIntent)//TODO make it call
        }
    }
    override fun begin(){
        Log.d("TAG", "trying to start youtube video $placeholder" )
        watchYoutubeVideo(placeholder)
    }
    override fun stop(){

    }
}

class ActionDelay(tmp : String) : SmartAlarmAction(tmp){
    override fun begin(){
        Log.d("TAG", "start of $placeholder delay" )
    }
    override fun stop(){

    }
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
    var events : MutableList<SmartAlarmAction> = mutableListOf(
        SmartAlarmAction("Set volume to 10%"),
        ActionDelay("10 seconds"),
        ActionPlayYoutube("ZqJfqIwpXZ8"),
        ActionDelay("10 seconds")
    )

}


class SmartAlarmModel {
    var alarms : MutableList<SmartAlarmAlarm> =
        mutableListOf<SmartAlarmAlarm>(
            SmartAlarmAlarm(5, "Example"),
            SmartAlarmAlarm(6, "Example2")
        )

}