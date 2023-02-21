package com.grahampoor.ps

import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grahampoor.ps.repository.ProcessedRoutes
import com.grahampoor.ps.rules.ProcessProgressData
import com.grahampoor.ps.rules.maxSsDriverDestinationSet
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyActivityTest {
    private lateinit var scenario: ActivityScenario<MainActivity>
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
    val driversTestNoIdeal = arrayListOf<String>(
        "aaa",
        "bbb",
        "bbaa"
    )
    val shipmentsTestNoIdeal = arrayListOf<String>(
        "123 odd odd",
        "123 even1 even",
        "123 odd odd"
    )
    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testLiveDataObserverForever() {
        val liveData = MutableLiveData<String>()
        var observedValue: String? = null

        scenario.onActivity { activity ->
            liveData.observeForever {
                observedValue = it
            }
            liveData.value = "test value"
        }

       assertThat(observedValue,equalTo("test value"))
    }
    @Test
    fun maxSsDriverDestinationSetTest() {
        val processRoutes = ProcessedRoutes()
        var progressData: ProcessProgressData? = null
        scenario.onActivity { activity ->
            processRoutes.processStatus.observeForever {
                progressData = it
            }
        }
        val size = 5
        val optimalRoutes = maxSsDriverDestinationSet(
            drivers = drivers.subList(0, size).toTypedArray(),
            shipments = shipments.subList(0, size).toTypedArray(),
            processRoutes.processStatus
        )

        Assert.assertEquals(
            size * size,
            optimalRoutes.driverRouteToScoreLookUp.size
        )
        Assert.assertTrue("Complete not set",
            progressData!!.completed)
        Assert.assertEquals("Combinations don't match iterations",
            factorial(size),progressData!!.combinationCount)
        Assert.assertEquals("Max SS doesn't match",
            58.0f,progressData!!.ssMax)

        //  }
    }
}
