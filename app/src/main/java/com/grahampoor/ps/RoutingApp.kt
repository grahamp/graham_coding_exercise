package com.grahampoor.ps

import android.app.Application

public class RoutingApp : Application() {

    companion object {
         lateinit var instance: RoutingApp
    }
    init {
        instance = this
    }

}