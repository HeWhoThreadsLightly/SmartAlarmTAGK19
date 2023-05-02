package com.example.smartalarm.graphics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.example.smartalarm.model.SmartAlarmAction

@Composable
fun renderAction(event : SmartAlarmAction){
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.secondary, RectangleShape)
    ) {
        Text(event.javaClass.name)
        Button(
            modifier = Modifier,
            onClick = {
                event.begin()
            }) {
            Text("Test")
        }
    }
}