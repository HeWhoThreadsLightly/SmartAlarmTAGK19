package com.example.smartalarm.graphics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smartalarm.model.SmartAlarmFilterMatch
import com.example.smartalarm.model.SmartAlarmModel

@Composable
fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    beforeRow: (@Composable (rowIndex: Int) -> Unit)? = null,
    afterRow: (@Composable (rowIndex: Int) -> Unit)? = null,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        LazyColumn(state = verticalLazyListState) {
            items(rowCount) { rowIndex ->
                Column {
                    beforeRow?.invoke(rowIndex)

                    Row(modifier = rowModifier) {
                        (0 until columnCount).forEach { columnIndex ->
                            Box(modifier = Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)

                                val existingWidth = columnWidths[columnIndex] ?: 0
                                val maxWidth = maxOf(existingWidth, placeable.width)

                                if (maxWidth > existingWidth) {
                                    columnWidths[columnIndex] = maxWidth
                                }

                                layout(width = maxWidth, height = placeable.height) {
                                    placeable.placeRelative(0, 0)
                                }
                            }) {
                                cellContent(columnIndex, rowIndex)
                            }
                        }
                    }

                    afterRow?.invoke(rowIndex)
                }
            }
        }
    }
}

@Composable
fun renderAlarmFilters(navController: NavHostController, model: SmartAlarmModel, id: Int) {
    var alarm = model.alarms.find { it.id == id }
    if (alarm == null) {
        Text("Id $id not found", modifier = Modifier.background(MaterialTheme.colorScheme.error))
        return
    }
    var activeFilters = alarm.activeFilters()
    @Composable
    fun decidedBackgroundColor(columnIndex : Int, rowIndex : Int): Color {
        if(rowIndex == 0 || rowIndex == 1){
            return MaterialTheme.colorScheme.surfaceVariant
        }
        var event = alarm.parsedEvents[rowIndex + 2]
        var filterType = activeFilters[columnIndex].filterType
        var match = event.filterResults[filterType]

        return when(match){
            SmartAlarmFilterMatch.Unknown -> MaterialTheme.colorScheme.surfaceVariant
            SmartAlarmFilterMatch.Matches -> Color.Green
            SmartAlarmFilterMatch.Fails -> Color.Red
            null -> MaterialTheme.colorScheme.error
        }
    }
    @Composable
    fun decidedTextColor(columnIndex : Int, rowIndex : Int): Color {
        if(rowIndex == 0 || rowIndex == 1){
            return MaterialTheme.colorScheme.onSurface
        }
        var event = alarm.parsedEvents[rowIndex + 2]
        var filterType = activeFilters[columnIndex].filterType
        var match = event.filterResults[filterType]

        return when(match){
            SmartAlarmFilterMatch.Unknown -> MaterialTheme.colorScheme.onSurface
            SmartAlarmFilterMatch.Matches -> Color.Black
            SmartAlarmFilterMatch.Fails -> Color.White
            null -> MaterialTheme.colorScheme.onSurface
        }
    }

    fun decidedText(columnIndex : Int, rowIndex : Int): String {

        var filter = activeFilters[columnIndex]

        return if(rowIndex == 0){
            filter.filterType.name
        }else if(rowIndex == 1){
            filter.filter
        }else{
            var event = alarm.parsedEvents[rowIndex + 1]
            event.event.eventData[filter.filterType]?: "NULL"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column() {
            Row() {
                Text(alarm.name + " filter matches")
            }
            Row() {

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Table(
                        modifier = Modifier.matchParentSize(),
                        columnCount = activeFilters.count(),
                        rowCount = alarm.parsedEvents.count() + 2,
                        cellContent = { columnIndex, rowIndex ->
                            Row(
                                modifier = Modifier
                                    .background(
                                        decidedBackgroundColor(columnIndex, rowIndex),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                                    .padding(8.dp)
                            ) {
                                Text(text = decidedText(columnIndex, rowIndex), color = decidedTextColor(columnIndex, rowIndex))
                            }

                        })

                }
            }
        }

    }
}