package eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects

import com.beust.klaxon.*

/**
 * Reads the 'rounds' property from the given object as an array of strings {"rounds": [...], ...}
 */
class RoundsList(val rounds: List<String>) {
    class RoundsListJsonConverter : Converter {
        override fun canConvert(cls: Class<*>): Boolean {
            return RoundsList::class.java.isAssignableFrom(cls)
        }

        @Suppress("UNCHECKED_CAST")
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