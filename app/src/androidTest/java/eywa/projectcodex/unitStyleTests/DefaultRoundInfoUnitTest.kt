package eywa.projectcodex.unitStyleTests

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import eywa.projectcodex.R
import eywa.projectcodex.TestUtils
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.daos.*
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.logic.UpdateDefaultRounds
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
    private class TestData {
        companion object {
            const val START_JSON = """{"rounds": ["""
            const val END_JSON = """]}"""
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
            val YORK_ROUND_OBJECTS = listOf(
                    Round(
                            5,
                            "york",
                            "York",
                            true,
                            false, listOf(),
                            true,
                            false
                    ),
                    RoundSubType(5, 1, "York"),
                    RoundSubType(5, 2, "Hereford (Bristol I)", 18),
                    RoundSubType(5, 3, "Bristol II", 16, 18),
                    RoundSubType(5, 4, "Bristol V", 0, 12),
                    RoundArrowCount(5, 1, 122.0, 72),
                    RoundArrowCount(5, 2, 122.0, 48),
                    RoundDistance(5, 1, 1, 100),
                    RoundDistance(5, 2, 1, 80),
                    RoundDistance(5, 1, 2, 80),
                    RoundDistance(5, 2, 2, 60),
                    RoundDistance(5, 1, 3, 60),
                    RoundDistance(5, 2, 3, 50),
                    RoundDistance(5, 1, 4, 30),
                    RoundDistance(5, 2, 4, 20)
            )
            val ST_GEORGE_ROUND_OBJECTS = listOf(
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
     * Testing json for two correct rounds results in the correct database calls
     */
    @Test
    fun testNewRounds() {
        val json = """
            ${TestData.START_JSON}
                {
                    ${TestData.YORK_MAIN_JSON},
                    ${TestData.YORK_SUB_TYPES_JSON},
                    ${TestData.YORK_ARROW_COUNTS_JSON},
                    ${TestData.YORK_DISTANCES_JSON}
                },
                ${TestData.ST_GEORGE_JSON}
            ${TestData.END_JSON}
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

        mockInfo.verifyUpdate(
                TestData.YORK_ROUND_OBJECTS.plus(TestData.ST_GEORGE_ROUND_OBJECTS)
                        .map { it to UpdateType.NEW }.toMap()
        )
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

        val db: ScoresRoomDatabase = mock(ScoresRoomDatabase::class.java)
        private val roundDao: RoundDao = mock(RoundDao::class.java)
        private val roundArrowCountDao: RoundArrowCountDao = mock(RoundArrowCountDao::class.java)
        private val roundSubTypeDao: RoundSubTypeDao = mock(RoundSubTypeDao::class.java)
        private val roundDistanceDao: RoundDistanceDao = mock(RoundDistanceDao::class.java)
        val resourcesMock: Resources = mock(Resources::class.java)

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
    }
}