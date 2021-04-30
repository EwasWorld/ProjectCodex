package eywa.projectcodex.logic

import android.content.res.Resources
import com.beust.klaxon.*
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.database.repositories.RoundsRepo
import eywa.projectcodex.ui.commonUtils.resourceStringReplace
import eywa.projectcodex.viewModels.OnToken
import eywa.projectcodex.viewModels.TaskRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val CONVERTER_LOG_TAG = "CustomJsonConverter"
private const val ROUND_CHECKER_LOG_TAG = "DefaultRoundChecker"
private const val defaultRoundMinimumId = 5

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
    /**
     * Validation
     * TODO Make a builder omg
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
         * @return a unique round ID
         * TODO_THREAD_UNSAFE Can cause clashes if multiple things are accessing the database
         */
        private fun getNewRoundId(existingRoundIds: List<Int>): Int {
            val dbDefaultMax = existingRoundIds.max() ?: return defaultRoundMinimumId

            // Check if there are any unused IDs to fill in
            val filteredIds = defaultRoundMinimumId.rangeTo(dbDefaultMax).filter { !existingRoundIds.contains(it) }
            return if (filteredIds.isEmpty()) dbDefaultMax + 1 else filteredIds.min()!!
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
         */
        fun getUpdates(
                readRoundInfo: DefaultRoundInfo,
                allDbRounds: List<Round>,
                allDbArrowCounts: List<RoundArrowCount>,
                allDbSubTypes: List<RoundSubType>,
                allDbDistances: List<RoundDistance>
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
                val roundId = getNewRoundId(allDbRounds.map { it.roundId })
                dbUpdateItems[readRoundInfo.getRound(roundId)] = UpdateType.NEW
                dbUpdateItems.putAll(readRoundInfo.getRoundArrowCounts(roundId).map { it to UpdateType.NEW })
                dbUpdateItems.putAll(readRoundInfo.getRoundSubTypes(roundId).map { it to UpdateType.NEW })
                dbUpdateItems.putAll(readRoundInfo.getRoundDistances(roundId).map { it to UpdateType.NEW })
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
 * TODO Passing the view model scope is almost certainly wrong
 */
class UpdateDefaultRounds(
        private val repository: RoundsRepo,
        private val resources: Resources,
        private val coroutineScope: CoroutineScope
) : TaskRunner.ProgressTask<String, Void?>() {
    companion object {
        const val LOG_TAG = "UpdateDefaultTask"
    }

    override fun runTask(progressToken: OnToken<String>): Void? {
        progressToken(resources.getString(R.string.main_menu__update_default_rounds_progress_init))
        val readRoundsStrings = JsonArrayToListConverter.convert(
                resources.openRawResource(R.raw.default_rounds_data).bufferedReader().use { it.readText() })
        val readRounds = mutableListOf<DefaultRoundInfo>()

        /*
         * Check read rounds
         */
        val progressTokenRawString = resources.getString(R.string.main_menu__update_default_rounds_progress_item)
        val progressTokenTotalReplacer = Pair("total", readRoundsStrings.size.toString())
        for (readRound in readRoundsStrings.withIndex()) {
            progressToken(
                    resourceStringReplace(
                            progressTokenRawString,
                            mapOf(progressTokenTotalReplacer, Pair("current", (readRound.index + 1).toString()))
                    )
            )
            val readRoundInfo = DefaultRoundInfoConverter.convert(readRound.value)
            readRounds.add(readRoundInfo)
            val dbUpdateItems = DefaultRoundInfoHelper.getUpdates(
                    readRoundInfo, repository.rounds.value!!, repository.roundArrowCounts.value!!,
                    repository.roundSubTypes.value!!, repository.roundDistances.value!!
            )
            /*
             * Update database
             */
            coroutineScope.launch {
                repository.updateRounds(dbUpdateItems)
            }
            if (isSoftCancelled) {
                CustomLogger.customLogger.i(LOG_TAG, "Task cancelled at ${readRound.index} or ${readRounds.size}")
                return null
            }
        }

        /*
         * Remove rounds and related objects from the database that are not in readRounds
         */
        progressToken(resources.getString(R.string.main_menu__update_default_rounds_progress_delete))
        val roundsToDelete = repository.rounds.value!!.filter { dbRound ->
            !readRounds.map { DefaultRoundInfoHelper.formatNameString(it.displayName) }.contains(dbRound.name)
        }

        val itemsToDelete = roundsToDelete.map { it as Any }.toMutableList()
        val idsOfRoundsToDelete = roundsToDelete.map { it.roundId }
        itemsToDelete.addAll(repository.roundArrowCounts.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })
        itemsToDelete.addAll(repository.roundSubTypes.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })
        itemsToDelete.addAll(repository.roundDistances.value!!.filter { idsOfRoundsToDelete.contains(it.roundId) })

        val updateItems = mapOf(*roundsToDelete.map { it as Any to UpdateType.DELETE }.toTypedArray())
        coroutineScope.launch {
            repository.updateRounds(updateItems)
        }


        progressToken(resources.getString(R.string.button_complete))
        return null
    }
}

// TODO Can I put this into a superclass?
private inline fun <reified T> convert(converter: Converter, jsonString: String): T? =
        Klaxon().converter(converter).parse<T>(jsonString)

/**
 * Splits a json string in the format {"rounds": [...]} into a list of strings
 */
class JsonArrayToListConverter : Converter {
    companion object {
        fun convert(objectJson: String): List<String> {
            return convert<List<String>>(JsonArrayToListConverter(), objectJson)!!
        }
    }

    override fun canConvert(cls: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(cls)
    }

    override fun fromJson(jv: JsonValue): Any {
        val jsonObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")
        val jsonRoundObjects = jsonObject["rounds"] as JsonArray<JsonObject>
        return jsonRoundObjects.toList().map { it.toJsonString() }
    }

    /**
     * Not currently used
     */
    override fun toJson(value: Any): String {
        throw NotImplementedError()
    }
}

/**
 * Converts a json string into a [DefaultRoundInfo] object
 */
class DefaultRoundInfoConverter : Converter {
    companion object {
        /**
         * @throws KlaxonException for invalid Json
         * @throws ClassCastException for invalid type for any given object
         */
        fun convert(objectJson: String): DefaultRoundInfo {
            return convert<DefaultRoundInfo>(DefaultRoundInfoConverter(), objectJson)!!
        }
    }

    override fun canConvert(cls: Class<*>): Boolean {
        return cls == DefaultRoundInfo::class.java
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