package com.example.smartalarm.graphics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.smartalarm.model.SmartAlarmFilter

@Composable
fun renderFilter(filter: SmartAlarmFilter) {
    var textRegex by remember { mutableStateOf(TextFieldValue(filter.filter)) }
    Text(
        text = filter.filterType.name,
        modifier = Modifier
            .padding(start = 10.dp, top = 0.dp, bottom = 0.dp) // Add padding to the left side
    )

    if (filter.active) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .border(1.dp, Color.Black)
                .fillMaxWidth()
        ) {
            TextField(
                value = textRegex,
                onValueChange = {
                    textRegex = it
                    filter.filter = it.text
                },
                modifier = Modifier.fillMaxWidth() // Add this modifier
            )
        }
    }
}












