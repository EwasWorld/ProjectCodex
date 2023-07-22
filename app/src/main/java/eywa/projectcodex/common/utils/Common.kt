package eywa.projectcodex.common.utils


/**
 * For each entry in [replacements], [resourceString].replace("{$key}", "value"). If no instance of "{$key}" is found,
 * nothing is replaced
 */
@Deprecated("Use numbered arguments instead")
fun resourceStringReplace(resourceString: String, replacements: Map<String, String>): String {
    var newString = resourceString
    for (entry in replacements.entries) {
        if (entry.key.contains(Regex("[{}]+"))) {
            throw IllegalArgumentException("Items given to resource string replace should not contain { or }")
        }
        newString = newString.replace("{${entry.key}}", entry.value)
    }
    return newString
}

fun <T> List<List<T>>.transpose(): List<List<T>> {
    if (isEmpty()) return this
    check(all { it.size == first().size }) { "Must be rectangular" }
    if (first().isEmpty()) return listOf(emptyList())

    return first().indices.map { index -> map { it[index] } }
}
