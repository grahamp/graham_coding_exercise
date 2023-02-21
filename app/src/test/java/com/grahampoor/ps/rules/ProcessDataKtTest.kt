package com.grahampoor.ps.rules

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProcessDataKtTest {

    val shipmentsIdeal = arrayListOf<String>(
        "1 a even x",
        "2 b odd x",
        "3 c odd x",
        "4 d even x",
        "5 e even x",
        "6 a even x",
        "7 b odd x",
        "8 c odd x",
        "9 d even x",
        "0 d even x"
    )
    val driversIdeal = arrayListOf<String>(

        "1 d even x",
        "2 d odd x",
        "3 d odd x",
        "4 d even x",
        "5 d even x",
        "6 d even x",
        "7 d odd x",
        "8 d odd x",
        "9 d even x",
        "0 d even x"
    )

    val drivers = arrayListOf<String>(
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
    val shipments = arrayListOf<String>(
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
        val end = 5
        val optimalRoutes = maxSsDriverDestinationSet(
            drivers = drivers.subList(0, end).toTypedArray(),
            shipments = shipments.subList(0, end).toTypedArray(),
        )
        val size =drivers.subList(0, end).size
        val permutations : Long   = factorial(size).toLong()

        assertEquals("Lookup table size wrong",size * size, optimalRoutes.driverRouteToScoreLookUp.size)
        assertNotEquals( "ssIdeal vs ssMax wrong",optimalRoutes.ssMax, optimalRoutes.ideal )
        assertEquals("Actual iterations not predicted",optimalRoutes.iterationCount,permutations )

    }

    @Test
    fun maxSsIdealDriverDestinationSetTest() {
        val size: Int = 10
        val permutations : Long   = factorial(size).toLong()
        val optimalRoutes = maxSsDriverDestinationSet(
            drivers = driversIdeal.subList(0, size).toTypedArray(),
            shipments = shipmentsIdeal.subList(0, size).toTypedArray(),
        )

        assertEquals(
            "Lookup table size wrong",
            size * size,
            optimalRoutes.driverRouteToScoreLookUp.size
        )
        assertEquals("ssIdeal vs ssMax wrong", optimalRoutes.ssMax, optimalRoutes.ideal)
        assertEquals("Actual iterations not predicted", optimalRoutes.iterationCount, 0)
    }

        @Test
    fun computeAndStoreCalculationForRouteTest() {
        val r = ResultsForRouteVar()
        computeAndStoreCalculationForRoute(r, "aaabbbbb", "1234 even eve")
        // 3 vowels * 1.5 yes common
        assertEquals(3f * 1.5f * 1.5f, r.ssCurrent)
        computeAndStoreCalculationForRoute(r, "aaabbbbb", "1234 odd odd")
        // 5 consonants * 1 no common
        assertEquals(5f * 1f, r.ssCurrent)

    }

    @Test
    fun calcDriverDestinationSSTest() {
        var ss = calcDriverDestinationSS("aaabbbb", "123 even eve")
        // 3 vowels * 1.5 no common
        assertEquals(3f * 1.5f, ss)
        ss = calcDriverDestinationSS("aaabbbb", "123 odd odd")
        // 4 consonants * 1 yes common
        assertEquals(4f, ss)
        ss = calcDriverDestinationSS("aaa bbbb", "123 even eve")
        // 3 vowels * yes common
        assertEquals(3f * 1.5f * 1.5f, ss)


    }

    @Test
    fun addressProcessedTest() {
        var ap = AddressProcessed("even eve")
        // Even, common factor
        assertTrue("Even/Odd wrong", ap.evenStreetName)
        assertEquals("Street not parsed", "even eve", ap.streetName)
        assertEquals("Factors over 1", setOf(2, 4), ap.factors2)
        ap = AddressProcessed("odd odd")
        // Odd, no common
        assertFalse("Even/Odd wrong", ap.evenStreetName)
        assertEquals("Street not parsed", "odd odd", ap.streetName)
        assertEquals("Factors over 1", 0, ap.factors2.size)

    }

    @Test
    fun driverProcessedTest() {
        val dp = DriverProcessed("aaa bbbb")
        // Even, common factor
        assertEquals("vowel count wrong", 3, dp.vowels)
        assertEquals("const count wrong", 4, dp.consonant)
        assertEquals("Factors over 1", setOf(2, 4), dp.factors2)
    }


    @Test
    fun parseStreetNameFromAddress() {
        val streetNameResult = parseStreetNameFromAddress("63187 Volkman Garden Suite 447")
        assertTrue(streetNameResult.isSuccess)
        val streetName = streetNameResult.getOrThrow()
        assertEquals("Volkman Garden", streetName)
    }

    @Test
    fun findFactorsGreaterThanOne4() {
        val factorSet = findFactorsGreaterThanOne(4)
        assertArrayEquals("failed", arrayOf(2),factorSet.toTypedArray())
    }

    @Test
    fun findFactorsGreaterThanOne12() {
        val factorSet = findFactorsGreaterThanOne(12)
        assertArrayEquals("failed", arrayOf(2, 3, 4, 6),factorSet.toTypedArray())
    }

    @Test
    fun findFactorsGreaterThanOnePrimeExpectNoting() {
        val factorSet = findFactorsGreaterThanOne(7)
        assertTrue("Expected empty", factorSet.isEmpty())
    }
}