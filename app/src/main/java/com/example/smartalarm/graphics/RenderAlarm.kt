package com.example.smartalarm.graphics

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smartalarm.model.SmartAlarmAlarm
import com.example.smartalarm.model.SmartAlarmModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun renderAlarm(navController: NavHostController, model: SmartAlarmModel, id: Int) {
    var alarm = model.alarms.find { it.id == id }
    if (alarm == null){
        Text("Id $id not found", modifier = Modifier.background(MaterialTheme.colorScheme.error))
        return
    }
    var textName by remember { mutableStateOf(TextFieldValue(alarm.name)) }
    var textStart by remember { mutableStateOf(TextFieldValue("90")) }
    val data = remember { mutableStateOf(alarm.actions) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            Log.d(
                "TAG",
                "renderAlarm: moved from ${this[from.index - 1]} at ${from.index - 1} to ${this[to.index - 1]} at ${to.index - 1}"
            )
            add(to.index - 1, removeAt(from.index - 1))//Note library indexes by 1
        }
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
                        .background(MaterialTheme.colorScheme.error, RectangleShape)

                ) {
                    TextField(value = textName, modifier = Modifier.fillMaxSize(), onValueChange = {
                        textName = it
                        alarm.name = it.text
                    })
                    Text(text = "Start")
                    TextField(
                        value = textStart,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { textStart = it })
                    Text(text = "Event in calendar")
                    Text(text = "Filters")
                    alarm.filters.values.forEach {
                        renderFilter(filter = it)
                    }

                    Text(text = "Alarm sequence")
                }
            }
            items(data.value, { it.id }) { item ->
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        //Text("Elevation = ${elevation.value}")
                        renderAction(item)
                    }
                }
            }
        }
    }
}

@Composable
fun renderAlarmItem(navController: NavHostController, item: SmartAlarmAlarm){
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape)
    ) {
        Text(item.name)
        Button(
            modifier = Modifier,
            onClick = {
                navController.navigate("ViewOne/${item.id}")
                //TODO open alarm in new context
            }) {
            Text("Edit")
        }
    }
}