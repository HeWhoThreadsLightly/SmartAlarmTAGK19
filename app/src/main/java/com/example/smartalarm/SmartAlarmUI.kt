package com.example.smartalarm
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.TextFieldValue

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
fun renderAlarm(){
    var alarm = SmartAlarmAlarm(5, "Example")
    var textName by remember { mutableStateOf(TextFieldValue(alarm.name)) }
    var textStart by remember { mutableStateOf(TextFieldValue("90")) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
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
            Text(text = "Set volume to 10%")
            Text(text = "Start playlist mk12321%")
            Text(text = "Raise volume to 80% over 15 min")
        }
    }

}
