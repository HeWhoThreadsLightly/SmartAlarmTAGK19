package com.example.smartalarm.graphics

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.ui.graphics.Color
import com.example.smartalarm.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenderStartWhen(alarm: SmartAlarmAlarm) {


    val (selectedOption, onOptionSelected) = remember { mutableStateOf(SmartAlarmStartType.Before) }

    var textStart by remember { mutableStateOf(TextFieldValue(alarm.startMinutes.toString())) }
    Text(text = "Start the alarm")

    Column {
        SmartAlarmStartType.values().forEach { index ->
            val text = index.toString()
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (index == selectedOption),
                        onClick = {
                            onOptionSelected(index)
                        }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (index == selectedOption),
                    onClick = { onOptionSelected(index) }
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    TextField(
        value = textStart,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = {
            textStart = it
            alarm.startMinutes = it.text.toIntOrNull()
                ?: 0 // TODO if selectedOption == SmartAlarmStartType.At interpret the value as a time of day like 13:45 in to minutes from midnight
        })
    when (selectedOption) {
        SmartAlarmStartType.Before -> Text("Minutes before the event")
        SmartAlarmStartType.After -> Text("Minutes after the event")
        SmartAlarmStartType.At -> Text("Minutes after midnight the same day")
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun RenderAlarm(navController: NavHostController, model: SmartAlarmModel, id: Int) {
    val alarm = model.alarms.find { it.id == id }
    if (alarm == null) {
        Text("Id $id not found", modifier = Modifier.background(MaterialTheme.colorScheme.error))
        return
    }
    var textName by remember { mutableStateOf(TextFieldValue(alarm.name)) }
    val alarmActionsState = remember { mutableStateOf(alarm.actions) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        alarmActionsState.value = alarmActionsState.value.toMutableList().apply {
            Log.d(
                "TAG",
                "RenderAlarm: moved from ${from.index} to ${to.index}"
            )

            if (to.index - 1 >= alarmActionsState.value.size) {
                return@apply
            }
            Log.d(
                "TAG",
                "renderAlarm: moved from ${this[from.index - 1]} at ${from.index - 1} to ${this[to.index - 1]} at ${to.index - 1}"
            )
            add(to.index - 1, removeAt(from.index - 1))//Note library indexes by 1
        }
        alarm.actions.add(to.index - 1, alarm.actions.removeAt(from.index - 1))
    })

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            item("Header") {
                Column(
                    modifier = Modifier
                        //.verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface, RectangleShape)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        onClick = {
                            model.remove(alarm)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Delete")
                    }
                    Text(text = "Alarm name")
                    TextField(value = textName, modifier = Modifier.fillMaxSize(), onValueChange = {
                        textName = it
                        alarm.name = it.text
                    })
                    RenderStartWhen(alarm)
                    Text(text = "Filters")
                    alarm.filters.values.forEach {
                        RenderFilter(filter = it)
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            // Handle button click, show a table of passing and failing filters on events from calendar
                            navController.navigate("view_one_filters/${id}")
                        }
                    ) {
                        Text("View filter results")
                    }

                    Text(text = "Alarm sequence")

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            alarm.actions.add(ActionDelay(alarm, 10))
                            navController.popBackStack()
                            navController.navigate("view_one/${id}")
                        }
                    ) {
                        Text("Add delay")
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            alarm.actions.add(SetVolume(alarm, 10))
                            alarm.invalidate()
                        }
                    ) {
                        Text("Add set volume")
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            alarm.actions.add(ActionPlayYoutube(alarm, "dQw4w9WgXcQ"))
                            alarm.invalidate()
                        }
                    ) {
                        Text("Add youtube")
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = {
                            alarm.runAlarmSequence(0)
                        }
                    ) {
                        Text("Test Action Sequence")
                    }
                }
            }
            items(alarmActionsState.value, { it.id }) { item ->
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surfaceTint)
                    ) {
                        //Text("Elevation = ${elevation.value}")
                        item.renderAction()
                    }
                }
            }
            item("Footer") {
                Box(modifier = Modifier.height(200.dp))
            }
        }
    }
}

@Composable
fun RenderAlarmItem(navController: NavHostController, item: SmartAlarmAlarm) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(4.dp)
            )
            .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
            .padding(16.dp)
            .fillMaxWidth(), // Make the item take up the entire row
        horizontalArrangement = Arrangement.SpaceBetween // Align children to the start and end
    ) {
        Text(item.name)
        Button(
            modifier = Modifier
                .padding(start = 20.dp) // Add padding to the button
                .fillMaxSize(), // Specify the size of the button
            onClick = {
                navController.navigate("view_one/${item.id}")
            }) {
            Text("Edit")
        }
    }
}



