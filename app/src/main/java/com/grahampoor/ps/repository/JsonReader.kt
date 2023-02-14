package com.grahampoor.ps.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.grahampoor.ps.R
import java.sql.Driver
import com.grahampoor.ps.RoutingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


data class ExampleJson2KtKotlin (

    @SerializedName("shipments" ) var shipments : ArrayList<String> = arrayListOf(),
    @SerializedName("drivers"   ) var drivers   : ArrayList<String> = arrayListOf()

)
data class shipments(val address: List<String>)
data class drivers(val name: List<String>)
data class DriversShipments(val shipments: shipments,val drivers: drivers)
class JsonReader {
    constructor()

    /* Breaking camel case for ease of JSON object dumping and reading*/
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var readObjectAsString=""
    init {
        scope.launch {
            val inputStream = RoutingApp.instance.resources.openRawResource(R.raw.trucks_drivers)
            val json = inputStream.bufferedReader().use { it.readText() }

            val gson = Gson()
            val readObject = gson.fromJson(json, ExampleJson2KtKotlin::class.java)
            readObjectAsString = readObject.toString()
            Log.d("", readObjectAsString)
      }
    }


}