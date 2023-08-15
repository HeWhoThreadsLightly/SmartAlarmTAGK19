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
import com.example.smartalarm.graphics.RenderAlarm
import com.example.smartalarm.graphics.RenderAlarmFilters
import com.example.smartalarm.graphics.RenderMain
import com.example.smartalarm.model.Constants
import com.example.smartalarm.model.SmartAlarmModel
import com.example.smartalarm.ui.theme.SmartAlarmTheme


fun initModel(context: MainActivity, alarmManager: AlarmManager): SmartAlarmModel {
    Log.d("TAG", "Started load sequence")
    try { // try to load saved settings

        Log.d("TAG", "Loading settings")
        val prefs: SharedPreferences? =
            context.getSharedPreferences(Constants.SHARED_PREF_KEY, ComponentActivity.MODE_PRIVATE)
        val jsonStr = prefs?.getString("json", null)

        return if (jsonStr != null) {

            //Log.d("TAG", "Loaded $jsonStr")
            val model = SmartAlarmModel(context, alarmManager, jsonStr)

            Log.d("TAG", "Parsed settings")

            model
        } else {
            Log.d("TAG", "Failed to load settings")
            SmartAlarmModel(context, alarmManager)
        }
    } catch (err: Exception) {

        Log.d("TAG", "Error loading settings")
        throw err
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var alarmManager: AlarmManager


    lateinit var model: SmartAlarmModel

    //@RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager//TODO caches here
        model = initModel(this, alarmManager)
        setContent {
            model.navController = rememberNavController()
            handleInitialIntent()
            registerReceiver(broadcastReceiver,  IntentFilter("broadCastName"))
            // Register the permissions callback, which handles the user's response to the
            // system permissions dialog. Save the return value, an instance of
            // ActivityResultLauncher. You can use either a val, as shown in this snippet,
            // or a late init var in your onAttach() or onCreate() method.


            SmartAlarmTheme {
                // A surface container using the 'background' color from the theme
                AppScreen(model = model)
            }
            model.startAutoUpdateCalendar()
        }
    }

    @Composable
    fun AppScreen(model: SmartAlarmModel) {
        val navController: NavHostController = rememberNavController()
        model.navController = navController
        NavHost(
            navController = navController,
            startDestination = "view_all"
        ) {
            composable("view_all") {
                RenderMain(navController, model)
            }

            composable("view_one/{id}") {
                val id = it.arguments!!.getString("id")!!.toInt()
                RenderAlarm(navController, model, id)
            }

            composable("view_one_filters/{id}") {
                val id = it.arguments!!.getString("id")!!.toInt()
                RenderAlarmFilters(model, id)
            }
        }
    }
    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val message = intent.getStringExtra("EXTRA_MESSAGE") ?: return
            val alarmID = intent.getIntExtra("EXTRA_ALARM_ID", 0)
            val eventID = intent.getIntExtra("EXTRA_EVENT_ID", 0)
            Log.d("TAG", "Intent2 received: $message $alarmID $eventID")
            model.handleReceivedMessage(message, alarmID, eventID)
        }
    }
    private fun handleInitialIntent() {
        val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
        val alarmID = intent?.getIntExtra("EXTRA_ALARM_ID", 0) ?: return
        val eventID = intent?.getIntExtra("EXTRA_EVENT_ID", 0) ?: return
        Log.d("TAG", "Intent1 received: $message $alarmID $eventID")
        model.handleReceivedMessage(message, alarmID, eventID)
    }

    override fun onPause() {
        super.onPause()
        val jsonStr: String = model.serialize()

        //Log.d("TAG", "Saving : $jsonStr")
        val prefs: SharedPreferences =
            getSharedPreferences(Constants.SHARED_PREF_KEY, MODE_PRIVATE)

        val editPrefs: SharedPreferences.Editor = prefs.edit()
        editPrefs.putString("json", jsonStr)
        editPrefs.apply()
        Log.d("TAG", "Saved settings")


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }


}
