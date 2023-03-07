package com.example.smartalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartalarm.ui.theme.SmartAlarmTheme

class MainActivity : ComponentActivity() {
    var model : SmartAlarmModel = SmartAlarmModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
