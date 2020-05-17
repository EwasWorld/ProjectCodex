package eywa.projectcodex

import com.beust.klaxon.*
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType

private const val CONVERTER_LOG_TAG = "CustomJsonConverter"
private const val ROUND_CHECKER_LOG_TAG = "DefaultRoundChecker"
private const val defaultRoundMinimumId = 5


/**
 * @throws KlaxonException for invalid Json
 * @throws ClassCastException for invalid type for any given object
 */
fun roundsFromJson(objectJson: String): List<DefaultRoundInfo> {
    return Klaxon().converter(ParsedRoundsConverter()).parse<ParsedRounds>(objectJson)!!.defaultRoundInfo
}

/**
 * A wrapper object to parse a JsonArrays of RoundInfo objects
 */
private class ParsedRounds(val defaultRoundInfo: List<DefaultRoundInfo>) {
    init {
        require(defaultRoundInfo.size == defaultRoundInfo.distinctBy {
            formatNameString(it.displayName)
        }.size) { "Duplicate round names are not allowed" }
    }
}

/**
 * Removes non-alphanumerics and spaces and converts to lower case
 */
fun formatNameString(string: String): String {
    // TODO Locale
    return string.replace(Regex("[^A-Za-z0-9]| "), "").toLowerCase()
}

/**
 * @param allDbRounds all rounds currently in the database. Must be all so that IDs for new default rounds can be
 * created without clashes
 * @return map of all the database entities that need to be updated to bring the database's default rounds in line with
 * the default rounds provided. Items will only be of the data class type (data class Archer() {}). Note for deletion of
 * a round it will only return an entry for the main round, not all arrowCounts, etc.
 */
fun checkDefaultRounds(
        defaultRounds: List<DefaultRoundInfo>,
        allDbRounds: List<Round>,
        allDbArrowCounts: List<RoundArrowCount>,
        allDbSubTypes: List<RoundSubType>,
        allDbDistances: List<RoundDistance>
): Map<Any, UpdateType> {
    require(defaultRounds.isNotEmpty()) { "No default rounds given" }
    if (allDbRounds.isEmpty()) {
        Log.i(ROUND_CHECKER_LOG_TAG, "No database rounds given")
    }

    val returnMap = mutableMapOf<Any, UpdateType>()
    // Used to calculate ids for new rounds
    val currentDefaultIds = allDbRounds.map { it.roundId }.toMutableList()
    val dbRounds = allDbRounds.filter { it.isDefaultRound }

    for (round in dbRounds) {
        require(allDbArrowCounts.any {
            it.roundId == round.roundId
        }) { "Default round has no arrow counts associated with it" }
        require(allDbDistances.any {
            it.roundId == round.roundId
        }) { "Default round has no distances associated with it" }
    }

    /*
     * Delete any rounds that no longer exist in defaultRounds
     */
    Log.d(ROUND_CHECKER_LOG_TAG, "deleting rounds")
    val deletedDbRounds = dbRounds.filter { dbRound ->
        !defaultRounds.map { formatNameString(it.displayName) }.contains(dbRound.name)
    }
    returnMap.putAll(deletedDbRounds.map { it to UpdateType.DELETE })

    /*
     * Check details of each default round
     */
    for (defaultRoundInfo in defaultRounds) {
        Log.i(ROUND_CHECKER_LOG_TAG, "Checking round: ${defaultRoundInfo.displayName}")
        val dbRound = dbRounds.find { it.name == formatNameString(defaultRoundInfo.displayName) }
        var roundId: Int

        /*
         * Add new round if doesn't exist in DB already
         */
        if (dbRound == null) {
            Log.d(ROUND_CHECKER_LOG_TAG, "round is new")
            roundId = -1
            /*
             * Note: new round ids should not clash with any existing round ids, even if the round will be deleted.
             * This means that whatever order the database updates are made, the database will remain consistent.
             */
            val dbDefaultMax = dbRounds.map { it.roundId }.max()
            if (dbDefaultMax != null) {
                var defaultRange =
                    defaultRoundMinimumId.rangeTo(dbDefaultMax).filter { !currentDefaultIds.contains(it) }

                if (defaultRange.isNotEmpty()) {
                    roundId = defaultRange[0]
                }
                else {
                    defaultRange = dbDefaultMax.rangeTo(dbDefaultMax + 100).filter { !currentDefaultIds.contains(it) }
                    if (defaultRange.isNotEmpty()) {
                        roundId = defaultRange[0]
                    }
                }
            }
            if (roundId <= 0) {
                roundId = (currentDefaultIds.max() ?: defaultRoundMinimumId - 1) + 1
            }
            currentDefaultIds.add(roundId)

            /*
             * Add round
             */
            returnMap[defaultRoundInfo.getRound(roundId)] = UpdateType.NEW
            returnMap.putAll(defaultRoundInfo.getRoundArrowCounts(roundId).map { it to UpdateType.NEW })
            returnMap.putAll(defaultRoundInfo.getRoundSubTypes(roundId).map { it to UpdateType.NEW })
            returnMap.putAll(defaultRoundInfo.getRoundDistances(roundId).map { it to UpdateType.NEW })
            continue
        }

        roundId = dbRound.roundId

        /*
         * Check main round info
         */
        val defaultRound = defaultRoundInfo.getRound(roundId)
        if (defaultRound != dbRound) {
            returnMap[defaultRound] = UpdateType.UPDATE
        }

        /*
         * Check arrow counts
         */
        Log.d(ROUND_CHECKER_LOG_TAG, "checking arrow counts")
        val dbArrowCounts = allDbArrowCounts.filter { it.roundId == roundId }.toSet()
        val defaultArrowCounts = defaultRoundInfo.getRoundArrowCounts(roundId).toSet()
        if (dbArrowCounts != defaultArrowCounts) {
            /*
             * Delete any that don't exist
             */
            for (dbArrowCount in dbArrowCounts) {
                if (defaultArrowCounts.find { it.distanceNumber == dbArrowCount.distanceNumber } == null) {
                    returnMap[dbArrowCount] = UpdateType.DELETE
                }
            }

            /*
             * Check each arrow count
             */
            for (defaultArrowCount in defaultArrowCounts) {
                val dbArrowCount = dbArrowCounts.find { it.distanceNumber == defaultArrowCount.distanceNumber }

                if (dbArrowCount == null) {
                    returnMap[defaultArrowCount] = UpdateType.NEW
                }
                else if (dbArrowCount != defaultArrowCount) {
                    returnMap[defaultArrowCount] = UpdateType.UPDATE
                }
                // else remain unchanged
            }
        }

        /*
         * Check subtypes
         */
        Log.d(ROUND_CHECKER_LOG_TAG, "checking sub types")
        val dbSubTypes = allDbSubTypes.filter { it.roundId == roundId }.toSet()
        val defaultSubTypes = defaultRoundInfo.getRoundSubTypes(roundId).toSet()
        if (dbSubTypes !== defaultSubTypes) {
            /*
             * Delete any that don't exist
             */
            for (dbSubType in dbSubTypes) {
                if (defaultSubTypes.find { it.subTypeId == dbSubType.subTypeId } == null) {
                    returnMap[dbSubType] = UpdateType.DELETE
                }
            }

            /*
             * Check each subtype
             */
            for (defaultSubType in defaultSubTypes) {
                val dbSubType = dbSubTypes.find { it.subTypeId == defaultSubType.subTypeId }

                if (dbSubType == null) {
                    returnMap[defaultSubType] = UpdateType.NEW
                }
                else if (dbSubType != defaultSubType) {
                    returnMap[defaultSubType] = UpdateType.UPDATE
                }
                // else remain unchanged
            }
        }

        /*
         * Check distances
         */
        Log.d(ROUND_CHECKER_LOG_TAG, "checking distances")
        val dbDistances = allDbDistances.filter { it.roundId == roundId }.toSet()
        val defaultDistances = defaultRoundInfo.getRoundDistances(roundId).toSet()
        if (dbDistances != defaultDistances) {
            /*
             * Delete any that don't exist
             */
            for (dbDistance in dbDistances) {
                if (defaultDistances.find {
                            it.distanceNumber == dbDistance.distanceNumber
                                    && it.subTypeId == dbDistance.subTypeId
                        } == null) {
                    returnMap[dbDistance] = UpdateType.DELETE
                }
            }

            /*
             * Check each distances
             */
            for (defaultDistance in defaultDistances) {
                val dbDistance = dbDistances.find {
                    it.distanceNumber == defaultDistance.distanceNumber
                            && it.subTypeId == defaultDistance.subTypeId
                }

                if (dbDistance == null) {
                    returnMap[defaultDistance] = UpdateType.NEW
                }
                else if (dbDistance != defaultDistance) {
                    returnMap[defaultDistance] = UpdateType.UPDATE
                }
                // else remain unchanged
            }
        }
    }

    /*
     * Sanity check
     */
    check(returnMap.filterKeys { !(it::class.isData) }.isEmpty()) { "Default rounds checker returned an invalid type" }
    return returnMap
}

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
        val subTypeList = if (roundSubTypes.isNotEmpty()) roundSubTypes else listOf(RoundInfoSubType(1, "", null, null))
        for (subType in subTypeList) {
            val distances = roundDistances.filter { subTypeCount -> subTypeCount.roundSubTypeId == subType.id }
            require(distances.size == distances.distinctBy { it.distance }.size) { "Duplicate distance in $displayName for subType: ${subType.id}" }
            require(roundArrowCounts.map { it.distanceNumber }.toSet() == distances.map { it.distanceNumber }.toSet()) { "Mismatched distanceNumbers in $displayName for subType: ${subType.id}" }
            require(distances.sortedByDescending { it.distance } == distances.sortedBy { it.distanceNumber }) { "Distances in $displayName are not non-ascending subType: ${subType.id}" }
        }

        // Names
        require(formatNameString(displayName) != "") { "Round name cannot be empty" }
        require(roundSubTypes.size == roundSubTypes.distinctBy {
            formatNameString(
                    it.subTypeName
            )
        }.size) { "Duplicate sub type names in $displayName" }
        require(roundSubTypes.size <= 1 || roundSubTypes.count {
            formatNameString(
                    it.subTypeName
            ) == ""
        } == 0) { "Illegal empty sub type name in $displayName" }
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
                roundId, formatNameString(displayName), displayName, isOutdoor, isMetric, permittedFaces, true,
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

private class ParsedRoundsConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean {
        return cls == ParsedRounds::class.java
    }

    override fun fromJson(jv: JsonValue): Any? {
        val klaxon = Klaxon()
        val jsonObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")
        val jsonRoundObjects = jsonObject["rounds"] as JsonArray<JsonObject>

        val all = mutableListOf<DefaultRoundInfo>()
        for (jsonRoundObject in jsonRoundObjects) {
            val roundName = parseObject<String>(jsonRoundObject, "roundName")
            Log.i(CONVERTER_LOG_TAG, roundName)
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

            all.add(
                    DefaultRoundInfo(
                            roundName, isOutdoor, isMetric, fiveArrowEnd, permittedFaces,
                            roundLengths, roundProgressions, roundDistances
                    )
            )
        }
        return ParsedRounds(all)
    }

    /**
     * @return the value the specified key maps to in the jsonObject
     */
    private fun <T> parseObject(jsonObject: JsonObject, key: String): T {
        return jsonObject[key] as? T ?: throw KlaxonException("Cannot parse $key from: $jsonObject")
    }

    /**
     * Not currently used
     */
    override fun toJson(value: Any): String {
        return "{}"
    }
}