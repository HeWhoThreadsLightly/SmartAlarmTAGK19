package com.example.smartalarm.graphics

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import kotlin.math.roundToInt
import android.media.Ringtone
import android.media.RingtoneManager
import com.example.smartalarm.model.SmartAlarmAlarm


fun smartAlarmAction(alarm: SmartAlarmAlarm, json: JSONObject): SmartAlarmAction {
    when (json.getString("type")) {
        "SmartAlarmAction" -> {
            return SmartAlarmAction(alarm, "Serialization error base class")
        }
        "ActionDelay" -> {
            return ActionDelay(alarm, json.getLong("delaySeconds"))
        }
        "SetVolume" -> {
            return SetVolume(alarm, json.getInt("volume"))
        }
        "ActionPlayYoutube" -> {
            return ActionPlayYoutube(alarm, json.getString("videoId"))
        }
        else -> {

            Log.d("TAG", "Unknown AlarmAction type")
            return SmartAlarmAction(alarm, "Serialization error unknown type")
        }
    }
}

open class SmartAlarmAction(var alarm: SmartAlarmAlarm, var simpleName: String) {
    val id: Int = SmartAlarmActionGlobalNexID++
    open fun begin() {
        Log.d("TAG", "Base start smart alarm action called")
    }

    open fun stop() {
        Log.d("TAG", "Base stop smart alarm action called")

    }

    @Composable
    open fun RenderAction() {
        Log.d("TAG", "Base render smart alarm action called")

        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                .padding(8.dp)
        ) {
            Text(simpleName)

            Row {
                Button(
                    onClick = {
                        alarm.actions.remove(this@SmartAlarmAction)
                        alarm.invalidate()
                    },
                    modifier = Modifier
                        .weight(0.5F)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )

                ) {
                    Text("Remove")
                }

                Button(
                    onClick = { begin() },
                    modifier = Modifier
                        .weight(0.5F)
                        .padding(start = 8.dp)

                ) {
                    Text("Test")
                }
            }
        }
    }

    open fun serialize(): JSONObject {
        val json = JSONObject()
        json.put("type", "SmartAlarmAction")
        return json
    }
}


var SmartAlarmActionGlobalNexID = 0

class ActionPlayYoutube(alarm: SmartAlarmAlarm, id: String) :
    SmartAlarmAction(alarm, "ActionPlayYoutube") {
    private var videoId = id
    private val youtubeIntent: Intent by lazy {
        Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.google.android.youtube")
            data = Uri.parse("vnd.youtube:$videoId")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun serialize(): JSONObject {
        val json = JSONObject()
        json.put("type", "ActionPlayYoutube")
        json.put("videoId", videoId)
        return json
    }

    override fun begin() {
        Log.d("TAG", "Trying to start YouTube video $videoId")

        try {
            ContextCompat.startActivity(alarm.model.context, youtubeIntent, null)
        } catch (ex: ActivityNotFoundException) {
            Log.d("TAG", "YouTube app not found, trying web version")
            val webIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=$videoId"))
            try {
                ContextCompat.startActivity(alarm.model.context, webIntent, null)
            } catch (ex: ActivityNotFoundException) {
                Log.d("TAG", "Failed to start YouTube video, playing default alarm sound")

                // Play the default notification sound (standard Android alarm sound)
                val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone: Ringtone = RingtoneManager.getRingtone(alarm.model.context, notification)
                ringtone.play()
            }
        }
    }

    override fun stop() {
        // implementation of stop method
    }

    @Composable
    override fun RenderAction() {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = simpleName,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
                var textVideo by remember { mutableStateOf(TextFieldValue(videoId)) }
                TextField(
                    value = textVideo,
                    onValueChange = {
                        textVideo = it
                        videoId = it.text
                    },
                    modifier = Modifier
                        .padding(start = 8.dp), // Add padding to the button
                    colors = TextFieldDefaults.textFieldColors(textColor = MaterialTheme.colorScheme.onSurface)
                )
                Spacer(modifier = Modifier.width(8.dp)) // Add a spacer for spacing between TextField and Button

                Row {
                    Button(
                        onClick = {
                            alarm.actions.remove(this@ActionPlayYoutube)
                            alarm.invalidate()
                        },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )

                    ) {
                        Text("Remove")
                    }

                    Button(
                        onClick = { begin() },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp)

                    ) {
                        Text("Test")
                    }
                }
            }

        }
    }
}

class ActionDelay(alarm: SmartAlarmAlarm, var delaySeconds: Long) :
    SmartAlarmAction(alarm, "ActionDelay") {

    override fun begin() {
        Log.d("TAG", "start of $delaySeconds s delay")
        CoroutineScope(Dispatchers.Main).launch {
            delay(delaySeconds * 1000)
            Log.d("TAG", "$delaySeconds s delay finished")
        }
    }

    override fun serialize(): JSONObject {
        val json = JSONObject()
        json.put("type", "ActionDelay")
        json.put("delaySeconds", delaySeconds)
        return json
    }

    override fun stop() {
        // implementation of stop method
    }

    @Composable
    override fun RenderAction() {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                //verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = simpleName,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
                var textSeconds by remember { mutableStateOf(TextFieldValue(delaySeconds.toString())) }
                TextField(
                    value = textSeconds,
                    onValueChange = {
                        textSeconds = it
                        delaySeconds = it.text.toLongOrNull() ?: 0
                    },
                    modifier = Modifier
                        .padding(start = 8.dp), // Add padding to the button
                    colors = TextFieldDefaults.textFieldColors(textColor = MaterialTheme.colorScheme.onSurface)
                )

                Row {
                    Button(
                        onClick = {
                            alarm.actions.remove(this@ActionDelay)
                            alarm.invalidate()
                        },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )

                    ) {
                        Text("Remove")
                    }

                    Button(
                        onClick = { begin() },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp)

                    ) {
                        Text("Test")
                    }
                }
            }

        }
    }
}

class SetVolume(alarm: SmartAlarmAlarm, private var volume: Int) :
    SmartAlarmAction(alarm, "SetVolume") {

    override fun begin() {
        setVolume(alarm.model.context, volume)
    }

    override fun serialize(): JSONObject {
        val json = JSONObject()
        json.put("type", "SetVolume")
        json.put("volume", volume)
        json.put("simpleName", simpleName)
        return json
    }

    override fun stop() {}

    private fun setVolume(context: Context, volume: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (maxVolume.toFloat() * volume.toFloat() / 100F)
        Log.d("TAG", "Setting volume to $newVolume, max is $maxVolume")
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.roundToInt(), 0)
    }

    @Composable
    override fun RenderAction() {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = simpleName,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                )
                var textVolume by remember { mutableStateOf(TextFieldValue(volume.toString())) }
                TextField(
                    value = textVolume,
                    onValueChange = {
                        textVolume = it
                        volume = it.text.toIntOrNull() ?: 0
                    },
                    modifier = Modifier
                        .padding(start = 8.dp), // Add padding to the button
                    colors = TextFieldDefaults.textFieldColors(textColor = MaterialTheme.colorScheme.onSurface)
                )
                Row {
                    Button(
                        onClick = {
                            alarm.actions.remove(this@SetVolume)
                            alarm.invalidate()
                        },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )

                    ) {
                        Text("Remove")
                    }

                    Button(
                        onClick = { begin() },
                        modifier = Modifier
                            .weight(0.5F)
                            .padding(start = 8.dp)

                    ) {
                        Text("Test")
                    }
                }
            }
        }
    }

}







