package eywa.projectcodex.components.mainMenu

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.beust.klaxon.*
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.OnToken
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.TaskRunner
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.exceptions.UserException
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @see Round
 */
class DefaultRoundInfo(
        val displayName: String,
        private val isOutdoor: Boolean,
        private val isMetric: Boolean,
        private val fiveArrowEnd: Boolean,
        private val permittedFaces: List<String>,
        private val roundSubTypes: List<RoundInfoSubType>,
        private val roundArrowCounts: List<RoundInfoArrowCount>,
        private val roundDistances: List<RoundInfoDistance>
) {
    companion object {
        // TODO Store this in the JSON and read it in
        const val CURRENT_DEFAULT_ROUNDS_VERSION = 1
        internal const val defaultRoundMinimumId = 5
    }

    /**
     * Validation
     * TODO_CURRENT Make a builder omg
     */
    init {
        // Lengths
        require(roundArrowCounts.isNotEmpty()) { "Must have at least one arrowCount in $displayName" }
        require(roundDistances.isNotEmpty()) { "Must have at least one distance in $displayName" }
        val subTypeMultiplier = if (roundSubTypes.isNotEmpty()) roundSubTypes.size else 1
        require(subTypeMultiplier * roundArrowCounts.size == roundDistances.size) { "distance length incorrect in $displayName" }

        // Duplicate IDs
        require(roundSubTypes.size == roundSubTypes.distinctBy { it.id }.size) { "Duplicate subTypeId in $displayName" }
        require(roundArrowCounts.size == roundArrowCounts.distinctBy { it.distanceNumber }.size) { "Duplicate distanceNumber in $displayName" }

        // Check distances
        val subTypeList = if (roundSubTypes.isNotEmpty()) roundSubTypes
        else listOf(
                RoundInfoSubType(1, "", null, null)
        )
        for (subType in subTypeList) {
            val distances = roundDistances.filter { subTypeCount -> subTypeCount.roundSubTypeId == subType.id }
            require(distances.size == distances.distinctBy { it.distance }.size) { "Duplicate distance in $displayName for subType: ${subType.id}" }
            require(roundArrowCounts.map { it.distanceNumber }.toSet() == distances.map { it.distanceNumber }
                    .toSet()) { "Mismatched distanceNumbers in $displayName for subType: ${subType.id}" }
            require(distances.sortedByDescending { it.distance } == distances.sortedBy { it.distanceNumber }) { "Distances in $displayName are not non-ascending subType: ${subType.id}" }
        }

        // Names
        require(DefaultRoundInfoHelper.formatNameString(displayName) != "") { "Round name cannot be empty" }
        require(
                roundSubTypes.size
                        == roundSubTypes.distinctBy { DefaultRoundInfoHelper.formatNameString(it.subTypeName) }.size
        ) {
            "Duplicate sub type names in $displayName"
        }
        require(roundSubTypes.size <= 1 ||
                roundSubTypes.count { DefaultRoundInfoHelper.formatNameString(it.subTypeName) == "" } == 0) {
            "Illegal empty sub type name in $displayName"
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundSubType
     */
    class RoundInfoSubType(
            val id: Int,
            val subTypeName: String,
            val gentsUnder: Int?,
            val ladiesUnder: Int?
    ) {
        fun toRoundSubType(roundId: Int): RoundSubType {
            return RoundSubType(roundId, id, subTypeName, gentsUnder, ladiesUnder)
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundArrowCount
     */
    class RoundInfoArrowCount(
            val distanceNumber: Int,
            val faceSizeInCm: Double,
            val arrowCount: Int
    ) {
        fun toRoundArrowCount(roundId: Int): RoundArrowCount {
            return RoundArrowCount(roundId, distanceNumber, faceSizeInCm, arrowCount)
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundDistance
     */
    class RoundInfoDistance(
            val distanceNumber: Int,
            val roundSubTypeId: Int,
            val distance: Int
    ) {
        fun toRoundDistance(roundId: Int): RoundDistance {
            return RoundDistance(roundId, distanceNumber, roundSubTypeId, distance)
        }
    }

    fun getRound(roundId: Int = 0): Round {
        return Round(
                roundId,
                DefaultRoundInfoHelper.formatNameString(displayName),
                displayName,
                isOutdoor,
                isMetric,
                permittedFaces,
                true,
                fiveArrowEnd
        )
    }

    fun getRoundSubTypes(roundId: Int): List<RoundSubType> {
        return roundSubTypes.map { it.toRoundSubType(roundId) }
    }

    fun getRoundArrowCounts(roundId: Int): List<RoundArrowCount> {
        return roundArrowCounts.map { it.toRoundArrowCount(roundId) }
    }

    fun getRoundDistances(roundId: Int): List<RoundDistance> {
        return roundDistances.map { it.toRoundDistance(roundId) }
    }
}

// TODO Make this private
class DefaultRoundInfoHelper {
    companion object {
        /**
         * Removes non-alphanumerics and spaces and converts to lower case
         */
        fun formatNameString(string: String): String {
            // TODO Locale
            return string.replace(Regex("[^A-Za-z0-9]| "), "").toLowerCase()
        }

        /**
         * Compares [readData] to [dbData] based on [equalityMeasures]
         * - Marks any items in [dbData] not in [readData] for deletion
         * - Marks any items in [readData] not in [dbData] as new
         * - Marks any items in [readData] and [dbData] with matching [equalityMeasures] for update if the items
         *   themselves are not equal
         * @return the marked items as a map of item to the type of update required
         */
        private fun <T> getUpdateItems(
                readData: Set<T>,
                dbData: Set<T>,
                equalityMeasures: (T) -> List<Int>
        ): MutableMap<Any, UpdateType> {
            val returnMap = mutableMapOf<Any, UpdateType>()
            if (dbData != readData) {
                /*
                 * Delete any items that are in the DB but not the read data
                 */
                for (dbItem in dbData) {
                    if (readData.find { equalityMeasures(it) == equalityMeasures(dbItem) } == null) {
                        returnMap[dbItem as Any] = UpdateType.DELETE
                    }
                }

                /*
                 * Check each read item
                 */
                for (readItem in readData) {
                    val dbItem = dbData.find { equalityMeasures(it) == equalityMeasures(readItem) }

                    if (dbItem == null) {
                        returnMap[readItem as Any] = UpdateType.NEW
                    }
                    else if (dbItem != readItem) {
                        returnMap[readItem as Any] = UpdateType.UPDATE
                    }
                    // else remain unchanged
                }
            }
            return returnMap
        }

        /**
         * TODO_THREAD_UNSAFE Can cause clashes if multiple things are accessing the database
         * @param nextRoundId the roundId to use if a new round is created.
         * This is used instead of [allDbRounds].max() + 1 because [allDbRounds] is not updated as new rounds are added
         */
        fun getUpdates(
                readRoundInfo: DefaultRoundInfo,
                allDbRounds: List<Round>,
                allDbArrowCounts: List<RoundArrowCount>,
                allDbSubTypes: List<RoundSubType>,
                allDbDistances: List<RoundDistance>,
                nextRoundId: Int
        ): Map<Any, UpdateType> {
            // Maps a db data item to an update type
            val dbUpdateItems = mutableMapOf<Any, UpdateType>()
            val dbRoundData =
                    allDbRounds.find { it.name == formatNameString(readRoundInfo.displayName) }
            if (dbRoundData != null) {
                /*
                 * Check round info
                 */
                val roundId = dbRoundData.roundId
                val defaultRound = readRoundInfo.getRound(roundId)

                // Main data
                if (defaultRound != dbRoundData) {
                    dbUpdateItems[defaultRound] = UpdateType.UPDATE
                }

                // Arrow counts
                dbUpdateItems.putAll(getUpdateItems(
                        readRoundInfo.getRoundArrowCounts(roundId).toSet(),
                        allDbArrowCounts.filter { it.roundId == roundId }.toSet()
                ) { listOf(it.distanceNumber) })

                // Sub types
                dbUpdateItems.putAll(getUpdateItems(
                        readRoundInfo.getRoundSubTypes(roundId).toSet(),
                        allDbSubTypes.filter { it.roundId == roundId }.toSet()
                ) { listOf(it.subTypeId) })

                // Distances
                dbUpdateItems.putAll(getUpdateItems(
                        readRoundInfo.getRoundDistances(roundId).toSet(),
                        allDbDistances.filter { it.roundId == roundId }.toSet()
                ) { listOf(it.distanceNumber, it.subTypeId) })
            }
            else {
                /*
                 * Create new round
                 */
                dbUpdateItems[readRoundInfo.getRound(nextRoundId)] = UpdateType.NEW
                dbUpdateItems.putAll(readRoundInfo.getRoundArrowCounts(nextRoundId).map { it to UpdateType.NEW })
                dbUpdateItems.putAll(readRoundInfo.getRoundSubTypes(nextRoundId).map { it to UpdateType.NEW })
                dbUpdateItems.putAll(readRoundInfo.getRoundDistances(nextRoundId).map { it to UpdateType.NEW })
            }

            // Sanity check to make sure no defaultRoundInfo snuck in
            check(dbUpdateItems.filterKeys { !(it::class.isData) }.isEmpty()) {
                "Default rounds checker returned an invalid type"
            }
            return dbUpdateItems
        }
    }
}

/**
 * This task will update the default rounds in the repository
 */
class UpdateDefaultRounds {
    companion object {
        private const val LOG_TAG = "UpdateDefaultRounds"
        private val taskExecutor = TaskRunner()
        private var currentTask: TaskRunner.ProgressTask<String, String>? = null
        val taskProgress = TaskProgress()

        /**
         * Begins an [UpdateDefaultRoundsTask] if one isn't already in progress
         */
        fun runUpdate(db: ScoresRoomDatabase, resources: Resources, sharedPreferences: SharedPreferences) {
            synchronized(taskProgress) {
                if (taskProgress.getState().value == UpdateTaskState.IN_PROGRESS) return
                taskProgress.update(
                        UpdateTaskState.IN_PROGRESS,
                        resources.getString(R.string.main_menu__update_default_rounds_initialising)
                )
            }
            currentTask = UpdateDefaultRoundsTask(RoundRepo(db), resources, sharedPreferences)
            taskExecutor.executeProgressTask(
                    currentTask!!,
                    onProgress = { progress ->
                        if (taskProgress.getState().value == UpdateTaskState.CANCELLING) {
                            CustomLogger.customLogger.i(LOG_TAG, "Ignored message while cancelling: $progress")
                        }
                        else {
                            taskProgress.update(newMessage = progress)
                        }
                    },
                    onComplete = { message ->
                        currentTask = null
                        taskProgress.update(UpdateTaskState.COMPLETE, message)
                    },
                    onError = { exception ->
                        CustomLogger.customLogger.e(
                                LOG_TAG,
                                "Update default rounds task failed with exception: " + exception.toString()
                                        + "\nlast progress token was " + taskProgress.getMessage().value
                        )
                        val message = when (exception) {
                            is UserException -> exception.getUserMessage(resources)
                            else -> resources.getString(R.string.err__internal_error)
                        }
                        currentTask = null
                        taskProgress.update(UpdateTaskState.ERROR, message)
                    }
            )
        }

        fun cancelUpdateDefaultRounds(resources: Resources) {
            synchronized(taskProgress) {
                taskProgress.update(UpdateTaskState.CANCELLING, resources.getString(R.string.general_cancelling))
                currentTask?.isSoftCancelled = true
            }
        }

        /**
         * If the current task is complete, reset the state to not started
         */
        fun resetState() {
            synchronized(taskProgress) {
                val currentState = taskProgress.getState().value
                if (currentState == UpdateTaskState.COMPLETE) {
                    taskProgress.update(UpdateTaskState.UP_TO_DATE)
                }
                else if (currentState == UpdateTaskState.ERROR) {
                    taskProgress.update(UpdateTaskState.NOT_STARTED)
                }
            }
        }

        /**
         * Force the state value back to not started with a null message
         */
        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun hardResetState() {
            synchronized(taskProgress) {
                taskProgress.update(UpdateTaskState.NOT_STARTED)
            }
        }
    }

    enum class UpdateTaskState { NOT_STARTED, IN_PROGRESS, CANCELLING, COMPLETE, ERROR, UP_TO_DATE }

    /**
     * Used to enforce ordering when updating the state and message
     */
    class TaskProgress {
        private val state = MutableLiveData<UpdateTaskState>(UpdateTaskState.NOT_STARTED)
        private val message = MutableLiveData<String?>(null)

        fun getState(): LiveData<UpdateTaskState> {
            return state
        }

        fun getMessage(): LiveData<String?> {
            return message
        }

        /**
         * Posts the [newState] if one is provided, then always posts the [newMessage]
         */
        internal fun update(newState: UpdateTaskState? = null, newMessage: String? = null) {
            if (newState != null) {
                state.postValue(newState)
            }
            message.postValue(newMessage)
        }
    }

    private class UpdateDefaultRoundsTask(
            private val repository: RoundRepo,
            private val resources: Resources,
            private val sharedPreferences: SharedPreferences
    ) :
            TaskRunner.ProgressTask<String, String>() {
        companion object {
            const val LOG_TAG = "UpdateDefaultRoundsTask"
        }

        override fun runTask(progressToken: OnToken<String>): String {
            progressToken(resources.getString(R.string.main_menu__update_default_rounds_initialising))

            /*
             * Check if an update is needed
             * TODO Read from the json file rather than using CURRENT_VERSION constant
             */
            val currentVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
            if (currentVersion >= DefaultRoundInfo.CURRENT_DEFAULT_ROUNDS_VERSION) {
                return resources.getString(R.string.main_menu__update_default_rounds_up_to_date)
            }

            /*
             * Make sure we can acquire the lock before processing
             *   (holds the lock for longer but doesn't waste time if the db isn't free)
             */
            val acquiredLock = RoundRepo.repositoryWriteLock.tryLock(1, TimeUnit.SECONDS)
            if (!acquiredLock) {
                throw UserException(R.string.err_main_menu__update_default_rounds_no_lock)
            }

            try {
                /*
                 * Get db info
                 */
                val latch = CountDownLatch(4) // Rounds, ArrowCounts, SubTypes, Distances
                var dbRounds: List<Round>? = null
                val dbRoundsObserver = Observer<List<Round>> {
                    dbRounds = it
                    latch.countDown()
                }
                var dbArrowCounts: List<RoundArrowCount>? = null
                val dbArrowCountsObserver = Observer<List<RoundArrowCount>> {
                    dbArrowCounts = it
                    latch.countDown()
                }
                var dbSubTypes: List<RoundSubType>? = null
                val dbSubTypesObserver = Observer<List<RoundSubType>> {
                    dbSubTypes = it
                    latch.countDown()
                }
                var dbDistances: List<RoundDistance>? = null
                val dbDistancesObserver = Observer<List<RoundDistance>> {
                    dbDistances = it
                    latch.countDown()
                }
                Handler(Looper.getMainLooper()).post {
                    repository.rounds.observeForever(dbRoundsObserver)
                    repository.roundArrowCounts.observeForever(dbArrowCountsObserver)
                    repository.roundSubTypes.observeForever(dbSubTypesObserver)
                    repository.roundDistances.observeForever(dbDistancesObserver)
                }
                var nextRoundId: Int? = null
                val dbInfoRetrieved by lazy {
                    check(latch.await(10, TimeUnit.SECONDS)) { "Failed to retrieve db information" }
                    nextRoundId = dbRounds!!.map { it.roundId }.max()?.plus(1) ?: DefaultRoundInfo.defaultRoundMinimumId
                    Handler(Looper.getMainLooper()).post {
                        repository.rounds.removeObserver(dbRoundsObserver)
                        repository.roundArrowCounts.removeObserver(dbArrowCountsObserver)
                        repository.roundSubTypes.removeObserver(dbSubTypesObserver)
                        repository.roundDistances.removeObserver(dbDistancesObserver)
                    }
                    true
                }

                /*
                 * Read default rounds data from file and make a list of strings
                 */
                val klaxon = Klaxon().converter(RoundsList.RoundsListJsonConverter())
                val rawString =
                        resources.openRawResource(R.raw.default_rounds_data).bufferedReader().use { it.readText() }
                val readRoundsStrings = klaxon.parse<RoundsList>(rawString)?.rounds
                        ?: throw IllegalStateException("Failed to parse default rounds file")
                val readRoundNames = mutableListOf<String>()

                /*
                 * Check each read rounds
                 */
                klaxon.converter(DefaultRoundInfoJsonConverter())
                val progressTokenRawString = resources.getString(R.string.main_menu__update_default_rounds_progress)
                val progressTokenTotalReplacer = Pair("total", readRoundsStrings.size.toString())
                for (readRound in readRoundsStrings.withIndex()) {
                    progressToken(
                            resourceStringReplace(
                                    progressTokenRawString,
                                    mapOf(progressTokenTotalReplacer, Pair("current", (readRound.index + 1).toString()))
                            )
                    )
                    val readRoundInfo = klaxon.parse<DefaultRoundInfo>(readRound.value)
                            ?: throw IllegalStateException("Failed to parse default round info. Index ${readRound.index}")

                    /*
                     * Check name
                     */
                    val readRoundName = DefaultRoundInfoHelper.formatNameString(readRoundInfo.displayName)
                    require(!readRoundNames.contains(readRoundName)) { "Duplicate name in default rounds file" }
                    readRoundNames.add(readRoundName)

                    /*
                     * Compare and update db
                     */
                    check(dbInfoRetrieved) { "Failed to retrieve db information" }
                    // Should not be null as empty database will return an empty list
                    val dbUpdateItems = DefaultRoundInfoHelper.getUpdates(
                            readRoundInfo, dbRounds!!, dbArrowCounts!!, dbSubTypes!!, dbDistances!!, nextRoundId!!
                    )
                    if (dbUpdateItems.containsValue(UpdateType.NEW)) {
                        nextRoundId = nextRoundId!! + 1
                    }
                    runBlocking {
                        repository.updateRounds(dbUpdateItems)
                    }

                    if (isSoftCancelled) {
                        CustomLogger.customLogger.i(
                                LOG_TAG,
                                "Task cancelled at ${readRound.index} of ${readRoundNames.size}"
                        )
                        return resources.getString(R.string.general_cancelled)
                    }
                }

                /*
                 * Remove rounds and related objects from the database that are not in readRounds
                 */
                progressToken(resources.getString(R.string.main_menu__update_default_rounds_deleting))
                val roundsToDelete =
                        repository.rounds.value!!.filter { dbRound -> !readRoundNames.contains(dbRound.name) }

                val delItems = roundsToDelete.map { it as Any }.toMutableList()
                val idsOfRoundsToDelete = roundsToDelete.map { it.roundId }
                delItems.addAll(repository.roundArrowCounts.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })
                delItems.addAll(repository.roundSubTypes.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })
                delItems.addAll(repository.roundDistances.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })

                val deleteItems = delItems.map { it to UpdateType.DELETE }.toMap()
                runBlocking {
                    repository.updateRounds(deleteItems)
                }

                /*
                 * Update the version so that we only update if necessary
                 */
                val editor = sharedPreferences.edit()
                editor.putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, DefaultRoundInfo.CURRENT_DEFAULT_ROUNDS_VERSION)
                editor.apply()
                return resources.getString(R.string.general_complete)
            }
            finally {
                RoundRepo.repositoryWriteLock.unlock()
            }
        }
    }
}

private class RoundsList(val rounds: List<String>) {
    /**
     * Splits a json string in the format {"rounds": [...]} into a list of strings
     */
    class RoundsListJsonConverter : Converter {
        override fun canConvert(cls: Class<*>): Boolean {
            return RoundsList::class.java.isAssignableFrom(cls)
        }

        override fun fromJson(jv: JsonValue): Any {
            val jsonObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")
            val jsonRoundObjects = jsonObject["rounds"] as JsonArray<JsonObject>
            return RoundsList(jsonRoundObjects.toList().map { it.toJsonString() })
        }

        /**
         * Not currently used
         */
        override fun toJson(value: Any): String {
            throw NotImplementedError()
        }
    }
}

/**
 * Converts a json string into a [DefaultRoundInfo] object
 */
class DefaultRoundInfoJsonConverter : Converter {
    companion object {
        private const val CONVERTER_LOG_TAG = "CustomJsonConverter"
    }

    override fun canConvert(cls: Class<*>): Boolean {
        return DefaultRoundInfo::class.java.isAssignableFrom(cls)
    }

    override fun fromJson(jv: JsonValue): Any {
        val klaxon = Klaxon()
        val jsonRoundObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")

        val roundName = parseObject<String>(jsonRoundObject, "roundName")
        CustomLogger.customLogger.i(CONVERTER_LOG_TAG, roundName)
        val isOutdoor = parseObject<Boolean>(jsonRoundObject, "outdoor")
        val isMetric = parseObject<Boolean>(jsonRoundObject, "isMetric")
        val fiveArrowEnd = parseObject<Boolean>(jsonRoundObject, "fiveArrowEnd")

        val permittedFaces = (jsonRoundObject["permittedFaces"] as JsonArray<String>).value

        val roundLengthsJson = jsonRoundObject["roundSubTypes"] as JsonArray<String>
        val roundLengths = mutableListOf<DefaultRoundInfo.RoundInfoSubType>()
        for (roundLength in (roundLengthsJson.value as ArrayList<JsonObject>)) {
            roundLength["id"] = roundLength["roundSubTypeId"]
            roundLength.remove("roundSubTypeId")
            val parsed = klaxon.parse<DefaultRoundInfo.RoundInfoSubType>(roundLength.toJsonString())
            if (parsed != null) {
                roundLengths.add(parsed)
            }
        }

        val roundProgressionJson = jsonRoundObject["roundArrowCounts"] as JsonArray<JsonObject>
        val roundProgressions = mutableListOf<DefaultRoundInfo.RoundInfoArrowCount>()
        for (roundProgression in (roundProgressionJson.value as ArrayList<JsonObject>)) {
            val parsed = klaxon.parse<DefaultRoundInfo.RoundInfoArrowCount>(roundProgression.toJsonString())
            if (parsed != null) {
                roundProgressions.add(parsed)
            }
        }

        val roundDistancesJson = jsonRoundObject["roundDistances"] as JsonArray<JsonObject>
        val roundDistances = mutableListOf<DefaultRoundInfo.RoundInfoDistance>()
        for (roundDistance in (roundDistancesJson.value as ArrayList<JsonObject>)) {
            if (roundDistance["roundSubTypeId"] == null) {
                roundDistance["roundSubTypeId"] = 1
            }
            val parsed = klaxon.parse<DefaultRoundInfo.RoundInfoDistance>(roundDistance.toJsonString())
            if (parsed != null) {
                roundDistances.add(parsed)
            }
        }

        return DefaultRoundInfo(
                roundName, isOutdoor, isMetric, fiveArrowEnd, permittedFaces,
                roundLengths, roundProgressions, roundDistances
        )
    }

    /**
     * @return the value the specified key maps to in the jsonObject
     */
    private inline fun <reified T> parseObject(jsonObject: JsonObject, key: String): T {
        val retrievedObject = jsonObject[key]
                ?: throw KlaxonException("Cannot parse $key from: $jsonObject")
        if (retrievedObject !is T) {
            throw ClassCastException("$key is not of type ${T::class.java.name}")
        }
        return retrievedObject
    }

    /**
     * Not currently used
     */
    override fun toJson(value: Any): String {
        throw NotImplementedError()
    }
}