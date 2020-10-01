package eywa.projectcodex.ui

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