package eywa.projectcodex.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import eywa.projectcodex.R


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

fun Context.openWebPage(url: String, onFail: () -> Unit) {
    val webpage = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, webpage)

    // TODO Swap try/catch with `if (intent.resolveActivity(packageManager) != null)`
    try {
        startActivity(intent)
    }
    catch (e: ActivityNotFoundException) {
        onFail()
    }
}

fun Any?.asDecimalFormat(decimalPlaces: Int = 1) =
        this?.let { ResOrActual.Actual("%.${decimalPlaces}f".format(this)) }
                ?: ResOrActual.StringResource(R.string.archer_round_stats__breakdown_placeholder)
