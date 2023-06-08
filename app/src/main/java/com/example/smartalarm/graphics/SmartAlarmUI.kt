package com.example.smartalarm.graphics

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartalarm.model.*


@Composable
fun AppScreen(model: SmartAlarmModel) {
    val navController = rememberNavController()
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

        composable("CreateToDo") {
            //createToDoTask(navController = navController)

        }
    }
}

