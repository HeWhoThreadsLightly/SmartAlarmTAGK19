package com.example.smartalarm
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

class SmartAlarmUI {

}

@Composable
fun renderFilter(filter: SmartAlarmFilter){
    var textRegex by remember { mutableStateOf(TextFieldValue(filter.filter)) }
    Text(text = filter.filterType.name)
    TextField(
        value = textRegex,
        onValueChange = {
        textRegex = it
        filter.filter = it.text
    })
}

@Composable
fun VerticalReorderList() {
    val data = remember { mutableStateOf(List(100) { "$it" }) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        item(){

        }
        items(data.value) { item ->
            ReorderableItem(state, key = item) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Column(
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Text(item)
                }
            }
        }
    }
}

@Composable
fun renderEvent(event : SmartAlarmEvent){
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape)
    ){
        Text(event.placeholder)
        Button(
            modifier = Modifier,
            onClick = {

            }) {
            Text("Discard")
        }
    }
}

@Composable
fun renderAlarm(){
    var alarm = SmartAlarmAlarm(5, "Example")
    var textName by remember { mutableStateOf(TextFieldValue(alarm.name)) }
    var textStart by remember { mutableStateOf(TextFieldValue("90")) }
    val data = remember{ mutableStateOf(alarm.events) }
    val dataOld = remember { mutableStateOf(List(10) { "$it" }) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            Log.d("TAG", "renderAlarm: moved from ${this[from.index -1]} at ${from.index -1 } to ${this[to.index -1]} at ${to.index -1}")
            add(to.index - 1, removeAt(from.index - 1))//Note library indexes by 1
        }
    })

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            item(){
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
                    TextField(value = textStart, modifier = Modifier.fillMaxWidth(), onValueChange = {textStart = it})
                    Text(text = "Event in calendar")
                    Text(text = "Filters")
                    alarm.filters.forEach{
                        renderFilter(filter = it)
                    }

                    Text(text = "Alarm sequence")
                }
            }
            items(data.value) { item ->
                ReorderableItem(state, key = item) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        renderEvent(item)
                    }
                }
            }
        }
    }

}
