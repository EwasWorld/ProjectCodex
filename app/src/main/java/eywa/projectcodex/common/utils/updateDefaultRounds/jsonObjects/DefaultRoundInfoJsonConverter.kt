package eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects

import com.beust.klaxon.*
import eywa.projectcodex.common.logging.CustomLogger

/**
 * Converts a json string into a [DefaultRoundInfo] object
 */
class DefaultRoundInfoJsonConverter(private val logger: CustomLogger) : Converter {
    companion object {
        private const val CONVERTER_LOG_TAG = "CustomJsonConverter"
    }

    override fun canConvert(cls: Class<*>): Boolean {
        return DefaultRoundInfo::class.java.isAssignableFrom(cls)
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromJson(jv: JsonValue): Any {
        val klaxon = Klaxon()
        val jsonRoundObject = jv.obj ?: throw KlaxonException("Top level item is not an object: ${jv.string}")

        val legacyRoundName = parseOptionalObject<String>(jsonRoundObject, "legacyRoundName")
        val rawRoundId = parseObject<Int>(jsonRoundObject, "id")
        val roundName = parseObject<String>(jsonRoundObject, "roundName")
        logger.i(CONVERTER_LOG_TAG, roundName)
        val isOutdoor = parseObject<Boolean>(jsonRoundObject, "outdoor")
        val isMetric = parseObject<Boolean>(jsonRoundObject, "isMetric")
        val fiveArrowEnd = parseObject<Boolean>(jsonRoundObject, "fiveArrowEnd")

        val roundLengthsJson = jsonRoundObject["roundSubTypes"] as? JsonArray<String>
                ?: throw KlaxonException("roundSubTypes is not an array")
        val roundLengths = mutableListOf<DefaultRoundInfo.RoundInfoSubType>()
        for (roundLength in (roundLengthsJson.value as ArrayList<JsonObject>)) {
            roundLength["id"] = roundLength["roundSubTypeId"]
            roundLength.remove("roundSubTypeId")
            val parsed = klaxon.parse<DefaultRoundInfo.RoundInfoSubType>(roundLength.toJsonString())
            if (parsed != null) {
                roundLengths.add(parsed)
            }
        }

        val roundProgressionJson = jsonRoundObject["roundArrowCounts"] as? JsonArray<JsonObject>
                ?: throw KlaxonException("roundArrowCounts is not an array")
        val roundProgressions = mutableListOf<DefaultRoundInfo.RoundInfoArrowCount>()
        for (roundProgression in (roundProgressionJson.value as ArrayList<JsonObject>)) {
            val parsed = klaxon.parse<DefaultRoundInfo.RoundInfoArrowCount>(roundProgression.toJsonString())
            if (parsed != null) {
                roundProgressions.add(parsed)
            }
        }

        val roundDistancesJson = jsonRoundObject["roundDistances"] as? JsonArray<JsonObject>
                ?: throw KlaxonException("roundDistances is not an array")
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
                legacyRoundName, rawRoundId,
                roundName, isOutdoor, isMetric, fiveArrowEnd,
                roundLengths, roundProgressions, roundDistances
        )
    }

    /**
     * Not currently used
     */
    override fun toJson(value: Any): String {
        throw NotImplementedError()
    }
}
