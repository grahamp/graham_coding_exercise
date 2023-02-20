package com.grahampoor.ps.repository

import androidx.lifecycle.MutableLiveData
import kotlin.Result

/**
 * State
 *
 * @constructor Create empty State
 *//*
Communicate state to UI while loading or transitioning to a new data set.
State.DataAvailable - Means there should be a valid table with drivers and routes available, that
replaces any previous data.
State.Initialized is posted when the class is initialized.
State.Loading is posted when raw data is read
State.Processing is posted when routes are being calculated
State.Updating is posted to tell the UI throw out the existing data because a change has happened in
drivers or routes, invalidating existing data. The UI should expect the Loading and Processing states
and then DataAvailable.
Error means we could not provide valid results.
Program exceptions will propagate to the UI through Result.failure
 */
enum class State {
    /**
     * Initialized
     *
     * @constructor Create empty Initialized
     */
    Initialized,

    /**
     * Loading
     *
     * @constructor Create empty Loading
     */
    Loading,

    /**
     * Processing
     *
     * @constructor Create empty Processing
     */
    Processing,

    /**
     * Data available
     *
     * @constructor Create empty Data available
     */
    DataAvailable,

    /**
     * Updating
     *
     * @constructor Create empty Updating
     */
    Updating,

    /**
     * Error
     *
     * @constructor Create empty Error
     */
    Error
}

/**
 * Processed data
 *
 * @property maxSSDriverRouteTable
 * @property stateInfo
 * @constructor Create empty Processed data
 */
data class ProcessedData(
    val maxSSDriverRouteTable: MutableMap<String, String>,
    val stateInfo : State = State.Initialized
)

/**
 * I processed routes
 *
 * @constructor Create empty I processed routes
 */
interface IProcessedRoutes {
    /**
     * Get data
     *
     * @return
     */
    fun getData(): MutableLiveData<Result<ProcessedData>>
}