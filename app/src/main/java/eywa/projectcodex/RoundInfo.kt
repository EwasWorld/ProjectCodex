package eywa.projectcodex

import com.beust.klaxon.*
import java.util.*

fun fromJson(objectJson: String): List<RoundInfo> {
    return Klaxon().converter(ParsedRoundsConverter()).parse<ParsedRounds>(objectJson)!!.roundInfo
}

/**
 * A wrapper object to parse a JsonArrays of RoundInfo objects
 */
private class ParsedRounds(val roundInfo: List<RoundInfo>)

class RoundInfo(
        val isOutdoor: Boolean,
        val isMetric: Boolean,
        val fiveArrowEnd: Boolean,
        val roundName: String,
        val permittedFaces: List<String>,
        val roundSubTypes: List<RoundInfoSubType>,
        val roundArrowCount: List<RoundInfoArrowCount>,
        val roundSubTypeCount: List<RoundInfoSubTypeCount>
) {
    class RoundInfoSubType(
            val id: Int,
            val subTypeName: String,
            /* null -> adult round
             * 0 -> invalid (e.g. gents no matter their age cannot shoot a Bristol V
             *               as their closest is the under 12s Bristol IV
             * x -> for archers under the age x
             */
            val gentsUnder: Int?,
            val ladiesUnder: Int?
    )

    class RoundInfoArrowCount(
            // distance 1 is the first distance shot
            val distanceNumber: Int,
            val faceSizeInCm: Double,
            val arrowCount: Int
    )

    class RoundInfoSubTypeCount(
            val distanceNumber: Int,
            val roundSubTypeId: Int,
            // meters if RoundInfo isMetric, else yards
            val distance: Int
    )
}

private class ParsedRoundsConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean {
        return cls == ParsedRounds::class.java
    }

    override fun fromJson(jv: JsonValue): Any? {
        val klaxon = Klaxon()
        val jsonObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")
        val jsonRoundObjects = jsonObject["rounds"] as JsonArray<JsonObject>

        val all = mutableListOf<RoundInfo>()
        for (jsonRoundObject in jsonRoundObjects) {
            val isOutdoor = parseObject<Boolean>(jsonRoundObject, "outdoor")
            val isMetric = parseObject<Boolean>(jsonRoundObject, "isMetric")
            val fiveArrowEnd = parseObject<Boolean>(jsonRoundObject, "fiveArrowEnd")
            val roundName = parseObject<String>(jsonRoundObject, "roundName")

            val permittedFaces = (jsonRoundObject["permittedFaces"] as JsonArray<String>).value

            val roundLengthsJson = jsonRoundObject["roundSubTypes"] as JsonArray<String>
            val roundLengths = mutableListOf<RoundInfo.RoundInfoSubType>()
            for (roundLength in (roundLengthsJson.value as ArrayList<JsonObject>)) {
                roundLength["id"] = roundLength["roundSubTypeId"]
                roundLength.remove("roundSubTypeId")
                val parsed = klaxon.parse<RoundInfo.RoundInfoSubType>(roundLength.toJsonString())
                if (parsed != null) {
                    roundLengths.add(parsed)
                }
            }

            val roundProgressionJson = jsonRoundObject["roundArrowCounts"] as JsonArray<JsonObject>
            val roundProgressions = mutableListOf<RoundInfo.RoundInfoArrowCount>()
            for (roundProgression in (roundProgressionJson.value as ArrayList<JsonObject>)) {
                val parsed = klaxon.parse<RoundInfo.RoundInfoArrowCount>(roundProgression.toJsonString())
                if (parsed != null) {
                    roundProgressions.add(parsed)
                }
            }

            val roundDistancesJson = jsonRoundObject["roundSubTypeCounts"] as JsonArray<JsonObject>
            val roundDistances = mutableListOf<RoundInfo.RoundInfoSubTypeCount>()
            for (roundDistance in (roundDistancesJson.value as ArrayList<JsonObject>)) {
                val parsed = klaxon.parse<RoundInfo.RoundInfoSubTypeCount>(roundDistance.toJsonString())
                if (parsed != null) {
                    roundDistances.add(parsed)
                }
            }

            all.add(
                    RoundInfo(
                            isOutdoor, isMetric, fiveArrowEnd, roundName, permittedFaces,
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