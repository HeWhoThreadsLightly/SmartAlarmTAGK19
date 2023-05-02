package com.example.smartalarm.graphics

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import com.example.smartalarm.model.SmartAlarmFilter

@Composable
fun renderFilter(filter: SmartAlarmFilter){
    var textRegex by remember { mutableStateOf(TextFieldValue(filter.filter)) }
    Text(text = filter.filterType.name)

    if(filter.active){
        TextField(
            value = textRegex,
            onValueChange = {
                textRegex = it
                filter.filter = it.text
            }
        )
    }

}

