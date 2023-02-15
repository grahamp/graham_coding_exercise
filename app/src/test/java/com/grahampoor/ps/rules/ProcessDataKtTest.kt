package com.grahampoor.ps.rules

import org.junit.Assert.*

import org.junit.Test

class ProcessDataKtTest {

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