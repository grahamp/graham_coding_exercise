package com.grahampoor.ps.rules

import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class ProcessDataKtTest {
    val drivers = setOf<String>(
        "Everardo Welch",
        "Orval Mayert",
        "Howard Emmerich",
        "Izaiah Lowe",
        "Monica Hermann",
        "Ellis Wisozk",
        "Noemie Murphy",
        "Cleve Durgan",
        "Murphy Mosciski",
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
            destinations = shipments)
        assertEquals( optimalRoutes.size, drivers.size)
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