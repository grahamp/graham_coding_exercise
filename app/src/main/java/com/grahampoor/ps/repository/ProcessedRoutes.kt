package com.grahampoor.ps.repository

import androidx.lifecycle.MutableLiveData
import com.grahampoor.ps.R
import com.grahampoor.ps.rules.maxSsDriverDestinationSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProcessedRoutes : IProcessedRoutes { // end processDataByRules
    val processedRouteData = MutableLiveData<Result<ProcessedData>>()
    private val processedDataError = Result.success(ProcessedData(HashMap(), State.Error))
    suspend fun runTest() {
        val testMap = hashMapOf<String, String>()
        testMap["a"] = "1"
        testMap["b"] = "2"
        testMap["c"] = "3"
        testMap["d"] = "4"
        testMap["f"] = "5"
        val scope = CoroutineScope(Dispatchers.Default)

        // Launch a coroutine to run the function on a background thread
        //scope.launch {
            for (i in 0..100) {
                delay(1000) // Simulate long running process
                val state = State.values()[i % 4 ]

                processedRouteData.postValue(
                    Result.success(
                        ProcessedData(
                            testMap,
                            state
                        )
                    )
                )
            }
       // }
    }
     fun run()  {

        val scope = CoroutineScope(Dispatchers.Default)

        // Launch a coroutine to run the function on a background thread
        scope.launch {
            processedRouteData.postValue(
                Result.success(
                    ProcessedData(
                        HashMap(),
                        State.Loading
                    )
                )
            )
            val result = readDriverShipments()
            if (result.isSuccess) {
                processedRouteData.postValue(
                    Result.success(
                        ProcessedData(
                            HashMap(),
                            State.Processing
                        )
                    )
                )
                val driversShipments: DriversShipments = result.getOrThrow() as DriversShipments
                try {
                    val optimalRoutes = maxSsDriverDestinationSet(
                        driversShipments.drivers.toTypedArray(),
                        driversShipments.shipments.toTypedArray()
                    )
                    processedRouteData.postValue(
                        Result.success(
                            ProcessedData(
                                optimalRoutes.maxSSDriverRouteTable,
                                State.DataAvailable
                            )
                        )
                    )
                } catch (e: Exception) {
                    // ToDo improve error handling in stream
                    processedRouteData.postValue(processedDataError)
                }
            } else {
                processedRouteData.postValue(processedDataError)
            }
        }
    }


    private fun readDriverShipments(): Result<Any> {
        return try {
            Result.success(readResourceFile(R.raw.trucks_drivers))
        } catch (e: Exception) {
            Result.success(e)
        }
    }

    override fun getData(): MutableLiveData<Result<ProcessedData>> {
        return processedRouteData
    }
}