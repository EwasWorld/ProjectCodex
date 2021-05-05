package eywa.projectcodex.unitStyleTests

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import eywa.projectcodex.R
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.daos.RoundArrowCountDao
import eywa.projectcodex.database.daos.RoundDao
import eywa.projectcodex.database.daos.RoundDistanceDao
import eywa.projectcodex.database.daos.RoundSubTypeDao
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.logic.UpdateDefaultRounds
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import java.io.InputStream
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@RunWith(AndroidJUnit4::class)
class DefaultRoundInfoUnitTest {
    private class TestData {
        companion object {
            const val START = """{"rounds": ["""
            const val END = """]}"""
            const val YORK_MAIN = """
              "roundName": "York",
              "outdoor": true,
              "isMetric": false,
              "fiveArrowEnd": false,
              "permittedFaces": []
            """
            const val YORK_SUB_TYPES = """
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
            const val YORK_ARROW_COUNTS = """
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
            const val YORK_DISTANCES = """
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
            const val ST_GEORGE = """
            {
              "roundName": "St. George",
              "outdoor": true,
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
        }
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @After
    fun teardown() {
        UpdateDefaultRounds.hardResetState()
    }

    /**
     * Check that the current default round data is parsable
     * Log the time taken to complete
     */
    @Test
    fun testCurrentDefaultRoundInfoFile() {
        // TODO Swap back to unit test and use file directly
        // val mockInfo = MockInfo(FileInputStream("src/main/res/raw/default_rounds_data.json"))
        val mockInfo = MockInfo(getInstrumentation().targetContext.resources.openRawResource(R.raw.default_rounds_data))

        /*
         * Observe state
         */
        val simpleStateObserver = LiveDataObserver.SimpleStateObserver()
        var currentIndex: Int? = null
        var timeCurrentStarted: Date? = null
        val itemCompletionTimes = mutableListOf<Long>()
        val observer = LiveDataObserver.Builder()
                .setStateObserver(simpleStateObserver.observer)
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
                })
                .build()
        observer.startObserving()

        /*
         * Run test
         */
        val startTime = Date()
        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock)

        // Wait for the async task to finish
        if (!simpleStateObserver.updateLatch.await(5, TimeUnit.MINUTES)) {
            Assert.fail("Latch wait timeout")
        }
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
     * Testing json for two correct rounds produces correct database objects
     */
    @Test
    fun testCorrectRounds() {
        val json = """
            ${TestData.START}
                {
                    ${TestData.YORK_MAIN},
                    ${TestData.YORK_SUB_TYPES},
                    ${TestData.YORK_ARROW_COUNTS},
                    ${TestData.YORK_DISTANCES}
                },
                ${TestData.ST_GEORGE}
            ${TestData.END}
        """
        val mockInfo = MockInfo(json.byteInputStream())

        val simpleStateObserver = LiveDataObserver.SimpleStateObserver()
        val observer = LiveDataObserver.Builder()
                .setStateObserver(simpleStateObserver.observer)
                .setMessageObserver(
                        LiveDataObserver.MessageTracker(
                                listOf(
                                        MockInfo.defaultMap[R.string.main_menu__update_default_rounds_initialising],
                                        MockInfo.defaultMap[R.string.main_menu__update_default_rounds_initialising],
                                        "1 of 2",
                                        "2 of 2",
                                        MockInfo.defaultMap[R.string.main_menu__update_default_rounds_deleting],
                                        MockInfo.defaultMap[R.string.button_complete]
                                )
                        )
                ).build()

        observer.startObserving()

        UpdateDefaultRounds.runUpdate(mockInfo.db, mockInfo.resourcesMock)
        if (!simpleStateObserver.updateLatch.await(10, TimeUnit.SECONDS)) {
            Assert.fail("Latch wait timeout")
        }
        observer.finishObserving()

        // TODO Check output
    }

    class LiveDataObserver private constructor() {
        private var stateObserver: Observer<UpdateDefaultRounds.UpdateTaskState>? = null
        private var messageObserver: Observer<String?>? = null
        private val state = UpdateDefaultRounds.getState()
        private val message = UpdateDefaultRounds.getProgressMessage()

        fun startObserving() {
            stateObserver?.let { state.observeForever(it) }
            messageObserver?.let { message.observeForever(it) }
        }

        fun finishObserving() {
            stateObserver?.let { state.removeObserver(it) }
            messageObserver?.let { message.removeObserver(it) }
        }

        class Builder {
            private val liveDataObserver = LiveDataObserver()

            fun setStateObserver(observer: Observer<UpdateDefaultRounds.UpdateTaskState>): Builder {
                liveDataObserver.stateObserver = observer
                return this
            }

            fun setMessageObserver(observer: Observer<String?>): Builder {
                liveDataObserver.messageObserver = observer
                return this
            }

            fun setMessageObserver(messageTracker: MessageTracker): Builder {
                liveDataObserver.messageObserver = Observer { message ->
                    println(message)
                    messageTracker.checkMessage(message)
                }
                return this
            }

            fun build(): LiveDataObserver {
                return liveDataObserver
            }
        }

        class MessageTracker(private val expectedMessages: List<String?>, private val checkNulls: Boolean = false) {
            private var currentMessageNumber = 0

            fun checkMessage(message: String?) {
                if (message != null || checkNulls) {
                    Assert.assertEquals(expectedMessages[currentMessageNumber++], message)
                }
            }
        }

        class SimpleStateObserver {
            val updateLatch = CountDownLatch(1)
            val observer = Observer<UpdateDefaultRounds.UpdateTaskState> { state ->
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (state) {
                    UpdateDefaultRounds.UpdateTaskState.COMPLETE -> updateLatch.countDown()
                    UpdateDefaultRounds.UpdateTaskState.ERROR -> Assert.fail("Update error")
                }
            }
        }
    }

    class MockInfo(rawData: InputStream, resourceMap: Map<Int, String> = mapOf()) {
        companion object {
            val defaultMap = mapOf(
                    Pair(R.string.main_menu__update_default_rounds_initialising, "init"),
                    Pair(R.string.main_menu__update_default_rounds_progress_label, "updating"),
                    Pair(R.string.main_menu__update_default_rounds_progress, "{current} of {total}"),
                    Pair(R.string.main_menu__update_default_rounds_deleting, "deleting"),
                    Pair(R.string.general_cancelled, "cancelled"),
                    Pair(R.string.button_complete, "complete"),
                    Pair(R.string.err__internal_error, "internal error")
            )
        }

        val db: ScoresRoomDatabase = Mockito.mock(ScoresRoomDatabase::class.java)
        val roundDao: RoundDao = Mockito.mock(RoundDao::class.java)
        val roundArrowCountDao: RoundArrowCountDao = Mockito.mock(RoundArrowCountDao::class.java)
        val roundSubTypeDao: RoundSubTypeDao = Mockito.mock(RoundSubTypeDao::class.java)
        val roundDistanceDao: RoundDistanceDao = Mockito.mock(RoundDistanceDao::class.java)
        val resourcesMock: Resources = Mockito.mock(Resources::class.java)

        init {
            check(resourceMap.values.union(defaultMap.values).size == resourceMap.size + defaultMap.size) {
                "Resource map values contain duplicates"
            }
            `when`(db.roundDao()).thenReturn(roundDao)
            `when`(db.roundArrowCountDao()).thenReturn(roundArrowCountDao)
            `when`(db.roundSubTypeDao()).thenReturn(roundSubTypeDao)
            `when`(db.roundDistanceDao()).thenReturn(roundDistanceDao)
            `when`(db.roundDistanceDao()).thenReturn(roundDistanceDao)

            `when`(roundDao.getAllRounds()).thenReturn(MutableLiveData<List<Round>>(listOf()))
            `when`(roundArrowCountDao.getAllArrowCounts()).thenReturn(MutableLiveData<List<RoundArrowCount>>(listOf()))
            `when`(roundSubTypeDao.getAllSubTypes()).thenReturn(MutableLiveData<List<RoundSubType>>(listOf()))
            `when`(roundDistanceDao.getAllDistances()).thenReturn(MutableLiveData<List<RoundDistance>>(listOf()))

            `when`(resourcesMock.openRawResource(anyInt())).thenReturn(rawData)
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
    }
}