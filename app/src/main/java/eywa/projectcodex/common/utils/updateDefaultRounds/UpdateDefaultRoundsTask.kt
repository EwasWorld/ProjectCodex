package eywa.projectcodex.common.utils.updateDefaultRounds

import android.content.SharedPreferences
import android.content.res.Resources
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import eywa.projectcodex.R
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.SharedPrefs
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.*
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.CompletionType.ALREADY_UP_TO_DATE
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState.CompletionType.COMPLETE
import eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects.DefaultRoundInfo
import eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects.DefaultRoundInfoJsonConverter
import eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects.RoundsList
import eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects.RoundsVersion
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

// TODO Make this private
class DefaultRoundInfoHelper {
    companion object {
        /**
         * Removes non-alphanumerics and spaces and converts to lower case
         */
        fun formatToDbName(roundDisplayName: String): String {
            return roundDisplayName.replace(Regex("[^A-Za-z0-9]| "), "").lowercase(Locale.getDefault())
        }

    }
}

/**
 * This task will update the default rounds in the repository
 */
open class UpdateDefaultRoundsTask(
        private val repository: RoundRepo,
        private val resources: Resources,
        private val sharedPreferences: SharedPreferences,
        private val logger: CustomLogger,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val _state: MutableStateFlow<UpdateDefaultRoundsState?> = MutableStateFlow(null)
    open val state: StateFlow<UpdateDefaultRoundsState?> = _state

    @Suppress("BlockingMethodInNonBlockingContext")
    open suspend fun runTask() = withContext(dispatcher) {
        if (_state.value != null) {
            return@withContext false
        }
        logger.d(LOG_TAG, "runTask")
        setState(Initialising)

        // TODO Check whether app version has changed

        /*
         * Check if an update is needed
         */
        val currentVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1).takeIf { it != -1 }
        val rawString =
                resources.openRawResource(R.raw.default_rounds_data).bufferedReader().use { it.readText() }
        val klaxon = Klaxon().converter(RoundsVersion.RoundsVersionJsonConverter())
        val dataVersion: Int?
        try {
            dataVersion = klaxon.parse<RoundsVersion>(rawString)?.version
        }
        catch (e: KlaxonException) {
            setState(InternalError(currentVersion, "Failed to parse default rounds file"))
            return@withContext true
        }
        if (dataVersion == null) {
            setState(InternalError(currentVersion, "Nothing returned from JSON parse of file"))
            return@withContext true
        }
        if ((currentVersion ?: -1) >= dataVersion) {
            setState(Complete(currentVersion, ALREADY_UP_TO_DATE))
            return@withContext true
        }

        /*
         * TODO Is a lock required?
         * Make sure we can acquire the lock before processing
         *   (holds the lock for longer but doesn't waste time if the db isn't free)
         */
//        val acquiredLock = RoundRepo.repositoryWriteLock.tryLock(1, TimeUnit.SECONDS)
//        if (!acquiredLock) {
//            setState(TemporaryError(currentVersion, ErrorType.CANT_ACQUIRE_LOCK))
//            return@withContext true
//        }

        try {
            /*
             * Get db info
             */
            val dbRoundsInfo = repository.fullRoundsInfo.first()

            /*
             * Read default rounds data from file and make a list of strings
             */
            klaxon.converter(RoundsList.RoundsListJsonConverter())
            val fileRoundStrings: List<String>? = klaxon.parse<RoundsList>(rawString)?.rounds
            if (fileRoundStrings == null) {
                setState(InternalError(currentVersion, "Nothing returned from JSON parse of round strings"))
                return@withContext true
            }

            var nextRoundId =
                    dbRoundsInfo.maxOfOrNull { it.round.roundId }?.plus(1) ?: DefaultRoundInfo.defaultRoundMinimumId
            val fileRoundNames = mutableSetOf<String>()

            /*
             * Check each read rounds
             */
            klaxon.converter(DefaultRoundInfoJsonConverter(logger))
            val progressTokenTotalItems = fileRoundStrings.size
            for (readRound in fileRoundStrings.withIndex()) {
                setState(StartProcessingNew(currentVersion, readRound.index + 1, progressTokenTotalItems))
                val readRoundInfo: DefaultRoundInfo?
                try {
                    readRoundInfo = klaxon.parse<DefaultRoundInfo>(readRound.value)
                }
                catch (e: java.lang.Exception) {
                    if (!(e is KlaxonException || e is IllegalArgumentException)) throw e
                    val klaxonMessage = if (e is KlaxonException) e.message ?: "KlaxonException" else null
                    setState(
                            InternalError(
                                    currentVersion,
                                    "Failed to create rounds object at index ${readRound.index}" +
                                            (klaxonMessage?.let { ". $it" } ?: ""),
                            )
                    )
                    return@withContext true
                }
                if (readRoundInfo == null) {
                    // TODO Rollback all updates?
                    setState(
                            InternalError(
                                    currentVersion,
                                    "Nothing returned from JSON parse of round at index ${readRound.index}",
                            )
                    )
                    return@withContext true
                }

                /*
                 * Check name
                 */
                val fileRoundName = DefaultRoundInfoHelper.formatToDbName(readRoundInfo.displayName)
                if (!fileRoundNames.add(fileRoundName)) {
                    // TODO Rollback all updates?
                    setState(InternalError(currentVersion, "Duplicate name in default rounds file: $fileRoundName"))
                    return@withContext true
                }

                /*
                 * Compare and update db
                 */
                // Should not be null as empty database will return an empty list
                val dbRoundInfo = dbRoundsInfo.find { it.round.name == fileRoundName }
                val dbUpdateItems = if (dbRoundInfo == null) {
                    getUpdateItemsForNewRound(readRoundInfo, nextRoundId++)
                }
                else {
                    getUpdateItemsForExistingRound(readRoundInfo, dbRoundInfo)
                }

                dbUpdateItems.takeIf { it.isNotEmpty() }
                        ?.let {
                            runBlocking {
                                repository.updateRounds(it)
                            }
                        }
            }

            /*
             * Remove rounds and related objects from the database that are not in readRounds
             */
            setState(DeletingOld(currentVersion))
            dbRoundsInfo.filter { !fileRoundNames.contains(it.round.name) }
                    .flatMap { fullRoundInfo ->
                        mutableListOf<Any>(fullRoundInfo.round).apply {
                            fullRoundInfo.roundArrowCounts?.let { addAll(it) }
                            fullRoundInfo.roundSubTypes?.let { addAll(it) }
                            fullRoundInfo.roundDistances?.let { addAll(it) }
                        }
                    }
                    .associateWith { UpdateType.DELETE }
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        runBlocking {
                            repository.updateRounds(it)
                        }
                    }

            /*
             * Update the version so that we only update if necessary
             */
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, dataVersion)
            editor.apply()
            logger.d(LOG_TAG, "runTask successfully completed")

            setState(Complete(dataVersion, COMPLETE))
            return@withContext true
        }
        finally {
            logger.d(LOG_TAG, "runTask finally")
            // TODO Is a lock required?
//            RoundRepo.repositoryWriteLock.unlock()
        }
    }

    private suspend fun setState(state: UpdateDefaultRoundsState) {
        when (state) {
            is InternalError -> state.message
            is TemporaryError -> state.type.name
            else -> null
        }.let { errorMessage ->
            logger.e(
                    LOG_TAG,
                    "Update default rounds task failed with exception: " + errorMessage
                            + "\nlast progress token was " + _state.value.asLogString()
            )
        }
        _state.emit(state)
    }

    /**
     * Compares [fileData] to [dbData] based on [equalityMeasures]
     * - Marks any items in [dbData] not in [fileData] for deletion
     * - Marks any items in [fileData] not in [dbData] as new
     * - Marks any items in [fileData] and [dbData] with matching [equalityMeasures] for update if the items
     *   themselves are not equal
     * @return the marked items as a map of item to the type of update required
     */
    private fun <T> getUpdateItems(
            fileData: Set<T>,
            dbData: Set<T>,
            equalityMeasures: (T) -> List<Int>
    ): MutableMap<Any, UpdateType> {
        val returnMap = mutableMapOf<Any, UpdateType>()
        if (dbData != fileData) {
            /*
             * Delete any items that are in the DB but not the read data
             */
            for (dbItem in dbData) {
                if (fileData.find { equalityMeasures(it) == equalityMeasures(dbItem) } == null) {
                    returnMap[dbItem as Any] = UpdateType.DELETE
                }
            }

            /*
             * Check each read item
             */
            for (readItem in fileData) {
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
     * TODO THREAD_UNSAFE Can cause clashes if multiple things are accessing the database
     */
    private fun getUpdateItemsForExistingRound(
            fileRoundInfo: DefaultRoundInfo,
            dbRoundData: FullRoundInfo,
    ): Map<Any, UpdateType> {
        // Maps a db data item to an update type
        val dbUpdateItems = mutableMapOf<Any, UpdateType>()
        /*
         * Check round info
         */
        val roundId = dbRoundData.round.roundId

        fileRoundInfo.getRound(roundId).let { fileRound ->
            if (fileRound != dbRoundData.round) {
                dbUpdateItems[fileRound] = UpdateType.UPDATE
            }
        }

        dbUpdateItems.putAll(
                getUpdateItems(
                        fileRoundInfo.getRoundArrowCounts(roundId).toSet(),
                        dbRoundData.roundArrowCounts?.toSet() ?: setOf(),
                ) { listOf(it.distanceNumber) }
        )

        dbUpdateItems.putAll(
                getUpdateItems(
                        fileRoundInfo.getRoundSubTypes(roundId).toSet(),
                        dbRoundData.roundSubTypes?.toSet() ?: setOf(),
                ) { listOf(it.subTypeId) }
        )

        dbUpdateItems.putAll(
                getUpdateItems(
                        fileRoundInfo.getRoundDistances(roundId).toSet(),
                        dbRoundData.roundDistances?.toSet() ?: setOf(),
                ) { listOf(it.distanceNumber, it.subTypeId) }
        )

        return dbUpdateItems
    }

    /**
     * TODO THREAD_UNSAFE Can cause clashes if multiple things are accessing the database
     */
    private fun getUpdateItemsForNewRound(
            fileRoundInfo: DefaultRoundInfo,
            roundId: Int,
    ): Map<Any, UpdateType> {
        val dbUpdateItems = mutableMapOf<Any, UpdateType>()
        dbUpdateItems[fileRoundInfo.getRound(roundId)] = UpdateType.NEW
        dbUpdateItems.putAll(fileRoundInfo.getRoundArrowCounts(roundId).map { it to UpdateType.NEW })
        dbUpdateItems.putAll(fileRoundInfo.getRoundSubTypes(roundId).map { it to UpdateType.NEW })
        dbUpdateItems.putAll(fileRoundInfo.getRoundDistances(roundId).map { it to UpdateType.NEW })
        return dbUpdateItems
    }

    companion object {
        const val LOG_TAG = "UpdateDefaultRoundsTask"
    }
}
