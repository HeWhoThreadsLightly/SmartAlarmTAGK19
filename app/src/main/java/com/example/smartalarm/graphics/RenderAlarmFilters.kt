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
fun RenderAlarmFilters(model: SmartAlarmModel, id: Int) {
    val alarm = model.alarms.find { it.id == id }
    if (alarm == null) {
        Text("Id $id not found", modifier = Modifier.background(MaterialTheme.colorScheme.error))
        return
    }
    val activeFilters = alarm.activeFilters()

    @Composable
    fun DecideBackgroundColor(columnIndex: Int, rowIndex: Int): Color {
        if (rowIndex == 0 || rowIndex == 1) {
            return MaterialTheme.colorScheme.surfaceVariant
        }
        val event = alarm.parsedEvents[rowIndex - 2]
        val filterType = activeFilters[columnIndex].filterType

        return when (event.filterResults[filterType]) {
            SmartAlarmFilterMatch.Unknown -> MaterialTheme.colorScheme.surfaceVariant
            SmartAlarmFilterMatch.Matches -> Color.Green
            SmartAlarmFilterMatch.Fails -> Color.Red
            null -> MaterialTheme.colorScheme.error
        }
    }

    @Composable
    fun DecideTextColor(columnIndex: Int, rowIndex: Int): Color {
        if (rowIndex == 0 || rowIndex == 1) {
            return MaterialTheme.colorScheme.onSurface
        }
        val event = alarm.parsedEvents[rowIndex - 2]
        val filterType = activeFilters[columnIndex].filterType

        return when (event.filterResults[filterType]) {
            SmartAlarmFilterMatch.Unknown -> MaterialTheme.colorScheme.onSurface
            SmartAlarmFilterMatch.Matches -> Color.Black
            SmartAlarmFilterMatch.Fails -> Color.White
            null -> MaterialTheme.colorScheme.onSurface
        }
    }

    fun decideText(columnIndex: Int, rowIndex: Int): String {

        val filter = activeFilters[columnIndex]

        return when (rowIndex) {
            0 -> {
                filter.filterType.name
            }
            1 -> {
                filter.filter
            }
            else -> {
                val event = alarm.parsedEvents[rowIndex - 2]
                event.event.eventData[filter.filterType] ?: "NULL"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Row {
                Text(alarm.name + " filter matches")
            }
            Row {

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
                                        DecideBackgroundColor(columnIndex, rowIndex),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(8.dp, MaterialTheme.colorScheme.surface, RectangleShape)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = decideText(columnIndex, rowIndex),
                                    color = DecideTextColor(columnIndex, rowIndex)
                                )
                            }

                        })

                }
            }
        }

    }
}