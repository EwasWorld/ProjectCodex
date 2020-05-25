package eywa.projectcodex

import android.content.res.Resources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.viewModels.NewRoundViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class RoundSelectionUnitTest {
    private val lifecycleOwner = mock<LifecycleOwner>()
    private val viewModel = mock<NewRoundViewModel>()
    private lateinit var resources: Resources
    private lateinit var allRounds: LiveData<LiveData<List<Round>>>
    private lateinit var allRoundSubTypes: LiveData<List<RoundSubType>>
    private lateinit var allRoundArrowCounts: LiveData<List<RoundArrowCount>>
    private lateinit var allRoundDistances: LiveData<List<RoundDistance>>

    @Before
    fun before() {
        resources = mock()

        allRounds = mock()
        allRoundSubTypes = mock()
        allRoundArrowCounts = mock()
        allRoundDistances = mock()
        Mockito.`when`(viewModel.allRounds).thenAnswer { allRounds }
        Mockito.`when`(viewModel.allRoundSubTypes).thenAnswer { allRoundSubTypes }
        Mockito.`when`(viewModel.allRoundArrowCounts).thenAnswer { allRoundArrowCounts }
        Mockito.`when`(viewModel.allRoundDistances).thenAnswer { allRoundDistances }
    }

    @Test
    fun testGetAvailableRoundsNoValues() {
        val noRounds = "test"
        val roundSel = setupRoundSelection(mapOf(Pair(R.string.create_round__no_rounds_found, noRounds)), listOf())
        val available = roundSel.getAvailableRounds()
        Assert.assertEquals(1, available.size)
        Assert.assertEquals(noRounds, available[0])
    }

    @Test
    fun testGetAvailableRounds() {
        val noRound = "test"
        val size = 10
        val rounds = TestData.generateRounds(size)
        val roundSel = setupRoundSelection(mapOf(Pair(R.string.create_round__no_round, noRound)), rounds)
        val available = roundSel.getAvailableRounds()
        Assert.assertEquals(listOf(null).plus(rounds).map { it?.displayName ?: noRound }, available)
    }

    @Test
    fun testGetRoundSubtypesNoRoundsInDatabase() {
        val roundSel = setupRoundSelection(mapOf(), listOf())
        val available = roundSel.getRoundSubtypes(0)
        Assert.assertEquals(null, available)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGetRoundSubtypesInvalidIndexHigh() {
        val roundSel = setupRoundSelection(mapOf(), listOf())
        roundSel.getRoundSubtypes(10)
    }

    @Test
    fun testGetRoundSubtypesNoSubtypesInDatabase() {
        val rounds = TestData.generateRounds(2)
        val roundSel = setupRoundSelection(mapOf(), rounds)
        val available = roundSel.getRoundSubtypes(0)
        Assert.assertEquals(null, available)
    }

    @Test
    fun testGetRoundSubtypesNoSubtypesForRound() {
        val rounds = TestData.generateRounds(5)
        val subtypes = TestData.generateSubTypes(3)
        val roundSel = setupRoundSelection(mapOf(), rounds, subtypes)
        val available = roundSel.getRoundSubtypes(4)
        Assert.assertEquals(null, available)
    }

    @Test
    fun testGetRoundSubtypesOneSubtypesForRound() {
        val rounds = TestData.generateRounds(5)
        val subtypes = TestData.generateSubTypes(3, 1)
        val roundSel = setupRoundSelection(mapOf(), rounds, subtypes)
        val available = roundSel.getRoundSubtypes(4)
        Assert.assertEquals(null, available)
    }

    @Test
    fun testGetRoundSubtypesManySubtypesForRound() {
        val rounds = TestData.generateRounds(5)
        val subtypes = TestData.generateSubTypes(3, 3, 3)
        val roundSel = setupRoundSelection(mapOf(), rounds, subtypes)
        val available = roundSel.getRoundSubtypes(2)
        Assert.assertEquals(subtypes.filter { it.roundId == 2 }.map { it.name }, available)
    }

    @Test
    fun testGetArrowCountIndicatorTextNoData() {
        val roundSel = setupRoundSelection(mapOf(), listOf())
        val text = roundSel.getArrowCountIndicatorText(0)
        Assert.assertEquals(null, text)
    }

    @Test
    fun testGetArrowCountIndicatorText() {
        val index = 1
        val size = 3
        val arrowCounts = TestData.generateArrowCounts(2, size, size)
        val roundSel = setupRoundSelection(mapOf(), TestData.generateRounds(5), arrows = arrowCounts)
        val text = roundSel.getArrowCountIndicatorText(index)
        if (text != null) {
            Assert.assertEquals(size - 1, text.count { it == ',' })
            val counts = text.split(", ").map { it.toInt() }
            Assert.assertEquals(
                    arrowCounts.filter { it.roundId == index }.sortedBy { it.distanceNumber }
                            .map { it.arrowCount / 12 }, counts
            )
        }
        else {
            Assert.fail("Text should not be null")
        }
    }

    @Test
    fun testGetDistanceIndicatorTextNoData() {
        val roundSel = setupRoundSelection(mapOf(), listOf())
        val text = roundSel.getDistanceIndicatorText(0, null)
        Assert.assertEquals(null, text)
    }

    @Test
    fun testGetDistanceIndicatorTextWithSubtype() {
        val roundIndex = 2
        val subtypeIndex = 0
        val size = 3
        val rounds = TestData.generateRounds(5)
        val distances = TestData.generateDistances(3, size, size)
        val subtypes = TestData.generateSubTypes(3)
        val roundSel = setupRoundSelection(
                mapOf(Pair(R.string.units_meters_short, "m")), rounds, subtypes = subtypes, distances = distances
        )
        roundSel.getRoundSubtypes(1)
        val text = roundSel.getDistanceIndicatorText(roundIndex, 0)
        if (text != null) {
            Assert.assertEquals(size - 1, text.count { it == ',' })
            val actualDistances = text.replace("m", "").split(", ").map { it.toInt() }
            // roundIndex = roundId because roundIds start at 1 and position 0 is null placeholder
            // subtypeIndex + 1 because subtypeIds start at 1
            val expectedDistances =
                    distances.filter { it.roundId == roundIndex && it.subTypeId == subtypeIndex + 1 }
                            .sortedBy { it.distanceNumber }.map { it.distance }

            Assert.assertEquals(expectedDistances, actualDistances)
        }
        else {
            Assert.fail("Text should not be null")
        }
    }

    @Test
    fun testGetDistanceIndicatorTextMetric() {
        val index = 2
        val meters = "m"
        val size = 3
        val rounds = TestData.generateRounds(5)
        val distances = TestData.generateDistances(3, size, size).filter { it.subTypeId == 1 }
        val roundSel = setupRoundSelection(
                mapOf(Pair(R.string.units_meters_short, meters)), rounds, distances = distances
        )
        roundSel.getRoundSubtypes(index)
        val text = roundSel.getDistanceIndicatorText(index, null)
        if (text != null) {
            Assert.assertEquals(size, text.count { it == meters[0] })
            Assert.assertEquals(size - 1, text.count { it == ',' })
            val counts = text.split(", ").map { it.replace(meters, "").toInt() }
            Assert.assertEquals(
                    distances.filter { it.roundId == index }.sortedBy { it.distanceNumber }.map { it.distance }, counts
            )
        }
        else {
            Assert.fail("Text should not be null")
        }
    }

    @Test
    fun testGetDistanceIndicatorTextImperial() {
        val index = 1
        val yards = "yd"
        val size = 3
        val rounds = TestData.generateRounds(5)
        val distances = TestData.generateDistances(2, size, size).filter { it.subTypeId == 1 }
        val roundSel = setupRoundSelection(
                mapOf(Pair(R.string.units_yards_short, yards)), rounds, distances = distances
        )
        val text = roundSel.getDistanceIndicatorText(index, null)
        if (text != null) {
            Assert.assertEquals(size, text.count { it == yards[0] })
            Assert.assertEquals(size - 1, text.count { it == ',' })
            val counts = text.split(", ").map { it.replace(yards, "").toInt() }
            Assert.assertEquals(
                    distances.filter { it.roundId == index }.sortedBy { it.distanceNumber }.map { it.distance }, counts
            )
        }
        else {
            Assert.fail("Text should not be null")
        }
    }

    /**
     * Set up mocks for resources and database calls and instantiate the RoundSelection
     * @param strings resource id mapped to what it should return
     */
    private fun setupRoundSelection(
            strings: Map<Int, String>, rounds: List<Round>, subtypes: List<RoundSubType> = listOf(),
            arrows: List<RoundArrowCount> = listOf(), distances: List<RoundDistance> = listOf()
    ): RoundSelection {
        /*
         * Set up strings
         */
        Mockito.`when`(resources.getString(any())).thenAnswer { invocation ->
            val str = strings[invocation.getArgument(0)]
            if (str == null) {
                Assert.fail("Bad string passed to resources")
            }
            str
        }

        /*
         * Set up observations
         */
        Mockito.`when`(allRounds.observe(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<Observer<List<Round>>>(1).onChanged(rounds)
        }
        Mockito.`when`(allRoundSubTypes.observe(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<Observer<List<RoundSubType>>>(1).onChanged(subtypes)
        }
        Mockito.`when`(allRoundArrowCounts.observe(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<Observer<List<RoundArrowCount>>>(1).onChanged(arrows)
        }
        Mockito.`when`(allRoundDistances.observe(any(), any())).thenAnswer { invocation ->
            invocation.getArgument<Observer<List<RoundDistance>>>(1).onChanged(distances)
        }

        return RoundSelection(resources, viewModel, lifecycleOwner)
    }
}