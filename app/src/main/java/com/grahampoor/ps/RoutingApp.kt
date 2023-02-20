package com.grahampoor.ps

import android.app.Application
import com.grahampoor.ps.repository.ProcessedRoutes

/**
 * Routing app
 *
 * @constructor Create empty Routing app
 */
class RoutingApp : Application() {
    val processedRoutes = ProcessedRoutes()

    companion object {
         lateinit var instance: RoutingApp
    }
    init {
        instance = this
        processedRoutes.run()
    }

}