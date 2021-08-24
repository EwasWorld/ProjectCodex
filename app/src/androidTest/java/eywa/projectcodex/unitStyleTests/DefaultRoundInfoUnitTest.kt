package eywa.projectcodex.unitStyleTests

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import eywa.projectcodex.R
import eywa.projectcodex.TestUtils
import eywa.projectcodex.common.latchAwaitTimeSeconds
import eywa.projectcodex.common.latchAwaitTimeUnit
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Captor
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.io.InputStream
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong
import kotlin.reflect.KClass

@RunWith(AndroidJUnit4::class)
class DefaultRoundInfoUnitTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @After
    fun teardown() {
        UpdateDefaultRounds.hardResetState()
    }

    /**
     * Create a shared preferences mock that will return [currentVersion] when [SharedPrefs.DEFAULT_ROUNDS_VERSION]
     * is requested
     */
    private fun getSharedPreferencesMock(currentVersion: Int = -1): Pair<SharedPreferences, SharedPreferences.Editor> {
        val sharedPreferences = mock(SharedPreferences::class.java)
        val sharedPreferencesEditor = mock(SharedPreferences.Editor::class.java)
        `when`(sharedPreferences.getInt(eq(SharedPrefs.DEFAULT_ROUNDS_VERSION.key), anyInt()))
                .thenReturn(currentVersion)
        `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        `when`(sharedPreferencesEditor.putInt(eq(SharedPrefs.DEFAULT_ROUNDS_VERSION.key), anyInt()))
                .thenReturn(sharedPreferencesEditor)
        return sharedPreferences to sharedPreferencesEditor
    }

    /**
     * Check that the current default round data is parsable
     * Log the time taken to complete
     */
    @Test
    fun testCurrentDefaultRoundInfoFile() {
        // TODO Swap back to unit test and use file directly
        // val mockInfo = MockInfo(FileInputStream("src/main/res/raw/default_rounds_data.json"))
        val mockInfo = MockInfo.Builder(
                getInstrumentation().targetContext.resources.openRawResource(R.raw.default_rounds_data)
        ).build()

        /*
         * Observe state
         */
        var currentIndex: Int? = null
        var timeCurrentStarted: Date? = null
        val itemCompletionTimes = mutableListOf<Long>()
        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(LiveDataObserver.SimpleStateObserver(observerBuilder.latchCountDownFunction()))
                .setMessageObserver(Observer { msg ->
                    println(msg)
                    msg?.let { message ->
                        if (message.contains(" of ")) {
                            /*
                             * Log time taken to process item
                             */
                            val currentTime = Date()
                            val splitProgressString = message.split(" ")
                            val readIndex = Integer.parseInt(splitProgressString[0])
                            if (readIndex - 1 == currentIndex && timeCurrentStarted != null) {
                                val completionTime = timeCurrentStarted!!.toInstant()
                                        .until(currentTime.toInstant(), ChronoUnit.MILLIS)
                                itemCompletionTimes.add(completionTime)

                                // 1 indexed so +1
                                val remainingToProcess = Integer.parseInt(splitProgressString[2]) - readIndex + 1
                                val secondsToCompletion =
                                        (itemCompletionTimes.average() * remainingToProcess).roundToLong()
                                println(
                                        "Item $readIndex completed in $completionTime ms." +
                                                " Estimated completion in ${secondsToCompletion / 1000} seconds"
                                )
                            }
                            currentIndex = readIndex
                            timeCurrentStarted = currentTime
                        }
                    }
                }, 0) // Doesn't matter how many messages are called, this is tested later
                .build()
        observer.startObserving()

        /*
         * Run test
         */
        val startTime = Date()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)

        // Wait for the async task to finish
        observer.awaitCompletion(5, TimeUnit.MINUTES)
        val endTime = Date()
        val duration = startTime.toInstant().until(endTime.toInstant(), ChronoUnit.SECONDS)
        println("Time took to complete: $duration seconds")
        if (itemCompletionTimes.isNotEmpty()) {
            println(
                    "Successfully processed ${itemCompletionTimes.size} items." +
                            " Each completed in ${itemCompletionTimes.average().roundToLong()} seconds on average"
            )
        }
        observer.finishObserving()
    }

    /**
     * Testing json for two correct rounds results in the correct insert database calls
     */
    @Test
    fun testNewRounds() {
        val json = "${TestData.START_JSON}${TestData.YORK_JSON},${TestData.ST_GEORGE_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()
        val sharedPref = getSharedPreferencesMock()

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(LiveDataObserver.SimpleStateObserver(observerBuilder.latchCountDownFunction()))
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 2",
                                        "2 of 2",
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.general_complete]
                                ), observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, sharedPref.first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(
                TestData.YORK_ALL_ROUND_OBJECTS.plus(TestData.ST_GEORGE_ALL_ROUND_OBJECTS)
                        .map { it to UpdateType.NEW }.toMap()
        )
        verify(sharedPref.second).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, 1)
    }

    /**
     * Testing that round objects in the database that differ from the provided JSON are updated with the JSON values
     */
    @Test
    fun testUpdateRounds() {
        val json = "${TestData.START_JSON}${TestData.YORK_JSON}${TestData.END_JSON}"
        /*
         * Change the objects the DB will return so they're different from the json given
         */
        val updatedRound = Round(
                TestData.YORK_ROUND_OBJECT.roundId,
                TestData.YORK_ROUND_OBJECT.name,
                TestData.YORK_ROUND_OBJECT.displayName,
                !TestData.YORK_ROUND_OBJECT.isOutdoor,
                TestData.YORK_ROUND_OBJECT.isMetric,
                TestData.YORK_ROUND_OBJECT.permittedFaces,
                TestData.YORK_ROUND_OBJECT.isDefaultRound,
                TestData.YORK_ROUND_OBJECT.fiveArrowEnd
        )
        val updatedArrowCount = RoundArrowCount(
                TestData.YORK_ARROW_COUNT_OBJECTS[0].roundId,
                TestData.YORK_ARROW_COUNT_OBJECTS[0].distanceNumber,
                TestData.YORK_ARROW_COUNT_OBJECTS[0].faceSizeInCm + 20,
                TestData.YORK_ARROW_COUNT_OBJECTS[0].arrowCount + 30
        )
        val updatedSubType = RoundSubType(
                TestData.YORK_SUB_TYPE_OBJECTS[0].roundId,
                TestData.YORK_SUB_TYPE_OBJECTS[0].subTypeId,
                TestData.YORK_SUB_TYPE_OBJECTS[0].name + "update"
        )
        val updatedDistance = RoundDistance(
                TestData.YORK_DISTANCE_OBJECTS[0].roundId,
                TestData.YORK_DISTANCE_OBJECTS[0].distanceNumber,
                TestData.YORK_DISTANCE_OBJECTS[0].subTypeId,
                TestData.YORK_DISTANCE_OBJECTS[0].distance + 20
        )
        val mockInfo = MockInfo.Builder(json.byteInputStream())
                .setDbData(
                        listOf(
                                listOf(updatedRound, updatedArrowCount, updatedSubType, updatedDistance),
                                TestData.YORK_ARROW_COUNT_OBJECTS.subList(1, TestData.YORK_ARROW_COUNT_OBJECTS.size),
                                TestData.YORK_SUB_TYPE_OBJECTS.subList(1, TestData.YORK_SUB_TYPE_OBJECTS.size),
                                TestData.YORK_DISTANCE_OBJECTS.subList(1, TestData.YORK_DISTANCE_OBJECTS.size)
                        ).flatten()
                )
                .build()

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(LiveDataObserver.SimpleStateObserver(observerBuilder.latchCountDownFunction()))
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 1",
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.general_complete]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(
                listOf(
                        TestData.YORK_ROUND_OBJECT,
                        TestData.YORK_ARROW_COUNT_OBJECTS[0],
                        TestData.YORK_SUB_TYPE_OBJECTS[0],
                        TestData.YORK_DISTANCE_OBJECTS[0]
                ).map { it to UpdateType.UPDATE }.toMap()
        )
    }

    /**
     * Testing that round objects in the database that don't exist in the provided JSON are deleted
     */
    @Test
    fun testDeleteRounds() {
        val json = "${TestData.START_JSON}${TestData.ST_GEORGE_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream())
                .setDbData(TestData.ST_GEORGE_ALL_ROUND_OBJECTS.plus(TestData.YORK_ALL_ROUND_OBJECTS))
                .build()

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(LiveDataObserver.SimpleStateObserver(observerBuilder.latchCountDownFunction()))
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 1",
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.general_complete]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(TestData.YORK_ALL_ROUND_OBJECTS.map { it to UpdateType.DELETE }.toMap())
    }

    /**
     * Test [UpdateDefaultRounds]'s state transitions as expected from not started to complete
     */
    @Test
    fun testWorkingStateTransitions() {
        val json = "${TestData.START_JSON}${TestData.YORK_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()

        val expectedStates = mutableListOf(
                UpdateDefaultRounds.UpdateTaskState.NOT_STARTED,
                UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS,
                UpdateDefaultRounds.UpdateTaskState.COMPLETE
        )
        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(Observer { state ->
                    if (expectedStates.isEmpty()) {
                        Assert.fail("No more states expected but state changed to $state")
                    }
                    Assert.assertEquals(expectedStates.removeAt(0), state)
                    if (expectedStates.isEmpty()) {
                        observerBuilder.latchCountDownFunction()()
                    }
                }, 1)
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 1",
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.general_complete]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(TestData.YORK_ALL_ROUND_OBJECTS.map { it to UpdateType.NEW }.toMap())
    }

    /**
     * Test [UpdateDefaultRounds]'s state transitions as expected from not started to error
     */
    @Test
    fun testErrorStateTransitions() {
        // Invalid json syntax
        val json = "${TestData.START_JSON}{{{${TestData.YORK_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()

        val expectedStates = mutableListOf(
                UpdateDefaultRounds.UpdateTaskState.NOT_STARTED,
                UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS,
                UpdateDefaultRounds.UpdateTaskState.ERROR
        )
        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(Observer { state ->
                    if (expectedStates.isEmpty()) {
                        Assert.fail("No more states expected but state changed to $state")
                    }
                    Assert.assertEquals(expectedStates.removeAt(0), state)
                    if (expectedStates.isEmpty()) {
                        observerBuilder.latchCountDownFunction()()
                    }
                }, 1)
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.err__internal_error]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(mapOf())
    }

    /**
     * Test that is the database is already up to date, the task will cancel early
     */
    @Test
    fun testUpToDate() {
        val json = "${TestData.START_JSON}${TestData.YORK_JSON},${TestData.ST_GEORGE_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()
        val sharedPref = getSharedPreferencesMock(1)

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(
                        LiveDataObserver.SimpleStateObserver(
                                observerBuilder.latchCountDownFunction(),
                                UpdateDefaultRounds.UpdateTaskState.COMPLETE
                        )
                )
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_up_to_date]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, sharedPref.first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(mapOf())
        verify(sharedPref.second, times(0)).putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, 1)
    }

    /**
     * Test that an error is thrown if [RoundRepo.repositoryWriteLock] is already locked. Ensure the hold count remains
     *    correct
     */
    @Test
    fun testRepoAlreadyLocked() {
        Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
        Assert.assertTrue(RoundRepo.repositoryWriteLock.tryLock())
        Assert.assertEquals(1, RoundRepo.repositoryWriteLock.holdCount)
        checkErrorState(
                "${TestData.START_JSON}${TestData.YORK_JSON}${TestData.END_JSON}",
                listOf(),
                R.string.err_about__update_default_rounds_no_lock
        )
        Assert.assertEquals(1, RoundRepo.repositoryWriteLock.holdCount)
        RoundRepo.repositoryWriteLock.unlock()
        Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
    }

    /**
     * Test that [RoundRepo.repositoryWriteLock] is properly released after updating default rounds
     */
    @Test
    fun testLockedIsReleasedOnSuccess() {
        val json = "${TestData.START_JSON}${TestData.YORK_JSON}${TestData.END_JSON}"
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(LiveDataObserver.SimpleStateObserver(observerBuilder.latchCountDownFunction()))
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 1",
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.general_complete]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(TestData.YORK_ALL_ROUND_OBJECTS.map { it to UpdateType.NEW }.toMap())

        Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
        Assert.assertTrue(RoundRepo.repositoryWriteLock.tryLock())
        RoundRepo.repositoryWriteLock.unlock()
    }

    /**
     * Test that [RoundRepo.repositoryWriteLock] is properly released when an error is thrown
     */
    @Test
    fun testLockedIsReleasedOnError() {
        checkErrorState("", listOf())
        Assert.assertEquals(0, RoundRepo.repositoryWriteLock.holdCount)
        Assert.assertTrue(RoundRepo.repositoryWriteLock.tryLock())
        RoundRepo.repositoryWriteLock.unlock()
    }

    /**
     * Test that an error is thrown if any rounds have a duplicate/equivalent names in the JSON
     */
    @Test
    fun testDuplicateRoundNameDb() {
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
        val mockInfo = MockInfo.Builder(json.byteInputStream())
                .setDbData(TestData.YORK_ALL_ROUND_OBJECTS)
                .build()

        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(
                        LiveDataObserver.SimpleStateObserver(
                                observerBuilder.latchCountDownFunction(),
                                UpdateDefaultRounds.UpdateTaskState.ERROR
                        )
                )
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        "1 of 2",
                                        "2 of 2",
                                        MockInfo.defaultMap[R.string.err__internal_error]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(TestData.ST_GEORGE_ALL_ROUND_OBJECTS.map { it to UpdateType.NEW }.toMap())
    }

    /**
     * Test that an error is thrown if invalid JSON syntax
     */
    @Test
    fun testInvalidJson() {
        // Missing { for round
        checkErrorState(
                """
                    ${TestData.START_JSON}
                            ${TestData.YORK_MAIN_JSON},
                            ${TestData.YORK_SUB_TYPES_JSON},
                            ${TestData.YORK_ARROW_COUNTS_JSON},
                            ${TestData.YORK_DISTANCES_JSON}
                        }
                    ${TestData.END_JSON}
                """,
                listOf()
        )
    }

    /**
     * Test that an error is thrown if the size of a roundDistances != subTypes * arrowCounts
     */
    @Test
    fun testBadDistancesSize() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if any round's sub types have a duplicate/equivalent names in the JSON
     */
    @Test
    fun testDuplicateSubTypeName() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if any round's sub types have a duplicate/equivalent ids in the JSON
     */
    @Test
    fun testDuplicateSubTypeIds() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if any round's arrow counts have a identical distance numbers in the JSON
     */
    @Test
    fun testDuplicateArrowCountDistanceNumbers() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if any round's distances have an identical distance number and sub type id in the
     *    JSON
     */
    @Test
    fun testDuplicateDistanceKeys() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if one of the keys in roundDistances doesn't match those of of subTypes or
     *    arrowCounts
     */
    @Test
    fun testDistanceKeysMismatch() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if a later distance entry with a higher actual distance (e.g. first distance is 100
     *    yards, second distance is 200 yards)
     */
    @Test
    fun testNonDescendingDistances() {
        checkErrorState(
                """
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
        )
    }

    /**
     * Test that an error is thrown if a round has no arrow counts
     */
    @Test
    fun testNoArrowCountsInput() {
        checkErrorState(
                """
                    ${TestData.START_JSON}
                        {
                            ${TestData.YORK_MAIN_JSON},
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
        )
    }

    /**
     * Test that an error is thrown if a round has no distances
     */
    @Test
    fun testNoDistancesInput() {
        checkErrorState(
                """
                    ${TestData.START_JSON}
                        {
                            ${TestData.YORK_MAIN_JSON},
                            "roundArrowCounts": [
                                {
                                    "distanceNumber": 1,
                                    "faceSizeInCm": 122,
                                    "arrowCount": 72
                                }
                            ]
                        }
                    ${TestData.END_JSON}
                """
        )
    }

    /**
     * Common test for invalid json. Checks that no calls were made on the database and that the UpdateTaskState was set
     *   to ERROR
     * @param json json to test
     * @param extraMessages any messages that should come between the first initialising messages and the error message
     * @param errorMessageId resource ID of the expected error message to be printed out (default: internal error)
     */
    private fun checkErrorState(
            json: String,
            extraMessages: List<String> = listOf("1 of 1"),
            errorMessageId: Int = R.string.err__internal_error
    ) {
        val mockInfo = MockInfo.Builder(json.byteInputStream()).build()
        val observerBuilder = LiveDataObserver.Builder()
        val observer = observerBuilder
                .setStateObserver(
                        LiveDataObserver.SimpleStateObserver(
                                observerBuilder.latchCountDownFunction(),
                                UpdateDefaultRounds.UpdateTaskState.ERROR
                        )
                )
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.about__update_default_rounds_initialising],
                                        *extraMessages.toTypedArray(),
                                        MockInfo.defaultMap[errorMessageId]
                                ),
                                observerBuilder.latchCountDownFunction()
                        )
                ).build()

        observer.startObserving()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock, getSharedPreferencesMock().first)
        observer.awaitCompletion()
        observer.finishObserving()

        mockInfo.verifyUpdate(mapOf())
    }

    private class LiveDataObserver private constructor() {
        private var stateObserver: Observer<UpdateDefaultRounds.UpdateTaskState>? = null
        private var messageObserver: Observer<String?>? = null
        private val state = UpdateDefaultRounds.taskProgress.getState()
        private val message = UpdateDefaultRounds.taskProgress.getMessage()
        private var messageTracker: MessageTracker? = null
        private lateinit var latch: CountDownLatch

        fun startObserving() {
            stateObserver?.let { state.observeForever(it) }
            messageObserver?.let { message.observeForever(it) }
        }

        fun finishObserving() {
            stateObserver?.let { state.removeObserver(it) }
            messageObserver?.let { message.removeObserver(it) }
            if (messageTracker != null) {
                val remaining = messageTracker!!.remainingMessages()
                if (remaining > 0) {
                    Assert.fail("Not all messages in the message tracker were consumed. $remaining messages left")
                }
            }
        }

        /**
         * Await up to the specified time for latch to hit zero
         */
        fun awaitCompletion(
                timeout: Long = latchAwaitTimeSeconds,
                timeoutUnit: TimeUnit = latchAwaitTimeUnit
        ) {
            if (!latch.await(timeout, timeoutUnit)) {
                Assert.fail("Latch wait timeout")
            }
        }

        class Builder {
            /**
             * Will be set to null after building to prevent tampering
             */
            private val liveDataObserver = LiveDataObserver()
            private var isBuilt = false
            private var stateLatchSize = 0
            private var messageLatchSize = 0

            /**
             * Counts down the LiveDataObserver. Can only be called after being built
             */
            fun latchCountDownFunction(): () -> Unit = {
                check(isBuilt) { "Object not yet built" }
                liveDataObserver.latch.countDown()
            }

            fun setStateObserver(
                    observer: Observer<UpdateDefaultRounds.UpdateTaskState>,
                    expectedLatchCountdowns: Int
            ): Builder {
                check(!isBuilt) { "Object has already been built" }

                liveDataObserver.stateObserver = observer
                stateLatchSize = expectedLatchCountdowns
                return this
            }

            fun setStateObserver(observer: SimpleStateObserver): Builder {
                check(!isBuilt) { "Object has already been built" }

                liveDataObserver.stateObserver = observer.observer
                stateLatchSize = 1
                return this
            }

            fun setMessageObserver(observer: Observer<String?>, expectedLatchCountdowns: Int): Builder {
                check(!isBuilt) { "Object has already been built" }

                liveDataObserver.messageObserver = observer
                messageLatchSize = expectedLatchCountdowns
                return this
            }

            fun setMessageObserver(messageTracker: MessageTracker): Builder {
                check(!isBuilt) { "Object has already been built" }

                liveDataObserver.messageTracker = messageTracker
                messageLatchSize = messageTracker.remainingMessages()
                liveDataObserver.messageObserver = Observer { message ->
                    println(message)
                    messageTracker.checkMessage(message)
                }
                return this
            }

            fun build(): LiveDataObserver {
                isBuilt = true
                liveDataObserver.latch = CountDownLatch(stateLatchSize + messageLatchSize)
                return liveDataObserver
            }
        }

        class MessageTracker(
                private val expectedMessages: List<String?>,
                private val latchCountDown: () -> Unit,
                private val checkNulls: Boolean = false
        ) {
            private var currentMessageNumber = 0

            fun checkMessage(message: String?) {
                if (message != null || checkNulls) {
                    if (remainingMessages() == 0) {
                        Assert.fail("No more messages expected, but message was set to $message")
                    }
                    Assert.assertEquals(
                            "Message number $currentMessageNumber",
                            expectedMessages[currentMessageNumber++],
                            message
                    )
                    latchCountDown()
                }
            }

            fun remainingMessages(): Int {
                return expectedMessages.size - currentMessageNumber
            }
        }

        class SimpleStateObserver(
                latchCountDown: () -> Unit,
                desiredState: UpdateDefaultRounds.UpdateTaskState = UpdateDefaultRounds.UpdateTaskState.COMPLETE
        ) {
            val observer = Observer<UpdateDefaultRounds.UpdateTaskState> { state ->
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (state) {
                    desiredState -> latchCountDown()
                    UpdateDefaultRounds.UpdateTaskState.ERROR -> Assert.fail("Update error - unexpected state: $state")
                }
            }
        }
    }

    private class MockInfo private constructor(rawData: InputStream) {
        companion object {
            val defaultMap = mapOf(
                    Pair(R.string.about__update_default_rounds_initialising, "init"),
                    Pair(R.string.about__update_default_rounds_progress, "{current} of {total}"),
                    Pair(R.string.about__update_default_rounds_deleting, "deleting"),
                    Pair(R.string.general_cancelled, "cancelled"),
                    Pair(R.string.general_cancelling, "cancelling"),
                    Pair(R.string.general_complete, "complete"),
                    Pair(R.string.err_about__update_default_rounds_no_lock, "no lock"),
                    Pair(R.string.err__internal_error, "internal error"),
                    Pair(R.string.about__update_default_rounds_up_to_date, "up to date")
            )
        }

        val resourcesMock: Resources = mock(Resources::class.java)
        val db: ScoresRoomDatabase = mock(ScoresRoomDatabase::class.java)
        private val roundDao: RoundDao = mock(RoundDao::class.java)
        private val roundArrowCountDao: RoundArrowCountDao = mock(RoundArrowCountDao::class.java)
        private val roundSubTypeDao: RoundSubTypeDao = mock(RoundSubTypeDao::class.java)
        private val roundDistanceDao: RoundDistanceDao = mock(RoundDistanceDao::class.java)
        private var resourceMap = defaultMap
        private var allRounds = MutableLiveData<List<Round>>(listOf())
        private var allArrowCounts = MutableLiveData<List<RoundArrowCount>>(listOf())
        private var allSubTypes = MutableLiveData<List<RoundSubType>>(listOf())
        private var allDistances = MutableLiveData<List<RoundDistance>>(listOf())

        init {
            `when`(db.roundDao()).thenReturn(roundDao)
            `when`(db.roundArrowCountDao()).thenReturn(roundArrowCountDao)
            `when`(db.roundSubTypeDao()).thenReturn(roundSubTypeDao)
            `when`(db.roundDistanceDao()).thenReturn(roundDistanceDao)
            `when`(resourcesMock.openRawResource(anyInt())).thenReturn(rawData)
        }

        private fun initialise() {
            `when`(roundDao.getAllRounds()).thenReturn(allRounds)
            `when`(roundArrowCountDao.getAllArrowCounts()).thenReturn(allArrowCounts)
            `when`(roundSubTypeDao.getAllSubTypes()).thenReturn(allSubTypes)
            `when`(roundDistanceDao.getAllDistances()).thenReturn(allDistances)

            `when`(resourcesMock.getString(anyInt())).thenAnswer(object : Answer<String> {
                override fun answer(invocation: InvocationOnMock?): String {
                    val key = invocation!!.arguments[0]
                    if (resourceMap.containsKey(key)) {
                        return resourceMap[key]!!
                    }
                    if (defaultMap.containsKey(key)) {
                        return defaultMap[key]!!
                    }
                    throw NotImplementedError("Mock resource mapping not created")
                }
            })
        }

        /**
         * Verifies:
         * - dao mock objects received the correct number of calls of each type
         * - dao mock methods were called with the correct arguments
         * - order of calls for each dao/method independently (i.e. will check roundDao.insert was called with round X
         *   then round Y but not that roundDao.insert(X) was called before/after roundDistanceDao.insert(Z))
         */
        fun verifyUpdate(expectedUpdates: Map<Any, UpdateType>) {
            val dbObjects = listOf(
                    DbObjects(
                            Round::class, roundDao,
                            ArgumentCaptor.forClass(Round::class.java),
                            listOf(ArgumentCaptor.forClass(Int::class.java))
                    ),
                    DbObjects(
                            RoundArrowCount::class, roundArrowCountDao,
                            ArgumentCaptor.forClass(RoundArrowCount::class.java),
                            listOf(ArgumentCaptor.forClass(Int::class.java), ArgumentCaptor.forClass(Int::class.java))
                    ),
                    DbObjects(
                            RoundSubType::class, roundSubTypeDao,
                            ArgumentCaptor.forClass(RoundSubType::class.java),
                            listOf(ArgumentCaptor.forClass(Int::class.java), ArgumentCaptor.forClass(Int::class.java))
                    ),
                    DbObjects(
                            RoundDistance::class, roundDistanceDao,
                            ArgumentCaptor.forClass(RoundDistance::class.java),
                            listOf(
                                    ArgumentCaptor.forClass(Int::class.java),
                                    ArgumentCaptor.forClass(Int::class.java),
                                    ArgumentCaptor.forClass(Int::class.java)
                            )
                    )
            ).map {
                @Suppress("UNCHECKED_CAST")
                it.clazz to (it as DbObjects<Any>)
            }.toMap()

            /*
             * Set up captors and verify times called
             */
            for (dbObject in dbObjects.values) {
                runBlocking {
                    verify(
                            dbObject.dao,
                            times(expectedUpdates.count { it.key::class == dbObject.clazz && it.value == UpdateType.NEW })
                    ).insert(TestUtils.capture(dbObject.captor))
                    verify(
                            dbObject.dao,
                            times(expectedUpdates.count { it.key::class == dbObject.clazz && it.value == UpdateType.UPDATE })
                    ).updateSingle(TestUtils.capture(dbObject.captor))
                }
            }
            runBlocking {
                val expectedDeletes = expectedUpdates.filter { it.value == UpdateType.DELETE }

                val roundDbObject = dbObjects[Round::class]!!
                verify(roundDao, times(expectedDeletes.count { it.key::class == roundDbObject.clazz }))
                        .delete(TestUtils.capture(roundDbObject.deleteCaptors[0]))
                val arrowCountDbObject = dbObjects[RoundArrowCount::class]!!
                verify(roundArrowCountDao, times(expectedDeletes.count { it.key::class == arrowCountDbObject.clazz }))
                        .delete(
                                TestUtils.capture(arrowCountDbObject.deleteCaptors[0]),
                                TestUtils.capture(arrowCountDbObject.deleteCaptors[1])
                        )
                val subTypeDbObject = dbObjects[RoundSubType::class]!!
                verify(roundSubTypeDao, times(expectedDeletes.count { it.key::class == subTypeDbObject.clazz }))
                        .delete(
                                TestUtils.capture(subTypeDbObject.deleteCaptors[0]),
                                TestUtils.capture(subTypeDbObject.deleteCaptors[1])
                        )
                val distanceDbObject = dbObjects[RoundDistance::class]!!
                verify(roundDistanceDao, times(expectedDeletes.count { it.key::class == distanceDbObject.clazz }))
                        .delete(
                                TestUtils.capture(distanceDbObject.deleteCaptors[0]),
                                TestUtils.capture(distanceDbObject.deleteCaptors[1]),
                                TestUtils.capture(distanceDbObject.deleteCaptors[2])
                        )
            }

            /*
             * Check call arguments
             */
            for (expectedUpdate in expectedUpdates) {
                val expectedItem = expectedUpdate.key
                val dbInfo = dbObjects[expectedItem::class]!!

                // Delete Type
                if (expectedUpdate.value == UpdateType.DELETE) {
                    val checkList = when (expectedUpdate.key::class) {
                        Round::class -> listOf((expectedItem as Round).roundId)
                        RoundArrowCount::class -> {
                            val arrowCount = (expectedItem as RoundArrowCount)
                            listOf(arrowCount.roundId, arrowCount.distanceNumber)
                        }
                        RoundSubType::class -> {
                            val subType = (expectedItem as RoundSubType)
                            listOf(subType.roundId, subType.subTypeId)
                        }
                        RoundDistance::class -> {
                            val distance = (expectedItem as RoundDistance)
                            listOf(distance.roundId, distance.distanceNumber, distance.subTypeId)
                        }
                        else -> throw IllegalStateException("Invalid expected type")
                    }
                    Assert.assertEquals(checkList, dbInfo.removeNextDeleteCapturedValue())
                }
                // Other types
                else {
                    Assert.assertEquals(expectedItem, dbInfo.removeNextMainCaptorVal())
                }
            }
        }

        /**
         * Helper class for [MockInfo.verifyUpdate]
         */
        private class DbObjects<T : Any>(
                val clazz: KClass<T>,
                val dao: RoundTypeDao<T>,
                @Captor val captor: ArgumentCaptor<T>,
                val deleteCaptors: List<ArgumentCaptor<Int>>
        ) {
            /**
             * Lazy because captors haven't been used yet so captor.allValues will throw an error
             * Stores the captured items left to be checked (earliest captured items first)
             */
            private val mainCaptorValues by lazy {
                ArrayBlockingQueue<T>(captor.allValues.size, false, captor.allValues)
            }

            /**
             * @see mainCaptorValues
             */
            private val deleteCaptorValues by lazy {
                val allItems = deleteCaptors.map { it.allValues }
                val totalItems = allItems[0].size
                val field = ArrayBlockingQueue<List<Int>>(totalItems)
                for (i in allItems[0].indices) {
                    if (!field.offer(allItems.map { it[i] })) {
                        throw IllegalStateException("Queue failed to add")
                    }
                }
                field
            }

            fun removeNextDeleteCapturedValue(): List<Int> {
                return deleteCaptorValues.poll()!!
            }

            fun removeNextMainCaptorVal(): T {
                return mainCaptorValues.poll()!!
            }
        }

        class Builder(rawData: InputStream) {
            /**
             * Will be set to null after building to prevent tampering
             */
            private var mockInfo: MockInfo? = MockInfo(rawData)

            @Suppress("UNCHECKED_CAST")
            fun setDbData(data: List<Any>): Builder {
                val dataGrouped = data.groupBy { it::class }
                dataGrouped[Round::class]?.let { dbData -> setDbRoundsData(dbData as List<Round>) }
                dataGrouped[RoundArrowCount::class]?.let { dbData ->
                    setDbArrowCountsData(dbData as List<RoundArrowCount>)
                }
                dataGrouped[RoundSubType::class]?.let { dbData -> setDbSubTypeData(dbData as List<RoundSubType>) }
                dataGrouped[RoundDistance::class]?.let { dbData -> setDbDistanceData(dbData as List<RoundDistance>) }
                return this
            }

            fun setDbRoundsData(rounds: List<Round>): Builder {
                mockInfo!!.allRounds = MutableLiveData(rounds)
                return this
            }

            fun setDbArrowCountsData(arrowCounts: List<RoundArrowCount>): Builder {
                mockInfo!!.allArrowCounts = MutableLiveData(arrowCounts)
                return this
            }

            fun setDbSubTypeData(subTypes: List<RoundSubType>): Builder {
                mockInfo!!.allSubTypes = MutableLiveData(subTypes)
                return this
            }

            fun setDbDistanceData(distances: List<RoundDistance>): Builder {
                mockInfo!!.allDistances = MutableLiveData(distances)
                return this
            }

            fun build(): MockInfo {
                val mi = mockInfo!!
                mockInfo = null
                mi.initialise()
                return mi
            }
        }
    }

    private class TestData {
        companion object {
            const val START_JSON = """{"version": 1, "rounds": ["""
            const val END_JSON = """]}"""

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
        }
    }
}