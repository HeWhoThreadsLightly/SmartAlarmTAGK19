package com.example.smartalarm

import android.app.AlarmManager
import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartalarm.graphics.renderAlarm
import com.example.smartalarm.graphics.renderAlarmFilters
import com.example.smartalarm.graphics.renderMain
import com.example.smartalarm.model.SmartAlarmModel
import com.example.smartalarm.ui.theme.SmartAlarmTheme


fun InitModel(context: MainActivity, alarmManager: AlarmManager): SmartAlarmModel {
    Log.d("TAG", "Started load sequence")
    try { // try to load saved settings

        Log.d("TAG", "Loading settings")
        var prefs: SharedPreferences? =
            context.getSharedPreferences("smartAlarm", ComponentActivity.MODE_PRIVATE)
        var jsonStr = prefs?.getString("json", null)

        if (jsonStr != null) {

            //Log.d("TAG", "Loaded $jsonStr")
            val model = SmartAlarmModel(context, alarmManager, jsonStr)

            Log.d("TAG", "Parsed settings")

            return model
        } else {
            Log.d("TAG", "Failed to load settings")
            return SmartAlarmModel(context, alarmManager)
        }
    } catch (err: Exception) {

        Log.d("TAG", "Error loading settings")
        throw err
        return SmartAlarmModel(context, alarmManager)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var alarmManager: AlarmManager


    lateinit var model: SmartAlarmModel

    //@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmManager =
            getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager//TODO caches here
        model = InitModel(this, alarmManager)
        setContent {
            model.navController = rememberNavController()
            takeInitialIntent()
            registerReceiver(broadcastReceiver,  IntentFilter("broadCastName"));
            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog. Save the return value, an instance of
            // ActivityResultLauncher. You can use either a val, as shown in this snippet,
            // or a lateinit var in your onAttach() or onCreate() method.


            SmartAlarmTheme {
                // A surface container using the 'background' color from the theme
                AppScreen(model = model)
            }
            model.startAutoUpdateCalendar()
        }
    }

    @Composable
    fun AppScreen(model: SmartAlarmModel) {
        var navController: NavHostController = rememberNavController()
        model.navController = navController
        NavHost(
            navController = navController,
            startDestination = "viewAll"
        ) {
            composable("viewAll") {
                renderMain(navController, model)
            }

            composable("ViewOne/{id}") {
                val id = it.arguments!!.getString("id")!!.toInt()
                renderAlarm(navController, model, id)
            }

            composable("ViewOneFilters/{id}") {
                val id = it.arguments!!.getString("id")!!.toInt()
                renderAlarmFilters(navController, model, id)
            }
        }
    }
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val message = intent?.getStringExtra("Extra_Message") ?: return
            val alarmID = intent?.getIntExtra("Extra_AlarmID", 0) ?: return
            val eventID = intent?.getIntExtra("Extra_EventID", 0) ?: return
            Log.d("TAG", "Intent2 received: $message $alarmID $eventID")
            model.receiveMessage(message, alarmID, eventID)
        }
    }
    fun takeInitialIntent() {
        val message = intent?.getStringExtra("Extra_Message") ?: return
        val alarmID = intent?.getIntExtra("Extra_AlarmID", 0) ?: return
        val eventID = intent?.getIntExtra("Extra_EventID", 0) ?: return
        Log.d("TAG", "Intent1 received: $message $alarmID $eventID")
        model.receiveMessage(message, alarmID, eventID)
    }

    override fun onPause() {
        super.onPause()
        val JSONstr: String = model.serialize()

        //Log.d("TAG", "Saving : $JSONstr")
        val prefs: SharedPreferences =
            getSharedPreferences("smartAlarm", ComponentActivity.MODE_PRIVATE)

        val editPrefs: SharedPreferences.Editor = prefs.edit()
        editPrefs.putString("json", JSONstr)
        editPrefs.commit()
        Log.d("TAG", "Saved settings")


    }

}
