package eywa.projectcodex.ui

import android.content.res.Resources
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