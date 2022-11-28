package eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects

import com.beust.klaxon.JsonObject
import com.beust.klaxon.KlaxonException


/**
 * @return the value the specified key maps to in the jsonObject
 */
internal inline fun <reified T> parseObject(jsonObject: JsonObject, key: String): T {
    val retrievedObject = jsonObject[key]
            ?: throw KlaxonException("Cannot parse $key from: $jsonObject")
    if (retrievedObject !is T) {
        throw ClassCastException("$key is not of type ${T::class.java.name}")
    }
    return retrievedObject
}