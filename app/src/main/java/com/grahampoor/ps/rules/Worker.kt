package com.grahampoor.ps.rules

import androidx.lifecycle.MutableLiveData
import com.grahampoor.ps.repository.DriversShipments
import com.grahampoor.ps.repository.readResourceFile

class Worker {

    private val driversShipments: DriversShipments = readResourceFile()
    val drivers = MutableLiveData<List<String>>(driversShipments.drivers)
    val optimalRoutes = maxSsDriverDestinationSet(
        driversShipments.drivers.toTypedArray(),
        driversShipments.shipments.toTypedArray()
    )

    // TODO Don't think this should hold a users state from a GUI selection.
    val selectedRoute = MutableLiveData<String>("No Route No Driver Selected")

    private val permutationData = MutableLiveData<List<Int>>()

    fun generatePermutations(n: Int): List<Array<Int>> {
        val digits = (0..n).toList()
        val permutations = mutableListOf<Array<Int>>()
        val stack = mutableListOf(mutableListOf<Int>())
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (current.size == n) {
                permutations.add(current.toTypedArray())
            } else {
                for (digit in digits) {
                    if (!current.contains(digit)) {
                        val newCurrent = current.toMutableList()
                        newCurrent.add(digit)
                        stack.add(newCurrent)
                    }
                }
            }
        }
        return permutations
    }

}