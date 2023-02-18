package com.grahampoor.ps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import com.grahampoor.ps.ui.theme.Graham_PSTheme

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.grahampoor.ps.repository.ProcessedData
import com.grahampoor.ps.repository.ProcessedRoutes
import com.grahampoor.ps.repository.State
import com.grahampoor.ps.veiwmodel.DriverRouteViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            ProcessedRoutes().processedRouteData.observeForever { routeResult ->
            setContent {
                Graham_PSTheme {
                    DriverScreen(DriverRouteViewModel(routeResult))
                }
            }
        }
    }
}

@Composable
fun DriverScreen(driverRouteViewModel: DriverRouteViewModel) {
    var selectedDriver by remember { mutableStateOf<String?>(null) }


    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        DriverList(items = driverRouteViewModel.drivers,
            driverRouteViewModel.currentRoute, onItemSelected = {
                driverRouteViewModel.selectedDriver.postValue(it)
            }, onButtonClicked = {
                openMapsToAddress(RoutingApp.instance, driverRouteViewModel.currentRoute)
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
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {

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
            Text("Show Map")
        }
    }
}

fun openMapsToAddress(context: Context, address: String) {
    val intentUri = Uri.parse("geo:0,0?q=$address")
    val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        Toast.makeText(context, "Google Maps app not installed", Toast.LENGTH_SHORT).show()
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val testMap = hashMapOf<String,String>()
    testMap["a"]="1"
    testMap["b"]="2"
    testMap["c"]="3"
    testMap["d"]="4"

    val drm = DriverRouteViewModel(Result.success(ProcessedData( testMap, State.DataAvailable)))

    Graham_PSTheme {
        DriverScreen(drm)
    }
}