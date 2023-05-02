package com.example.smartalarm.graphics

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavHostController
import com.example.smartalarm.model.SmartAlarmModel
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun renderMain(navController: NavHostController, model: SmartAlarmModel){

    val data = remember { mutableStateOf(model.alarms) }
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
            item(key = "Header") {
                Column(
                    modifier = Modifier
                        //.verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error, RectangleShape)

                ) {

                    Text("Menu placeholder")
                    Button(
                        modifier = Modifier,
                        onClick = {
                            model.update()
                            //TODO open alarm in new context
                        }) {
                        Text("Update calendar")
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
                        renderAlarmItem(navController, item)
                    }
                }
            }
        }
    }
}