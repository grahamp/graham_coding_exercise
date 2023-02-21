package com.grahampoor.ps.rules

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import java.math.BigInteger


/*

The top-secret algorithm is:
● If the length of the shipment's destination street name is even, the base suitability score (SS)
is the number of vowels in the driver’s name multiplied by 1.5.
● If the length of the shipment's destination street name is odd, the base SS is the number of
consonants in the driver’s name multiplied by 1.
● If the length of the shipment's destination street name shares any common factors (besides 1)
with the length of the driver’s name, the SS is increased by 50% above the base SS.

Write an Android application using the attached json file as input that displays a list of drivers.
When one is selected from the list display the correct shipment destination to that driver in a way
that maximizes the total SS over the set of drivers. Each driver can only have one shipment and each
shipment can only be offered to one driver.

IMPLICIT Rules
* All data is received before processing. A result is only valid for a given set of drivers and destinations.
1) Display all drivers
2) Select a driver
3) Calculate SS for selected driver:
    1) Driver number of vowels, drivers consonants,  factors
    2) Shipments length, even, odd, factors
    3)  if streetName.length == even  ss = driver.vowelNum*1.5 else ss= driver.constNum * 1
    4)  if shareCommonFactor() ss = ss * 1.5 Note this never out weighs putting even streets with
    max vowel names, so brittle optimization possible.
4) Assign all the drivers to streets such that the sum of SS is maximized.

Brute force for each driver, calculate SS for each destination value.

BUT the drivers with the most vowels are guaranteed to produce the greater or equal values to any
other driver when matched

 */
/*
Creating a structure with the terms of the algorithm is motivated by a desire for readability.
The motivation for readability in the context is to support the mental mapping of the set of rules
to code for the author, Graham Poor, and code reviewer.

 */
/*

 */
/* Only support roman ascii characters in names.  */
/*
Given that I don't understand the reasoning for these rules.
The decision is taken that 'y' is not a vowel, would communicate this to the person responsible for rules.
With the level of effort required to call 'y' a vowel in "Sly" and a consonant in "Yuri".
This also will fail with non-Roman alphabets, so to Product owner about the issues with
this rule and names in other languages, etc.
 */


/* All sets of chars to test against are REQUIRED to be lowercase */
val vowelsSet = setOf('a', 'e', 'i', 'o', 'u')

/* Explicit check because (consonants == name.length - vowelCount) is too error prone given all the non letters.*/
val consonantsSet = setOf(
    'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q',
    'r', 's', 't', 'v', 'x', 'y', 'z'
)

/**
 * Max ss driver destination values
 * maxSSDriverRouteTable contains our result.
 * Other values are for Unit testing sanity, to verify we tested all
 * unique drivers to route sets.
 * Could do some clever DI to remove from product
 *
 * @property maxSSDriverRouteTable
 * @property driverRouteToScoreLookUp
 * @property iterationCount
 * @property maxSS
 * @constructor Create empty Max ss driver destination values
 */
data class MaxSsDriverDestinationValues(
    val maxSSDriverRouteTable: MutableMap<String, String>,
    val driverRouteToScoreLookUp: MutableMap<String, Float>,
    val iterationCount: Long,
    val maxSS: Float
)

/*
Brute force answer,
This has a big O of (n!)  which in general is not acceptable,
but could be okay if data sets are under 10 should be okayish and could be mitigated by
caching result.
We should also offload this computation to the server to do once for all clients apps
"Each day we get the list of shipment destinations".
That is if the set of drivers doesn't change more often, which we don't know... need to find out.
But at P(n) the server doesn't save us as n grows to into the 100s,

NOTE: The rules seem to enable some significant optimizations. For example I think all even
streets can be safely matched with the drivers with the greatest number of vowels, but only given
the multipliers provided 1.5, 1.0, 1.5.

But before it is worth spending any time on *brittle* optimizations like that some question need to be
answered by asking the product requirement owners and testing and analysing:

First: Is there a problem that optimization solves? On the datasets we anticipate does analysis or
stress testing show an issue? Are there latency issues for users? Costs for network, server
time/storage, wear on the device flash (which does wear out if you read and write all the time)?
Are there battery consumption issues, or overheating?
If not what are we optimizing? Are we scared of wearing out the CPU? :)

If there is a need to optimize:
1) Ask product owners and understand  what is the real world problem we are trying
to solve? Is there another way to do it with rules that are less combinatorially expensive?
Or allow other optimizations, For example if routes and driver sets are stable we could calculate and
store the result once for all clients on a server.
2) Are the rules stable? Or do the rules and parameters change?  If so, how often, and is it worth
the effort required?
3) Are there generic optimizations that work? Trade off larger big (O) space for a lower big (0)
computation
 speed, Memoization, other? Chat GPT suggested a greedy algorithm I assume putting the n2
 driver->route combinations in a max heap. If the problem was to take the best n driver->routes
 that would be the solution, but because we need to select n distinct drivers and n distinct routes
 it is more complex: n heaps and pull from each until you get a valid set? That doesn't seem better
 in the worst case. And the solution I have only generates and considers valid sets, which might
 be an advantage.
4) Do we need the optimal solution?  What if we can guarantee the solution is 99.5% optimal.
This is how the Traveling Salesman problem is "solved" in practice.
Going this direction seems to have provided a solution given the rules and the data set.
If we find what the "ideal" SS is and compare our result we can stop checking permutations
if our calculated SS == ideal SS. IdealSS == the sum of all drivers best ss route, even if
that may be over a list of destinations with duplicates.

*/


/**
 * Max ss driver destination set
 *
 * @param drivers
 * @param shipments
 * @param processStatus
 * @return
 */
@Suppress("KDocUnresolvedReference")
fun maxSsDriverDestinationSet(
    drivers: Array<String>,
    shipments: Array<String>,
    processStatus: MutableLiveData<ProcessProgressData> = MutableLiveData()
): MaxSsDriverDestinationValues {

    var combinationCount: Long = 0// Sanity check. Did we do all combinations?
    var ssMax: Float
    var ssCurrent: Float
    var ssSum = 0f
    val ssIdeal: Float
    var maxSSDriverRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()
    val candidateRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()

    val n = shipments.size
    val digits = (0 until n).toList()
    val stack = mutableListOf(mutableListOf<Int>())
    val driverToSSTableIdealSS = findIdealSSAndDriverToSSTable(drivers, shipments, processStatus)
    val driverRouteToScoreLookUp = driverToSSTableIdealSS.driverRouteToScoreLookUp
    ssIdeal = driverToSSTableIdealSS.ssIdeal
    ssMax = driverToSSTableIdealSS.maxSS

    // Generate all permutation of route sets length n using indexes 0 through n-1.
    // Iterate through each set of permutations.
    // For each of the unique sets of size n of indexes on the routes.
    // Iterate in n
    // Keep a driver list with the same ordered 1 - n for all n! permutation of shipment indexes.
    // Use the permuted shipping indexes to produce a unique permutation of shipping addresses.
    // This gets us all sets of combinations n drivers to n shipments.
    // This only generates valid sets of candidate driver routes each distinct driver to a distinct route.
    while (stack.isNotEmpty()) {
        val current = stack.removeLast()
        if (current.size != n) {
            for (digit in digits) {
                if (!current.contains(digit)) {
                    val newCurrent = current.toMutableList()
                    newCurrent.add(digit)
                    stack.add(newCurrent)
                }
            }
        } else {
            val shipmentPermutedIndex = current.toTypedArray()
            if (ssSum > ssMax) {
                ssMax = ssSum
                maxSSDriverRouteTable = candidateRouteTable.toMutableMap()
            }
            ssSum = 0f
            candidateRouteTable.clear()
            for (i in drivers.indices) {
                val shipmentIndex: Int =
                    shipmentPermutedIndex[i]
                val driver = drivers[i]
                val shipment = shipments[shipmentIndex]
                val key = "$driver -> $shipment"
                candidateRouteTable[driver] = shipment
                ssCurrent = driverRouteToScoreLookUp[key]!!
                driverRouteToScoreLookUp[key] = ssCurrent
                ssSum += ssCurrent
            }
            combinationCount += 1
            if (0L == combinationCount % 10000L)
                processStatus.postValue(
                    ProcessProgressData(
                        n,
                        combinationCount,
                        ssSum,
                        ssMax,
                        ssIdeal
                    )
                )
        } // if new permutation available
        if (ssMax == ssIdeal)
            break  // We found a set that is equal to the maximum possible search no more
    }// While permuting
    processStatus.postValue(
        ProcessProgressData(
            n, combinationCount, ssSum,
            ssMax, ssIdeal = ssIdeal, completed = true
        )
    )
    return MaxSsDriverDestinationValues(
        maxSSDriverRouteTable,
        driverRouteToScoreLookUp,
        combinationCount,
        ssMax
    )
}

data class DriverToSSTableIdealSS(
    val driverRouteToScoreLookUp: MutableMap<String, Float>,
    val maxSS: Float,
    val ssIdeal: Float
)

fun findIdealSSAndDriverToSSTable(
    drivers: Array<String>,
    shipments: Array<String>,
    processStatus: MutableLiveData<ProcessProgressData> = MutableLiveData()
): DriverToSSTableIdealSS {

    val driverRouteToScoreLookUp: MutableMap<String, Float> =
        HashMap<String, Float>().toMutableMap()
    var combinationCount = 0L// Sanity check. Did we do all combinations?
    val maxSSForEachDriver: MutableMap<String, Float> =
        HashMap<String, Float>().toMutableMap()
    val candidateRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()
    val r = ResultsForRouteVar()
    val n = shipments.size
    for (shippingIndexOffset in shipments.indices) {
        if (r.ssSum > r.ssMax) {
            r.ssMax = r.ssSum
        }
        r.ssSum = 0f
        candidateRouteTable.clear()
        for (i in drivers.indices) {
            val shipmentIndex: Int = (i + shippingIndexOffset) % drivers.size
            val driver = drivers[i]
            val shipment = shipments[shipmentIndex]
            computeAndStoreCalculationForRoute(
                r,
                driver,
                shipment,
                driverRouteToScoreLookUp,
                maxSSForEachDriver,
                candidateRouteTable
            )
            combinationCount += 1
            if (0L == combinationCount % 10000L)
                processStatus.postValue(
                    ProcessProgressData(
                        n,
                        combinationCount,
                        r.ssSum,
                        r.ssMax,
                        r.ssIdeal
                    )
                )

        }// For each driver with an offset shipping index

    } // For each shipping address, generate an offset modulo lastIndex of {driverIndex+0, driverIndex+1, driverIndex+2...driverIndex+(n-1)}
    if (driverRouteToScoreLookUp.size == drivers.size * shipments.size) {
        r.ssIdeal = maxSSForEachDriver.values.sum()
    }
    processStatus.postValue(
        ProcessProgressData(
            n, combinationCount, r.ssSum,
            r.ssMax, r.ssIdeal, completed = true
        )
    )
    return DriverToSSTableIdealSS(
        driverRouteToScoreLookUp,
        r.ssMax,
        r.ssIdeal
    )
}


data class ResultsForRouteVar(
    var ssMax: Float = 0f,
    var ssCurrent: Float = 0f,
    var ssSum: Float = 0f,
    var ssIdeal: Float = Float.MAX_VALUE
)

/**
 * Compute and store calculation of SS for a route
 *
 * @param r
 * @param driver
 * @param shipment
 * @param driverRouteToScoreLookUp
 * @param maxSSForEachDriver
 * @param candidateRouteTable
 */
@VisibleForTesting
internal fun computeAndStoreCalculationForRoute(
    r: ResultsForRouteVar,
    driver : String,
    shipment : String,
    driverRouteToScoreLookUp: MutableMap<String, Float> =
        HashMap<String, Float>().toMutableMap(),
    maxSSForEachDriver: MutableMap<String, Float> =
        HashMap<String, Float>().toMutableMap(),
    candidateRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()
) {
        val key = "$driver -> $shipment"
        candidateRouteTable[driver] = shipment
        r.ssCurrent = if (driverRouteToScoreLookUp.containsKey(key)) {
            driverRouteToScoreLookUp[key]!!
        } else {
            calcDriverDestinationSS(driver, shipment)
        }
        driverRouteToScoreLookUp[key] = r.ssCurrent
        r.ssSum += r.ssCurrent
        val ssMaxForDriver = maxSSForEachDriver[driver]
        if (null == ssMaxForDriver)
            maxSSForEachDriver[driver] = r.ssCurrent
        else {
            if (r.ssCurrent > ssMaxForDriver)
                maxSSForEachDriver[driver] = r.ssCurrent
        }
}

/**
 * Process progress data
 *
 * @property size
 * @property combinationCount
 * @property ssValue
 * @property ssMax
 * @property completed
 * @constructor Create empty Process progress data
 */
data class ProcessProgressData(
    val size: Int,
    val combinationCount: Long,
    val ssValue: Float,
    val ssMax: Float,
    val ssIdeal: Float = Float.MAX_VALUE,
    val completed: Boolean = false
) {
    override fun toString(): String {
        val p = factorial(size).toDouble()
        return "Total drivers->routes sets\n" +
                "${"%.0f".format(p)} of $size \n" +
                "Cur= $ssValue\n" +
                "SSMax= $ssMax\n" +
                "${percent(ssMax.toDouble(), ssIdeal.toDouble())} % of Ideal \n" +
                "${percent(combinationCount.toDouble(), p)} % complete \n"
    }

    private fun percent(a: Double, b: Double) = "${"%.0f".format((a / b) * 100).toInt()}"
}

/**
 * Driver processed
 *
 * @constructor
 *
 * @param driverIn
 */
class DriverProcessed(driverIn: String) {
    private val driver = driverIn
    val vowels: Int = countOccurrences(driver, vowelsSet)
    val consonant: Int = countOccurrences(driver, consonantsSet)
    val factors2: Set<Int> = findFactorsGreaterThanOne(driver.length)
}

/**
 * Address processed
 *
 * @constructor
 *
 * @param streetNameIn
 *//*
L
*/
class AddressProcessed(streetNameIn: String) {
    @VisibleForTesting
    internal val streetName = streetNameIn
    val evenStreetName: Boolean = (0 == streetName.length % 2) // 0 is even in computer sci
    val factors2: Set<Int> = findFactorsGreaterThanOne(streetName.length)
}

/**
 * Calc driver destination s s
 *
 * @param driverString
 * @param addressString
 * @param vowelFactor
 * @param consonantFactor
 * @param commonFactorsFactor
 * @return
 */
fun calcDriverDestinationSS(
    driverString: String,
    addressString: String,
    vowelFactor: Float = 1.5f,
    consonantFactor: Float = 1.0f,
    commonFactorsFactor: Float = 0.5f
): Float {
    val driver = DriverProcessed(driverString)
    val streetNameResult = parseStreetNameFromAddress(addressString)
    val streetName = if (streetNameResult.isSuccess)
        streetNameResult.getOrThrow()
    else
        throw streetNameResult.exceptionOrNull()!!
    val address = AddressProcessed(streetName)
    var ss: Float = if (address.evenStreetName) {
        driver.vowels * vowelFactor
    } else {
        driver.consonant * consonantFactor
    }
    if (driver.factors2.intersect(address.factors2).isNotEmpty())
        ss += ss * commonFactorsFactor
    return ss
}

/**
 * Parse street name from address
 *
 * @param address
 * @return
 *
 * Parsing  street names out in a simplistic and brittle way, not acceptable for production .
 * Parsing arbitrary addresses
 * reliably is beyond the scope of level of effort suggested for this exercise. Luckily the data
 * set presented *seems* support simply taking the first
 * two words of in each address. And thus avoiding the potholes of "Stravenue",*/
fun parseStreetNameFromAddress(address: String): Result<String> {
    return try {
        val addressElements = address.split(" ")
        /*
         Destination addresses in the given set follow the format that allows
         BNF parsing generalization:
         shipment =: <StreetNumber>" "<StreetNamePart1>" "<StreetNamePart2>" "<Other>
         For example
         "63187 Volkman Garden Suite 447",
     */
        val streetName = "${addressElements[1]} ${addressElements[2]}"
        Result.success(streetName)

    } catch (e: Exception) {
        // General catch but just passing whatever didn't allow a valid result, by my assumed rules.
        Result.failure(
            Exception(
                "Failed to parse street name from $address because ${e.message}",
                e
            )
        )
    }
}
/**
 * Find factors greater than one
 *
 * @param num
 * @return
 */
fun findFactorsGreaterThanOne(num: Int): Set<Int> {
    val factors = mutableSetOf<Int>()
    for (i in 2 until num) {
        if (num % i == 0) {
            factors.add(i)
        }
    }
    return factors
}

/**
 * Count occurrences
 *
 * @param str
 * @param target
 * @return
 */
fun countOccurrences(str: String, target: Set<Char>): Int {
    var count = 0
    for (char in str) {
        if (char in target) {
            count++
        }
    }
    return count
}

/**
 * Factorial
 *
 * @param n
 * @return
 */
fun factorial(n: Int): BigInteger {
    var result = BigInteger.ONE
    for (i in 2..n) {
        result *= i.toBigInteger()
    }
    return result
}



