package com.example.smartalarm.model

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*


open class SmartAlarmAction(var model: SmartAlarmModel){
    val id: Int = SmartAlarmActionGlobalNexID++

    open fun begin(){
        Log.d("TAG", "Base start smart alarm action called")
    }
    open fun stop(){
        Log.d("TAG", "Base stop smart alarm action called")

    }
}


var SmartAlarmActionGlobalNexID = 0

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
            ContextCompat.startActivity(model.context, appIntent, null)
        } catch (ex: ActivityNotFoundException) {
            try {
                ContextCompat.startActivity(model.context, webIntent, null)
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
        CoroutineScope(Dispatchers.Main).launch {
            delay(delaySeconds * 1000)
            Log.d("TAG", "${delaySeconds}s delay finished")
        }
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
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (maxVolume * volume) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }
}
