package com.grahampoor.ps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import com.grahampoor.ps.rules.Worker
import com.grahampoor.ps.ui.theme.Graham_PSTheme

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.grahampoor.ps.rules.ProcessDataByRules

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val processData = ProcessDataByRules()
        processData.selectedRoute.observeForever { route ->
            setContent {
                Graham_PSTheme {
                    DriverScreen(processData)
                }
            }
        }
    }
}

@Composable
fun DriverScreen(processData : ProcessDataByRules) {
    var selectedItem by remember { mutableStateOf<String?>(null) }



    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        DriverList(items = processData.drivers.value!!.toList(),
            processData.selectedRoute.value!!, onItemSelected = { selectedItem = it
                processData.selectedRoute.postValue(processData.optimalRoutes[selectedItem]) }, onButtonClicked = {
                processData.selectedRoute.postValue(processData.optimalRoutes[selectedItem])
            })
    }
}


//@Composable
//fun MyScreen() {
//    val items = listOf("Item 1", "Item 2", "Item 3")
//    var selectedItem : String? by remember { mutableStateOf<String?>() }
//
//    DriverList(items = items, onItemSelected = { selectedItem = it }, onButtonClicked = {
//        // Handle button click here
//    })
//
//    // Use the selected item here
//    if (selectedItem != null) {
//        // ...
//    }
//}


@Composable
fun DriverList(
    items: List<String>,
    route: String,
    onItemSelected: (String) -> Unit,
    onButtonClicked: () -> Unit
) {
    Column {
        // Selectable list
        Text(
            text = route,
            modifier = Modifier
        )
        LazyColumn {

            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemSelected(item) }
                        .padding(16.dp)
                )
            }
        }

        // Button
        Button(
            onClick = onButtonClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Button Text")
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    Graham_PSTheme {
//        DriverScreen("DefaultPreview dummy route")
//    }
//}