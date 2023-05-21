package com.example.smartalarm.model

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import androidx.compose.runtime.Composable

open class SmartAlarmAction(var model: SmartAlarmModel, var simpelName: String){
    val id: Int = SmartAlarmActionGlobalNexID++
    open fun begin(){
        Log.d("TAG", "Base start smart alarm action called")
    }
    open fun stop(){
        Log.d("TAG", "Base stop smart alarm action called")

    }
    @Composable
    open fun renderAction(){
        Log.d("TAG", "Base render smart alarm action called")

        Row(
            modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape)
        ) {
            Text(simpelName)
            Button(
                modifier = Modifier,
                onClick = {
                    begin()
                }) {
                Text("Test")
            }
        }
    }
}


var SmartAlarmActionGlobalNexID = 0

class ActionPlayYoutube(model: SmartAlarmModel, id: String) : SmartAlarmAction(model, "ActionPlayYoutube") {
    private var videoId = id
    private val youtubeIntent: Intent by lazy {
        Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.google.android.youtube")
            data = Uri.parse("vnd.youtube:$videoId")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun begin() {
        Log.d("TAG", "Trying to start YouTube video $videoId")

        try {
            ContextCompat.startActivity(model.context, youtubeIntent, null)
        } catch (ex: ActivityNotFoundException) {
            Log.d("TAG", "YouTube app not found, trying web version")
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=$videoId"))
            try {
                ContextCompat.startActivity(model.context, webIntent, null)
            } catch (ex: ActivityNotFoundException) {
                Log.d("TAG", "Failed to start YouTube video")
            }
        }
    }

    override fun stop() {
        // Implementation of stop method
    }

    @Composable
    override fun renderAction(){
        Column(){
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape)
            ) {
                Text(simpelName)
                Button(
                    modifier = Modifier,
                    onClick = {
                        begin()
                    }) {
                    Text("Test")
                }
            }
            Row() {
                var textVideo by remember { mutableStateOf( TextFieldValue( videoId )) }
                TextField(
                    value = textVideo,
                    onValueChange = {
                        textVideo = it
                        videoId = it.text
                    }
                )
            }
        }

    }
}

class ActionDelay(model: SmartAlarmModel, private val delaySeconds: Long) : SmartAlarmAction(model, "ActionDelay") {

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
class SetVolume(model: SmartAlarmModel, private val volume: Int) : SmartAlarmAction(model, "SetVolume") {

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
