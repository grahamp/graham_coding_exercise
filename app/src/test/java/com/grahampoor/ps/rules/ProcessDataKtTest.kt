package com.grahampoor.ps.rules

import android.util.Log
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class ProcessDataKtTest {
    val drivers = setOf<String>(

        "Noemie Murphy",
        "Cleve Durgan",
        "Murphy Mosciski",
        "Everardo Welch",
        "Orval Mayert",
        "Howard Emmerich",
        "Izaiah Lowe",
        "Monica Hermann",
        "Ellis Wisozk",
        "Kaiser Sose"
    )
    val shipments = setOf<String>(
        "215 Osinski Manors",
        "9856 Marvin Stravenue",
        "7127 Kathlyn Ferry",
        "987 Champlin Lake",
        "63187 Volkman Garden Suite 447",
        "75855 Dessie Lights",
        "1797 Adolf Island Apt. 744",
        "2431 Lindgren Corners",
        "8725 Aufderhar River Suite 859",
        "79035 Shanna Light Apt. 322"
    )
    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun maxSsDriverDestinationSetTest() {

        val optimalRoutes = maxSsDriverDestinationSet(
            drivers= drivers,
            shipments = shipments)
        assertEquals( drivers.size, optimalRoutes.size)
    }
    fun  allCombos(  drivers: Array<String>,
                     shipments: Array<String>): MutableMap<String, String> {
       /* A potential optimization... maybe not worth it because the calculation is that bad.
        But shows I know about "Memoization". We could also store each complete "route-set" with it's value. But before making
        storage speed tradoffs, or doing any work on optimization we have first identify the problem and confirm many questions I
        outlined in other comments.
        */
        val driverRouteToScoreLookUp:  MutableMap<String,Float> = HashMap<String,Float>()
        HashMap<String, Float>().toMutableMap()
        var countIterations = 0
        var countCalulation = 0
        var maxSS = 0f
        var currentSS: Float
        var ssSetSum = 0f
        var maxSSDriverRouteTable: MutableMap<String, String> =
            HashMap<String, String>().toMutableMap()
        val candidateRouteTable: MutableMap<String, String> =
            HashMap<String, String>().toMutableMap()
        for (i in drivers.indices) {
            Log.d(
                "ProcessData",
                " $i) Driver ${drivers[i]}  start")

            for (j in shipments.indices) {
                if (ssSetSum > maxSS) {
                    maxSS = ssSetSum
                    maxSSDriverRouteTable = candidateRouteTable.toMutableMap()
                }
                ssSetSum = 0f
                candidateRouteTable.clear()

                for (i in drivers.indices) {
                    countIterations += 1
                    var shipmentIndex = (i + j) % drivers.size
                    val driver = drivers[i]
                    val shipment = shipments[shipmentIndex]
                    val key = "$driver -> $shipment"
                    candidateRouteTable[driver]=shipment
                    currentSS = if (!driverRouteToScoreLookUp.containsKey(key)) {
                        calcDriverDestinationSS(driver, shipment)
                    } else {
                        driverRouteToScoreLookUp.get(key)!!
                    }
                    driverRouteToScoreLookUp[key] = currentSS
                    ssSetSum += currentSS
                }
            }
            Log.d(
                "ProcessData",
                " $i) Driver ${drivers[i]} end \n start Score $ssSetSum interations=$countIterations calculations=$countCalulation"
            )
        } //for each driver
        return maxSSDriverRouteTable
    }
    @Test
    fun combinationsSetTest2() {

        val routes = allCombos(
            drivers.toTypedArray(),
            shipments.toTypedArray())
        assertEquals( drivers.size*shipments.size, routes.size)
    }

    fun <T> allCombinations(set1: Set<T>, set2: Set<T>): Set<Set<T>> {
        if (set1.isEmpty()) {
            return setOf(emptySet())
        }
        val combinations = mutableSetOf<Set<T>>()
        for (element in set2) {
            val remaining = set2.filter { it != element }.toSet()
            for (subCombination in allCombinations(set1 - element, remaining)) {
                combinations.add(subCombination + element)
            }
        }
        return combinations
    }


    @Test
    fun main() {
        val numbers = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val letters = setOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j')
        var combinations :  MutableSet<Set<Any>> = mutableSetOf<Set<Any>>()

        combinations = allCombinations2(numbers, letters,combinations)
        for (combination in combinations) {
            println(combination)
        }
    }

    fun allCombinations2(set1: Set<Int>, set2: Set<Char>, combinations: MutableSet<Set<Any>>): MutableSet<Set<Any>> {
        if (set1.isEmpty()) {
            return combinations
        }
        for (element in set2) {
            val remaining = set2.filter { it != element }.toSet()
            for (subCombination in allCombinations2(set1, remaining, combinations)) {
                combinations.add(subCombination + element)
            }
        }
        return combinations
    }

    @Test
    fun getVowelsSet() {
    }

    @Test
    fun parseStreetNameFromAddress() {
        val streetNameResult = parseStreetNameFromAddress(  "63187 Volkman Garden Suite 447",)
        assertTrue(streetNameResult.isSuccess)
        val streetName = streetNameResult.getOrThrow()
        assertEquals("Volkman Garden",streetName)
    }
    @Test
    fun findFactorsGreaterThanOne4() {
        val factorSet = findFactorsGreaterThanOne(4)
        assertTrue("failed",!factorSet.intersect(setOf(2)).isEmpty())
    }
    @Test
    fun findFactorsGreaterThanOne12() {
        val factorSet = findFactorsGreaterThanOne(12)
        assertTrue("failed",!factorSet.intersect(setOf(2, 3, 4, 6,)).isEmpty())
    }

    @Test
    fun findFactorsGreaterThanOnePrimeExpectNoting() {
        val factorSet = findFactorsGreaterThanOne(7)
        assertTrue("Expected empty",factorSet.isEmpty())
    }
}