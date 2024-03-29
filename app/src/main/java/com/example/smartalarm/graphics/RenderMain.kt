package com.example.smartalarm.graphics

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.smartalarm.model.SmartAlarmAlarm
import com.example.smartalarm.model.SmartAlarmModel
import com.example.smartalarm.model.globalNextID
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@SuppressLint("MutableCollectionMutableState")
@Composable
fun RenderMain(navController: NavHostController, model: SmartAlarmModel) {

    val data = remember { mutableStateOf(model.alarms) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            Log.d(
                "TAG",
                "renderAlarm: moved from ${this[from.index - 1]} at ${from.index - 1} to ${this[to.index - 1]} at ${to.index - 1}"
            )
            add(to.index - 1, removeAt(from.index - 1))//Note library indexes by 1
        }
        model.alarms.add(to.index - 1, model.alarms.removeAt(from.index - 1))//Note library indexes by 1
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
            item(key = "Header") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RectangleShape)

                ) {

                    Text("Smart Alarm", fontSize = 26.sp, modifier = Modifier.padding(8.dp))
                    Row {
                        Button(
                            modifier = Modifier,
                            onClick = {
                                model.update()
                            }) {
                            Text("Update calendar")
                        }

                        Button(
                            modifier = Modifier,
                            onClick = {
                                val newID = globalNextID++
                                model.alarms.add(SmartAlarmAlarm(model, newID, "New alarm"))
                                navController.navigate("view_one/${newID}")

                            }) {
                            Text("New Alarm")
                        }
                    }

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
                        RenderAlarmItem(navController, item)
                    }
                }
            }
        }
    }
}