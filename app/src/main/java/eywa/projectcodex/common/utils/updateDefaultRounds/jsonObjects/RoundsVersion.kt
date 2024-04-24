package eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException
import eywa.projectcodex.common.utils.DoNotObfuscate

/**
 * Reads the 'version' property from the given object as an int {"version": 000, ...}
 */
@DoNotObfuscate
class RoundsVersion(val version: Int) {
    @DoNotObfuscate
    class RoundsVersionJsonConverter : Converter {
        override fun canConvert(cls: Class<*>): Boolean {
            return RoundsVersion::class.java.isAssignableFrom(cls)
        }

        override fun fromJson(jv: JsonValue): Any {
            val jsonObject = jv.obj ?: throw KlaxonException("Cannot parse null object: ${jv.string}")
            return RoundsVersion(parseObject(jsonObject, "version"))
        }

        /**
         * Not currently used
         */
        override fun toJson(value: Any): String {
            throw NotImplementedError()
        }
    }
}
