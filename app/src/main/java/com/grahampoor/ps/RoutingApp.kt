package com.grahampoor.ps

import android.app.Application
import com.grahampoor.ps.repository.ProcessedRoutes

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