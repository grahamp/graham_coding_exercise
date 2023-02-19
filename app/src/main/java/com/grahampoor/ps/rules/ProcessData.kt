package com.grahampoor.ps.rules

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableSharedFlow


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
    val iterationCount: Int,
    val maxSS: Float
)

/*
Brute force answer,
This has a big (O) of n^2 which in general is not acceptable,
but could be okay if data sets are small, under 10 should be okayish and could be mitigated by
caching result.

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
*/
data class ProcessProgressData(
    val size: Int,
    val combinationCount: Int,
    val maxSS: Float
)
/**
 * Max ss driver destination set
 *
 * We get all the combinations by matching driver at index i with shipment
 * shippingIndex[i]   We progress through the combination in a way incremental
 * way that creates a full valid candidate routing table at each iteration of the outer loop.
 * This lets us test whether this is the Max SS route that we copy and save or not, in
 * which case we do not have to store it
 * An optimization this utilized is to store the calculated results for each of the n^2
 * driver -> route calculation a use of "Memoization". We could also store the final result,
 * maybe even storing it and tracking a change in original data set.
 * But before making storage speed tradeoffs, or doing any work on optimization we have
 * first identify the problem and confirm many questions I outlined in other comments.


 * @param drivers
 * @param shipments
 * @return
 */
fun maxSsDriverDestinationSet(
    drivers: Array<String>,
    shipments: Array<String>,
    processStatus: MutableLiveData<ProcessProgressData> = MutableLiveData()
): MaxSsDriverDestinationValues {

    val driverRouteToScoreLookUp: MutableMap<String, Float> =
        HashMap<String, Float>().toMutableMap()
    var combinationCount = 0// Sanity check. Did we do all combinations?
    var maxSS = 0f
    var currentSS: Float
    var ssSum = 0f
    var maxSSDriverRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()
    val candidateRouteTable: MutableMap<String, String> =
        HashMap<String, String>().toMutableMap()

    val n = shipments.size
    val digits = (0 until n).toList()
    val stack = mutableListOf(mutableListOf<Int>())

    // Generate all permutation of indexes this should get us all sets of n drivers to n shipments
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
            if (ssSum > maxSS) {
                maxSS = ssSum
                maxSSDriverRouteTable = candidateRouteTable.toMutableMap()
            }
            ssSum = 0f
            candidateRouteTable.clear()
            for (i in drivers.indices) {
                val shipmentIndex: Int =
                    shipmentPermutedIndex[i] //(i + shippingIndexOffset) % drivers.size
                val driver = drivers[i]
                val shipment = shipments[shipmentIndex]
                val key = "$driver -> $shipment"
                candidateRouteTable[driver] = shipment
                currentSS = if (!driverRouteToScoreLookUp.containsKey(key)) {
                    calcDriverDestinationSS(driver, shipment)
                } else {
                    driverRouteToScoreLookUp[key]!!
                }
                driverRouteToScoreLookUp[key] = currentSS
                ssSum += currentSS
            }
            combinationCount+=1
            processStatus.postValue(ProcessProgressData(n, combinationCount , ssSum))
//        Log.d(
//            "ProcessData",
//            " $shippingIndexOffset) shipment ${shipments[shippingIndexOffset]} end \n start Score $ssSum "
//        )
        } // if new permutation available
    }// While permuting
    return MaxSsDriverDestinationValues(
        maxSSDriverRouteTable,
        driverRouteToScoreLookUp,
        combinationCount,
        maxSS
    )
}


/**
 * Parse street name from address
 *
 * @param address
 * @return
 *//* Parsing  street names out in a simplistic and brittle way, not acceptable for production .
Parsing arbitrary addresses reliably is beyond the scope of level of effort
suggested for this exercise. Luckily the data set presented *seems* support simply taking the first
two words of in each address. And thus avoiding the potholes of "Stravenue",*/
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
 *  Long explicit variable names throughout because the algorithm is so quirky and specific.
 *  Big O(sqrt(n)) where n is the letters in the street name
 * @constructor
 *
 * @param streetNameIn
 *//*
L
*/
class AddressProcessed(streetNameIn: String) {
    private val streetName = streetNameIn
    val evenStreetName: Boolean = (0 == streetName.length / 2) // 0 is even in computer sci
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
    commonFactorsFactor: Float = 1.5f
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



