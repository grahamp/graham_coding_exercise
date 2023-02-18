package com.grahampoor.ps.veiwmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grahampoor.ps.repository.IProcessedRoutes
import com.grahampoor.ps.repository.ProcessedData
import com.grahampoor.ps.repository.State

class DriverRouteViewModel(resultIn: Result<ProcessedData>) : ViewModel() {
    val result = resultIn
    private val NoRouteAvalableMessage = "No Route Available "
    var drivers = ArrayList<String>()

    private val _selectedDriver = MutableLiveData<String>()
    val selectedDriver : LiveData<String> = _selectedDriver
    fun setDriver(userSelectedDriver : String) {
        // Fetch user data from the repository and update the LiveData
        _selectedDriver.postValue(userSelectedDriver)
    }
    var currentRoute = NoRouteAvalableMessage
    fun updateRoute() {
        if (result.isSuccess)
            when (result.getOrThrow().stateInfo) {
                State.Initialized -> {
                    currentRoute = "$NoRouteAvalableMessage : ${State.Initialized.name} "
                }
                State.Loading -> {
                    currentRoute = "$NoRouteAvalableMessage : ${State.Loading.name} "
                }
                State.Processing -> {
                    currentRoute = "$NoRouteAvalableMessage : ${State.Processing.name} "
                }
                State.DataAvailable -> {
                    currentRoute = "Routes Available: Please select a Driver"
                    drivers =
                        result.getOrThrow().maxSSDriverRouteTable.keys.toMutableList() as ArrayList<String>
                }
                State.Updating -> {
                    currentRoute = "$NoRouteAvalableMessage : ${State.Updating.name} "
                }
                State.Error -> {
                    currentRoute = "$NoRouteAvalableMessage : ${State.Error.name} "
                }
            }
    }

    init {
        updateRoute()
        selectedDriver.observeForever { selectedDriver ->
            currentRoute = result.getOrThrow().maxSSDriverRouteTable[selectedDriver]!!
        }
    }
}