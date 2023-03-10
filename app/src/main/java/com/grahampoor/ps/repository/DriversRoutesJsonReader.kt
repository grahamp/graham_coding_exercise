package com.grahampoor.ps.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.grahampoor.ps.R
import com.grahampoor.ps.RoutingApp


/**
 * Drivers shipments
 *
 * @property shipments
 * @property drivers
 * @constructor Create empty Drivers shipments
 */
data class DriversShipments(

    @SerializedName("shipments") var shipments: ArrayList<String> = arrayListOf(),
    @SerializedName("drivers") var drivers: ArrayList<String> = arrayListOf()

)

/**
 * Read resource file
 *
 * @param resourceFile
 * @return
 */
fun readResourceFile(resourceFile: Int = R.raw.trucks_drivers): DriversShipments {
       // scope.launch {
            val inputStream = RoutingApp.instance.resources.openRawResource(resourceFile)
            val json = inputStream.bufferedReader().use { it.readText() }

            val gson = Gson()
            val driversShipments: DriversShipments  = gson.fromJson(json, DriversShipments::class.java)
            val readObjectAsString = driversShipments.toString()
            Log.d("Read JSON Data DriverShipments", readObjectAsString)
       // }
       return driversShipments

    }


