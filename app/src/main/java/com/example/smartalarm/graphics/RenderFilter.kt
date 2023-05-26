package com.example.smartalarm.graphics

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.smartalarm.model.SmartAlarmFilter

@Composable
fun renderFilter(filter: SmartAlarmFilter) {
    var textRegex by remember { mutableStateOf(TextFieldValue(filter.filter)) }
    var isActive by remember { mutableStateOf(filter.active) }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(1.dp, Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = filter.filterType.name,
                modifier = Modifier.padding(start = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive) {
                    TextField(
                        value = textRegex,
                        onValueChange = {
                            textRegex = it
                            filter.filter = it.text
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = it
                        filter.active = it
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

