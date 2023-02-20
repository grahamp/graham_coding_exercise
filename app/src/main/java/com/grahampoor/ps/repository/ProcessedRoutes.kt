package com.grahampoor.ps.repository

import androidx.lifecycle.MutableLiveData
import com.grahampoor.ps.R
import com.grahampoor.ps.rules.ProcessProgressData
import com.grahampoor.ps.rules.maxSsDriverDestinationSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Processed routes
 *
 * @constructor Create empty Processed routes
 */
class ProcessedRoutes : IProcessedRoutes { // end processDataByRules
    val processedRouteData = MutableLiveData<Result<ProcessedData>>()
    private val processedDataError = Result.success(ProcessedData(HashMap(), State.Error))
    val processStatus = MutableLiveData<ProcessProgressData>()

    /**
     * Run
     *
     */
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
                        driversShipments.shipments.toTypedArray(),
                        processStatus
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


    /**
     * Read driver shipments
     *
     * @return
     */
    private fun readDriverShipments(): Result<Any> {
        return try {
            Result.success(readResourceFile(R.raw.trucks_drivers))
        } catch (e: Exception) {
            Result.success(e)
        }
    }

    /**
     * Get data
     *
     * @return
     */
    override fun getData(): MutableLiveData<Result<ProcessedData>> {
        return processedRouteData
    }
}