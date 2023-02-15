package com.grahampoor.ps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.grahampoor.ps.repository.DriversShipments
import com.grahampoor.ps.repository.readResourceFile
import com.grahampoor.ps.rules.maxSsDriverDestinationSet
import com.grahampoor.ps.ui.theme.Graham_PSTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
           val ds :DriversShipments = readResourceFile()
           val optimalRoutes = maxSsDriverDestinationSet(ds.drivers.toSet(),
                ds.shipments.toSet())
           setContent {
               Graham_PSTheme {
                   // A surface container using the 'background' color from the theme
                   Surface(
                       modifier = Modifier.fillMaxSize(),
                       color = MaterialTheme.colors.background
                   ) {
                       Greeting(optimalRoutes.toString())
                   }
               }
       }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Graham_PSTheme {
        Greeting("Android")
    }
}