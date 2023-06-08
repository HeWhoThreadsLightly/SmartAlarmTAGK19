package com.example.smartalarm

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartalarm.graphics.AppScreen
import com.example.smartalarm.model.AlarmItem
import com.example.smartalarm.model.SmartAlarmAndroidAlarmScheduler
import com.example.smartalarm.model.SmartAlarmModel
import com.example.smartalarm.ui.theme.SmartAlarmTheme

class MainActivity : ComponentActivity() {
    var model : SmartAlarmModel = SmartAlarmModel(this)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduler = SmartAlarmAndroidAlarmScheduler(this)
        var alarmItem: AlarmItem? = null
        setContent {
            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog. Save the return value, an instance of
            // ActivityResultLauncher. You can use either a val, as shown in this snippet,
            // or a lateinit var in your onAttach() or onCreate() method.



            SmartAlarmTheme {
                // A surface container using the 'background' color from the theme
                AppScreen(model = model)
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SmartAlarmTheme {
        Greeting("Android")
    }
}