package com.grahampoor.ps.rules

import androidx.lifecycle.MutableLiveData
import com.grahampoor.ps.repository.DriversShipments
import com.grahampoor.ps.repository.readResourceFile

class Worker {

    private val driversShipments : DriversShipments = readResourceFile()
    val drivers = MutableLiveData<List<String>>(driversShipments.drivers)
    val optimalRoutes = maxSsDriverDestinationSet(driversShipments.drivers.toTypedArray(),
        driversShipments.shipments.toTypedArray())
    // TODO Don't think this should hold a users state from a GUI selection.
    val selectedRoute = MutableLiveData<String> ("No Route No Driver Selected")

}