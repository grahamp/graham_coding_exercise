package com.grahampoor.ps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.grahampoor.ps.repository.ProcessedData
import com.grahampoor.ps.repository.State
import com.grahampoor.ps.rules.ProcessProgressData
import com.grahampoor.ps.ui.theme.Graham_PSTheme
import com.grahampoor.ps.veiwmodel.DriverRouteViewModel
import kotlinx.coroutines.launch

/**
 * Main activity
 *
 * @constructor Create empty Main activity
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ToDo add DI and inject those objects whose lifecycles are not scoped to acctivity
        val processedRoutes = RoutingApp.instance.processedRoutes
        processedRoutes.processedRouteData.observe(this) { routeResult ->
            setContent {
                Graham_PSTheme {
                    val driverRouteViewModel = DriverRouteViewModel(routeResult)
                    setContent {
                        DriverScreen(driverRouteViewModel)
                    }
                    driverRouteViewModel.selectedDriver.observe(this) {
                        setContent {
                            DriverScreen(driverRouteViewModel)
                        }
                    }
                    processedRoutes.processStatus.observe(this) {
                        setContent {
                            DriverScreen(driverRouteViewModel, it)
                        }
                    }
                }
            }

        }
        lifecycleScope.launch {
            val result = processedRoutes.processedRouteData.value
            //ToDo Better check here But only need this for a demo to show
            // updating progress of long computation. Remove in production
            if ((result?.getOrThrow()?.stateInfo != State.DataAvailable) &&
                (result?.getOrThrow()?.stateInfo != State.Processing)){
                processedRoutes.run()
            }
        }
    }

    /**
     * Driver screen
     *
     * @param driverRouteViewModel
     * @param processProgressData
     */
    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun DriverScreen(
        driverRouteViewModel: DriverRouteViewModel,
        processProgressData: ProcessProgressData? = null
    ) {
        var selectedDriver by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current // get the activity context

        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(Modifier.fillMaxSize()) {

                // Button
                if (driverRouteViewModel.drivers.size > 0) {
                    Text(
                        text = driverRouteViewModel.selectedDriver.value ?: "Select Driver",
                        modifier = Modifier.padding(16.dp),
                        fontSize = TextUnit(28f, TextUnitType.Sp)
                    )
                    Button(
                        onClick = {
                            openMapsRouteToAddress(context, driverRouteViewModel.currentRoute)
                        },
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(16.dp)
                    ) {
                        Text("Show Route")
                    }
                } else {
                    if ((processProgressData != null) && !processProgressData.completed) {
                        Text(
                            text = processProgressData.toString(),
                            modifier = Modifier.padding(16.dp),
                            fontSize = TextUnit(28f, TextUnitType.Sp)
                        )
                    }
                }

                Text(
                    text = driverRouteViewModel.currentRoute,
                    modifier = Modifier.padding(16.dp),
                    fontSize = TextUnit(28f, TextUnitType.Sp)
                )
                DriverList(items = driverRouteViewModel.drivers,
                    onItemSelected = {
                        val listItem = driverRouteViewModel.drivers[it]
                        selectedDriver = listItem
                        driverRouteViewModel.setDriver(listItem)
                    })


            }
        }
    }

    /**
     * Driver list
     *
     * @param items
     * @param onItemSelected
     * @receiver
     */
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun DriverList(items: List<String>, onItemSelected: (Int) -> Unit) {
        val selectedItemIndex = remember { mutableStateOf(-1) }

        LazyColumn {
            itemsIndexed(items) { index, item ->
                ListItem(
                    text = { Text(item) },
                    modifier = Modifier
                        .clickable {
                            selectedItemIndex.value = index
                            onItemSelected(index)
                        }
                        .background(
                            if (selectedItemIndex.value == index) Color.Gray else Color.Transparent
                        )
                )
            }
        }
    }


    /**
     * Open maps route to address
     *
     * @param context
     * @param address
     */
    private fun openMapsRouteToAddress(context: Context, address: String) {
        // Finds some street with the right address and number
        val intentUriForMap = Uri.parse("geo:0,0?q=$address")
        // This fits the spirit of the exercise but fails with these addresses with the state
        // val intentUriForRoute = Uri.parse("google.navigation:q=$address") // create the Uri with the address

        val mapIntent = Intent(Intent.ACTION_VIEW, intentUriForMap)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Google Maps app not installed", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Default preview
     *
     */
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
            drm.setDriver("b")
        }

    }
}