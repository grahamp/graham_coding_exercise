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
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
                    val driverRouteViewModel = DriverRouteViewModel(routeResult)
                    DriverScreen(driverRouteViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun DriverScreen(driverRouteViewModel: DriverRouteViewModel) {
    var selectedDriver by remember { mutableStateOf<String?>(null) }

    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(Modifier.fillMaxSize()) {
            // Button
            Button(
                onClick = {
                    openMapsToAddress(RoutingApp.instance, driverRouteViewModel.currentRoute)
                },
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            ) {
                Text("Show Map")
            }
            Text(
                text = driverRouteViewModel.currentRoute,
                modifier = Modifier.padding(16.dp),
                fontSize = TextUnit(28f, TextUnitType.Sp)
            )
            DriverList(items = driverRouteViewModel.drivers,
                onItemSelected = {
                    selectedDriver = it
                    driverRouteViewModel.currentRoute = it
                    driverRouteViewModel.selectedDriver.postValue(it)
                })


        }
    }
}


@OptIn(ExperimentalUnitApi::class)
@Composable
fun DriverList(
    items: List<String>,
    onItemSelected: (String) -> Unit,
) {

    Column(Modifier.wrapContentSize()) {
        LazyColumn(
            Modifier.weight(0.5f),
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
    val testMap = hashMapOf<String, String>()
    testMap["a"] = "1"
    testMap["b"] = "2"
    testMap["c"] = "3"
    testMap["d"] = "4"
    testMap["f"] = "5"

    val drm = DriverRouteViewModel(Result.success(ProcessedData(testMap, State.DataAvailable)))

    Graham_PSTheme {
        DriverScreen(drm)
    }
}