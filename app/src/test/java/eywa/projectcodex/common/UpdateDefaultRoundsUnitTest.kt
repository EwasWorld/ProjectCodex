package eywa.projectcodex.common

import android.content.SharedPreferences
import android.content.res.Resources
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.*
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import java.io.FileInputStream
import java.io.InputStream
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateDefaultRoundsUnitTest {
    // TODO_CURRENT See unit style tests in android test too

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var sut: UpdateDefaultRoundsTask

    private lateinit var repo: RoundRepo
    private lateinit var resources: Resources
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor

    private lateinit var capturedUpdates: MutableList<Map<Any, UpdateType>>

    private fun SetupParams.setup() {
        capturedUpdates = mutableListOf()

        resources = mock { on { openRawResource(any()) } doReturn getInputStream() }

        repo = mock {
            on { fullRoundsInfo } doReturn flow { emit(fullRoundInfo) }
            onBlocking { updateRounds(any()) } doAnswer {
                @Suppress("UNCHECKED_CAST")
                capturedUpdates.add(it.arguments[0] as Map<Any, UpdateType>)
                Unit
            }
        }

        sharedPrefsEditor = mock {}
        sharedPrefs = mock {
            on { getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1) } doReturn currentDbVersion
            on { edit() } doReturn sharedPrefsEditor
        }

        sut = UpdateDefaultRoundsTask(
                repository = repo,
                resources = resources,
                sharedPreferences = sharedPrefs,
                logger = mock {},
                dispatcher = dispatcher,
        )
    }

    /**
     * Check that the current default round data is parsable
     * Log the time taken to complete
     */
    @Test
    fun testCurrentDefaultRoundInfoFile() = runTest(dispatcher) {
        SetupParams().apply {
            setFileStream(FileInputStream("src\\main\\res\\general\\raw\\default_rounds_data.json"))
            setup()
        }

        val timeTracker = TimeTracker()
        val progressListener = launch {
            sut.state.collect {
                when (it) {
                    is StartProcessingNew -> {
                        if (!timeTracker.started) {
                            timeTracker.start(it.currentItemIndex, it.totalItems)
                        }
                        else {
                            timeTracker.processItem(it.currentItemIndex)
                        }
                    }
                    is DeletingOld -> timeTracker.finish()
                    else -> {}
                }
            }
        }

        val startTime = Calendar.getInstance()
        assert(sut.runTask())
        assertEquals(
                Complete(3, CompletionType.COMPLETE),
                sut.state.value
        )
        val endTime = Calendar.getInstance()
        val duration = startTime.toInstant().until(endTime.toInstant(), ChronoUnit.MILLIS)
        println("Total time to complete: $duration ms")

        timeTracker.printResults()
        progressListener.cancel()
    }

    /**
     * Testing json for two correct rounds results in the correct insert database calls
     */
    @Test
    fun testNewRounds() = runTest(dispatcher) {
        SetupParams().apply {
            setFileText("${TestData.START_JSON}${TestData.YORK_JSON},${TestData.ST_GEORGE_JSON}${TestData.END_JSON}")
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        StartProcessingNew(null, 1, 2),
                        StartProcessingNew(null, 2, 2),
                        DeletingOld(null),
                        Complete(TestData.FILE_DB_VERSION, CompletionType.COMPLETE),
                ),
                actualStates,
        )

        verify(sharedPrefs).getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
        verify(sharedPrefsEditor).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, TestData.FILE_DB_VERSION)

        checkCapturedUpdates(
                TestData.YORK_ALL_ROUND_OBJECTS.associateWith { UpdateType.NEW },
                TestData.ST_GEORGE_ALL_ROUND_OBJECTS.associateWith { UpdateType.NEW },
        )
    }

    /**
     * Testing that round objects in the database that differ from the provided JSON are updated with the JSON values
     */
    @Test
    fun testUpdateRounds() = runTest(dispatcher) {
        val dbItem = FullRoundInfo(
                round = TestData.YORK_ROUND_OBJECT.copy(isOutdoor = !TestData.YORK_ROUND_OBJECT.isOutdoor),
                roundSubTypes = listOf(TestData.YORK_SUB_TYPE_OBJECTS[0].copy(name = "SomeSillyName"))
                        .plus(TestData.YORK_SUB_TYPE_OBJECTS.drop(1)),
                roundArrowCounts =
                listOf(TestData.YORK_ARROW_COUNT_OBJECTS[0].copy(faceSizeInCm = 10_000.0, arrowCount = 10_000))
                        .plus(TestData.YORK_ARROW_COUNT_OBJECTS.drop(1)),
                roundDistances = listOf(TestData.YORK_DISTANCE_OBJECTS[0].copy(distance = 10_000))
                        .plus(TestData.YORK_DISTANCE_OBJECTS.drop(1)),
        )

        SetupParams(
                fullRoundInfo = listOf(dbItem),
        ).apply {
            setFileText("${TestData.START_JSON}${TestData.YORK_JSON}${TestData.END_JSON}")
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        StartProcessingNew(null, 1, 1),
                        DeletingOld(null),
                        Complete(TestData.FILE_DB_VERSION, CompletionType.COMPLETE),
                ),
                actualStates,
        )

        verify(sharedPrefs).getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
        verify(sharedPrefsEditor).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, TestData.FILE_DB_VERSION)

        checkCapturedUpdates(
                listOf(
                        TestData.YORK_ROUND_OBJECT,
                        TestData.YORK_ARROW_COUNT_OBJECTS[0],
                        TestData.YORK_SUB_TYPE_OBJECTS[0],
                        TestData.YORK_DISTANCE_OBJECTS[0],
                ).associateWith { UpdateType.UPDATE }
        )
    }

    /**
     * Testing that round objects in the database that don't exist in the provided JSON are deleted
     */
    @Test
    fun testDeleteRounds() = runTest(dispatcher) {
        SetupParams(
                fullRoundInfo = listOf(TestData.ST_GEORGE_FULL_ROUND_DATA, TestData.YORK_FULL_ROUND_DATA),
        ).apply {
            setFileText("${TestData.START_JSON}${TestData.ST_GEORGE_JSON}${TestData.END_JSON}")
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        StartProcessingNew(null, 1, 1),
                        DeletingOld(null),
                        Complete(TestData.FILE_DB_VERSION, CompletionType.COMPLETE),
                ),
                actualStates,
        )

        verify(sharedPrefs).getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
        verify(sharedPrefsEditor).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, TestData.FILE_DB_VERSION)

        checkCapturedUpdates(
                TestData.YORK_ALL_ROUND_OBJECTS.associateWith { UpdateType.DELETE },
        )
    }

    /**
     * Test that is the database is already up to date, the task will cancel early
     */
    @Test
    fun testUpToDate() = runTest(dispatcher) {
        SetupParams(
                currentDbVersion = TestData.FILE_DB_VERSION,
        ).apply {
            setFileText(TestData.EMPTY_ROUNDS_JSON)
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        Complete(TestData.FILE_DB_VERSION, CompletionType.ALREADY_UP_TO_DATE),
                ),
                actualStates,
        )

        verify(sharedPrefs).getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
        verify(sharedPrefsEditor, never()).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, TestData.FILE_DB_VERSION)

        checkCapturedUpdates()
    }

    /**
     * Test [UpdateDefaultRoundsTask] correctly reports a JSON error
     */
    @Test
    fun testJsonError() = runTest(dispatcher) {
        SetupParams().apply {
            setFileText("${TestData.START_JSON}{{{${TestData.YORK_JSON}${TestData.END_JSON}")
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        InternalError(null, "Failed to parse default rounds file"),
                ),
                actualStates,
        )
    }

    /**
     * Test that it errors if any rounds have a duplicate/equivalent names in the JSON
     */
    @Test
    fun testDuplicateRoundNameDb() = runTest(dispatcher) {
        val json = """
            ${TestData.START_JSON}
                ${TestData.ST_GEORGE_JSON},
                {
                   "roundName": "stgeoRge.",
                   "outdoor": true,
                   "isMetric": false,
                   "fiveArrowEnd": false,
                   "permittedFaces": [],
                    ${TestData.YORK_SUB_TYPES_JSON},
                    ${TestData.YORK_ARROW_COUNTS_JSON},
                    ${TestData.YORK_DISTANCES_JSON}
                }
            ${TestData.END_JSON}
        """

        SetupParams(
                fullRoundInfo = listOf(TestData.YORK_FULL_ROUND_DATA)
        ).apply {
            setFileText(json)
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                listOf(
                        null,
                        Initialising,
                        StartProcessingNew(null, 1, 2),
                        StartProcessingNew(null, 2, 2),
                        InternalError(null, "Duplicate name in default rounds file: stgeorge"),
                ),
                actualStates,
        )
    }

    /**
     * Test that an error is thrown if the size of a roundDistances != subTypes * arrowCounts
     */
    @Test
    fun testBadDistancesSize() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        "roundDistances": [
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 1,
                                "distance": 100
                            }
                        ]
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if any round's sub types have a duplicate/equivalent names in the JSON
     */
    @Test
    fun testDuplicateSubTypeName() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        "roundSubTypes": [
                            {
                                "roundSubTypeId": 1,
                                "subTypeName": "York",
                                "gentsUnder": null,
                                "ladiesUnder": null
                            },
                            {
                                "roundSubTypeId": 2,
                                "subTypeName": "york!",
                                "gentsUnder": 18,
                                "ladiesUnder": null
                            },
                            {
                                "roundSubTypeId": 3,
                                "subTypeName": "Bristol II",
                                "gentsUnder": 16,
                                "ladiesUnder": 18
                            },
                            {
                                "roundSubTypeId": 4,
                                "subTypeName": "Bristol V",
                                "gentsUnder": 0,
                                "ladiesUnder": 12
                            }
                        ],
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        ${TestData.YORK_DISTANCES_JSON}
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if any round's sub types have a duplicate/equivalent ids in the JSON
     */
    @Test
    fun testDuplicateSubTypeIds() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        "roundSubTypes": [
                            {
                                "roundSubTypeId": 1,
                                "subTypeName": "York",
                                "gentsUnder": null,
                                "ladiesUnder": null
                            },
                            {
                                "roundSubTypeId": 1,
                                "subTypeName": "Hereford (Bristol I)",
                                "gentsUnder": 18,
                                "ladiesUnder": null
                            },
                            {
                                "roundSubTypeId": 3,
                                "subTypeName": "Bristol II",
                                "gentsUnder": 16,
                                "ladiesUnder": 18
                            },
                            {
                                "roundSubTypeId": 4,
                                "subTypeName": "Bristol V",
                                "gentsUnder": 0,
                                "ladiesUnder": 12
                            }
                        ],
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        ${TestData.YORK_DISTANCES_JSON}
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if any round's arrow counts have a identical distance numbers in the JSON
     */
    @Test
    fun testDuplicateArrowCountDistanceNumbers() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        "roundArrowCounts": [
                            {
                                "distanceNumber": 1,
                                "faceSizeInCm": 122,
                                "arrowCount": 72
                            },
                            {
                                "distanceNumber": 1,
                                "faceSizeInCm": 122,
                                "arrowCount": 48
                            }
                        ],
                        ${TestData.YORK_DISTANCES_JSON}
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if any round's distances have an identical distance number and sub type id in the
     *    JSON
     */
    @Test
    fun testDuplicateDistanceKeys() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        "roundDistances": [
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 1,
                                "distance": 100
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 1,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 2,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 2,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 3,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 3,
                                "distance": 50
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 4,
                                "distance": 30
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 4,
                                "distance": 20
                            }
                        ]
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if one of the keys in roundDistances doesn't match those of of subTypes or
     *    arrowCounts
     */
    @Test
    fun testDistanceKeysMismatch() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        "roundDistances": [
                            {
                                "distanceNumber": 17,
                                "roundSubTypeId": 1,
                                "distance": 100
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 17,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 2,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 2,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 3,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 3,
                                "distance": 50
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 4,
                                "distance": 30
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 4,
                                "distance": 20
                            }
                        ]
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if a later distance entry with a higher actual distance (e.g. first distance is 100
     *    yards, second distance is 200 yards)
     */
    @Test
    fun testNonDescendingDistances() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                        "roundDistances": [
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 1,
                                "distance": 100
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 1,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 2,
                                "distance": 80
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 2,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 3,
                                "distance": 60
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 3,
                                "distance": 50
                            },
                            {
                                "distanceNumber": 1,
                                "roundSubTypeId": 4,
                                "distance": 30
                            },
                            {
                                "distanceNumber": 2,
                                "roundSubTypeId": 4,
                                "distance": 120
                            }
                        ]
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0")
    }

    /**
     * Test that an error is thrown if a round has no arrow counts
     */
    @Test
    fun testNoArrowCountsInput() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_DISTANCES_JSON},
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0. roundArrowCounts is not an array")
    }

    /**
     * Test that an error is thrown if a round has no distances
     */
    @Test
    fun testNoDistancesInput() {
        val json = """
                ${TestData.START_JSON}
                    {
                        ${TestData.YORK_MAIN_JSON},
                        ${TestData.YORK_SUB_TYPES_JSON},
                        ${TestData.YORK_ARROW_COUNTS_JSON},
                    }
                ${TestData.END_JSON}
        """
        testError(json, "Failed to create rounds object at index 0. roundDistances is not an array")
    }


    /**
     */
    private fun testError(json: String, errorMessage: String) = runTest(dispatcher) {
        SetupParams().apply {
            setFileText(json)
            setup()
        }

        val collector = StateCollector().apply { startCollecting() }

        assert(sut.runTask())

        val actualStates = collector.stopCollecting()
        assertEquals(
                InternalError(null, errorMessage),
                actualStates.last(),
        )
    }

    /**
     * Compares [expectedUpdates] with [capturedUpdates], providing nicer error messages on fail
     */
    private fun checkCapturedUpdates(vararg expectedUpdates: Map<Any, UpdateType>) {
        assertEquals(
                "Repo.updateItems() incorrect number of invocations",
                expectedUpdates.size,
                capturedUpdates.size,
        )
        for (comparisonIndex in expectedUpdates.indices) {
            assertTrue(
                    "Repo.updateItems() not enough invocations. Next expected call: ${expectedUpdates[comparisonIndex]}",
                    capturedUpdates.size > comparisonIndex
            )

            val actual = capturedUpdates[comparisonIndex].toMutableMap()
            val expected = expectedUpdates[comparisonIndex]

            expected.entries.forEach { (expectedKey, expectedValue) ->
                assertEquals(
                        "Value for \n$expectedKey\n at index $comparisonIndex are different",
                        expectedValue,
                        actual[expectedKey],
                )
                actual.remove(expectedKey)
            }
            assertEquals(
                    "Actual at index $comparisonIndex has extra items",
                    mutableMapOf<Any, UpdateType>(),
                    actual,
            )
        }
    }

    private inner class StateCollector {
        private var collectorJob: Job? = null
        private val collectedStates = mutableListOf<UpdateDefaultRoundsState?>()

        fun TestScope.startCollecting() {
            require(collectorJob == null) { "Cannot call startCollecting more than once" }
            collectorJob = launch {
                sut.state.collect { collectedStates.add(it) }
            }
        }

        fun stopCollecting(): List<UpdateDefaultRoundsState?> {
            require(collectorJob != null) { "Must call startCollecting first" }
            collectorJob!!.cancel()
            return collectedStates
        }
    }

    private class TimeTracker {
        var started = false
            private set
        private var finished = false
        private var totalItems: Int? = null
        private var lastSeenIndex: Int? = null
        private var timeCurrentStarted: Calendar? = null
        private val itemCompletionTimes = mutableListOf<Long>()

        /**
         * Starts the tracker,
         * marking the current time as the start of processing item [itemIndex] of [totalItems],
         * marking this [TimeTracker] as started
         */
        fun start(itemIndex: Int, totalItems: Int) {
            require(!started) { "Cannot call start twice" }

            timeCurrentStarted = Calendar.getInstance()
            this.lastSeenIndex = itemIndex
            this.totalItems = totalItems
            started = true
        }

        /**
         * Updates time taken for previous item and restarts the timer for item [itemIndex].
         * Prints the time taken for the previous item and an estimate of how long it will take
         * to complete remaining items
         */
        fun processItem(itemIndex: Int) {
            require(started) { "Must call start(...) first" }
            require(!finished) { "Cannot be called after finish(...)" }

            val currentTime = Calendar.getInstance()

            val countOfItemsProcessed = itemIndex - lastSeenIndex!!
            if (countOfItemsProcessed < 1) return


            val timeToProcess = timeCurrentStarted!!.toInstant()
                    .until(currentTime.toInstant(), ChronoUnit.MILLIS)
            itemCompletionTimes.add(timeToProcess / countOfItemsProcessed)


            val countRemainingToProcess = totalItems!! - (itemIndex - 1)
            val msToCompletion = (itemCompletionTimes.average() * countRemainingToProcess).roundToLong()

            val itemsProcessedString = if (countOfItemsProcessed == 1) {
                "Item ${itemIndex - 1} completed in"
            }
            else {
                "Items $lastSeenIndex to ${itemIndex - 1} completed in avg"
            }
            println(
                    "$itemsProcessedString $timeToProcess ms." +
                            " Estimated completion in $msToCompletion ms"
            )

            lastSeenIndex = itemIndex
            timeCurrentStarted = currentTime
        }

        /**
         * Updates time taken for previous item and marks this [TimeTracker] as finished
         */
        fun finish() {
            require(started) { "Must call start(...) first" }
            require(!finished) { "Cannot be called after finish(...)" }

            if (lastSeenIndex != totalItems) fail("Not all items processed, last seen: $lastSeenIndex of $totalItems")
            processItem(totalItems!! + 1)
            println("-----------------------------")
            finished = true
        }

        /**
         * Prints how many items were processed and the average time it took to complete each one
         */
        fun printResults() {
            require(started) { "Must call start(...) first" }
            require(finished) { "Must call finish(...) first" }
            if (itemCompletionTimes.isNotEmpty()) {
                println(
                        "Successfully processed $totalItems items." +
                                " Each completed in ${itemCompletionTimes.average().roundToLong()} ms on average"
                )
            }
        }
    }

    class SetupParams(
            val currentDbVersion: Int = DEFAULT_DB_VERSION,
            val fullRoundInfo: List<FullRoundInfo> = listOf(),
    ) {
        private var fileText: String? = null
        private var fileStream: InputStream? = null

        fun getInputStream() = fileStream ?: (fileText ?: TestData.EMPTY_ROUNDS_JSON).byteInputStream()

        fun setFileText(value: String) {
            fileStream = null
            fileText = value
        }

        fun setFileStream(value: InputStream) {
            fileText = null
            fileStream = value
        }

        companion object {
            const val DEFAULT_DB_VERSION = -1
        }
    }

    object TestData {
        const val FILE_DB_VERSION = 5
        const val START_JSON = """{"version": $FILE_DB_VERSION, "rounds": ["""
        const val END_JSON = """]}"""

        const val EMPTY_ROUNDS_JSON = START_JSON + END_JSON

        /*
         * York
         */
        const val YORK_MAIN_JSON = """
              "roundName": "York",
              "outdoor": true,
              "isMetric": false,
              "fiveArrowEnd": false,
              "permittedFaces": []
            """
        const val YORK_SUB_TYPES_JSON = """
              "roundSubTypes": [
                {
                  "roundSubTypeId": 1,
                  "subTypeName": "York",
                  "gentsUnder": null,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 2,
                  "subTypeName": "Hereford (Bristol I)",
                  "gentsUnder": 18,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 3,
                  "subTypeName": "Bristol II",
                  "gentsUnder": 16,
                  "ladiesUnder": 18
                },
                {
                  "roundSubTypeId": 4,
                  "subTypeName": "Bristol V",
                  "gentsUnder": 0,
                  "ladiesUnder": 12
                }
              ]
            """
        const val YORK_ARROW_COUNTS_JSON = """
              "roundArrowCounts": [
                {
                  "distanceNumber": 1,
                  "faceSizeInCm": 122,
                  "arrowCount": 72
                },
                {
                  "distanceNumber": 2,
                  "faceSizeInCm": 122,
                  "arrowCount": 48
                }
              ]
            """
        const val YORK_DISTANCES_JSON = """
              "roundDistances": [
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 1,
                  "distance": 100
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 1,
                  "distance": 80
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 2,
                  "distance": 80
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 2,
                  "distance": 60
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 3,
                  "distance": 60
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 3,
                  "distance": 50
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 4,
                  "distance": 30
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 4,
                  "distance": 20
                }
              ]
            """
        const val YORK_JSON = """
                {
                    $YORK_MAIN_JSON,
                    $YORK_SUB_TYPES_JSON,
                    $YORK_ARROW_COUNTS_JSON,
                    $YORK_DISTANCES_JSON
                },
            """
        val YORK_ROUND_OBJECT = Round(5, "york", "York", true, false, listOf(), true, false)
        val YORK_ARROW_COUNT_OBJECTS = listOf(
                RoundArrowCount(5, 1, 122.0, 72),
                RoundArrowCount(5, 2, 122.0, 48)
        )
        val YORK_SUB_TYPE_OBJECTS = listOf(
                RoundSubType(5, 1, "York"),
                RoundSubType(5, 2, "Hereford (Bristol I)", 18),
                RoundSubType(5, 3, "Bristol II", 16, 18),
                RoundSubType(5, 4, "Bristol V", 0, 12)
        )
        val YORK_DISTANCE_OBJECTS = listOf(
                RoundDistance(5, 1, 1, 100),
                RoundDistance(5, 2, 1, 80),
                RoundDistance(5, 1, 2, 80),
                RoundDistance(5, 2, 2, 60),
                RoundDistance(5, 1, 3, 60),
                RoundDistance(5, 2, 3, 50),
                RoundDistance(5, 1, 4, 30),
                RoundDistance(5, 2, 4, 20)
        )
        val YORK_ALL_ROUND_OBJECTS = listOf(
                listOf(YORK_ROUND_OBJECT), YORK_ARROW_COUNT_OBJECTS, YORK_SUB_TYPE_OBJECTS, YORK_DISTANCE_OBJECTS
        ).flatten()
        val YORK_FULL_ROUND_DATA = FullRoundInfo(
                round = YORK_ROUND_OBJECT,
                roundSubTypes = YORK_SUB_TYPE_OBJECTS,
                roundDistances = YORK_DISTANCE_OBJECTS,
                roundArrowCounts = YORK_ARROW_COUNT_OBJECTS,
        )

        /*
         * St. George
         */
        const val ST_GEORGE_JSON = """
            {
              "roundName": "St. George",
              "outdoor": false,
              "isMetric": true,
              "fiveArrowEnd": true,
              "permittedFaces": [
                "NO_TRIPLE",
                "FIVE_CENTRE"
              ],
              "roundSubTypes": [
                {
                  "roundSubTypeId": 1,
                  "subTypeName": "St. George",
                  "gentsUnder": null,
                  "ladiesUnder": null
                },
                {
                  "roundSubTypeId": 2,
                  "subTypeName": "Albion",
                  "gentsUnder": null,
                  "ladiesUnder": null
                }
              ],
              "roundArrowCounts": [
                {
                  "distanceNumber": 1,
                  "faceSizeInCm": 122,
                  "arrowCount": 36
                },
                {
                  "distanceNumber": 2,
                  "faceSizeInCm": 122,
                  "arrowCount": 36
                }
              ],
              "roundDistances": [
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 1,
                  "distance": 100
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 1,
                  "distance": 80
                },
                {
                  "distanceNumber": 1,
                  "roundSubTypeId": 2,
                  "distance": 80
                },
                {
                  "distanceNumber": 2,
                  "roundSubTypeId": 2,
                  "distance": 60
                }
              ]
            }
            """
        val ST_GEORGE_ALL_ROUND_OBJECTS = listOf(
                Round(
                        6,
                        "stgeorge",
                        "St. George",
                        false,
                        true,
                        listOf("NO_TRIPLE", "FIVE_CENTRE"),
                        true,
                        true
                ),
                RoundSubType(6, 1, "St. George"),
                RoundSubType(6, 2, "Albion"),
                RoundArrowCount(6, 1, 122.0, 36),
                RoundArrowCount(6, 2, 122.0, 36),
                RoundDistance(6, 1, 1, 100),
                RoundDistance(6, 2, 1, 80),
                RoundDistance(6, 1, 2, 80),
                RoundDistance(6, 2, 2, 60)
        )
        val ST_GEORGE_FULL_ROUND_DATA = FullRoundInfo(
                round = ST_GEORGE_ALL_ROUND_OBJECTS.filterIsInstance<Round>().first(),
                roundSubTypes = ST_GEORGE_ALL_ROUND_OBJECTS.filterIsInstance<RoundSubType>(),
                roundDistances = ST_GEORGE_ALL_ROUND_OBJECTS.filterIsInstance<RoundDistance>(),
                roundArrowCounts = ST_GEORGE_ALL_ROUND_OBJECTS.filterIsInstance<RoundArrowCount>(),
        )
    }
}