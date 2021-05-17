package eywa.projectcodex.components.commonUtils

import android.content.Context
import android.content.res.Resources
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import java.util.*


/**
 * Does a breadth-first search of **child** fragments of [root] searching for an instance of [T]
 */
inline fun <reified T> findInstanceOf(root: Fragment): T? {
    val queue: Queue<Fragment>? = LinkedList(listOf(root))
    while (queue!!.isNotEmpty()) {
        for (fragment in queue.remove().childFragmentManager.fragments) {
            if (fragment is T) return fragment
            queue.offer(fragment)
        }
    }
    return null
}

fun getColourResource(resources: Resources, colourResourceId: Int, theme: Resources.Theme): Int {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        resources.getColor(colourResourceId, theme)
    }
    else {
        ResourcesCompat.getColor(resources, colourResourceId, theme)
    }
}

/**
 * For each entry in [replacements], [resourceString].replace("{$key}", "value"). If no instance of "{$key}" is found,
 * nothing is replaced
 */
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

fun displayToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
