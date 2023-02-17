package com.grahampoor.ps.rules

import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import javax.sql.DataSource

class ProcessDataKtTest {

    val drivers= arrayListOf<String>(

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
    val shipments= arrayListOf<String>(
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
            drivers= drivers.subList(0,10).toTypedArray(),
            shipments = shipments.subList(0,10).toTypedArray())
        assertEquals( drivers.size, optimalRoutes.size)
    }


    @Test
    fun getVowelsSet() {
    }
    fun permuteRecur(
        input: List<Int>,
        used: List<Boolean>,
        output: MutableList<Int>
    ) {
        if (output.size == input.size) {
            println(output.joinToString(""))
            return
        }
        for (i in input.indices) {
            if (!used[i]) {
                output.add(input[i])
                val newUsed = used.toMutableList()
                newUsed[i] = true
                permuteRecur(input, newUsed, output)
                output.removeLast()
            }
        }
    }
    fun generatePermutations(n: Int): List<String> {
        val digits = (0..n).toList()
        val permutations = mutableListOf<String>()
        val stack = mutableListOf(mutableListOf<Int>())
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (current.size == n) {
                permutations.add(current.joinToString(""))
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

    @Test
    fun permuteRecurTest() {
        val input = (0..8).toList()
        val used = List(input.size) { false }
        val output = mutableListOf<Int>()
        permuteRecur(input, used, output)
    }
    @Test
    fun permuteIterTest() {
        generatePermutations(8)
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