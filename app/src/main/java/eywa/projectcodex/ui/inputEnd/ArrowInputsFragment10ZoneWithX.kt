package eywa.projectcodex.ui.inputEnd

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import eywa.projectcodex.R
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class ArrowInputsFragment10ZoneWithX : Fragment() {
    private var listener: ScoreButtonPressedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_arrow_inputs_10_zone_with_x, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (buttonId in view.findViewById<Group>(R.id.group_arrow_inputs__score_buttons).referencedIds) {
            val button = view.findViewById<Button>(buttonId)!!
            button.setOnClickListener {
                listener?.onScoreButtonPressed(button.text.toString())
            }
        }
    }

    interface ScoreButtonPressedListener {
        fun onScoreButtonPressed(score: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = findListener((context as FragmentActivity).nav_host_fragment)
                   ?: throw ClassCastException("$context must implement ScoreButtonPressedListener")
    }

    /**
     * Does a breadth-first search of **child** fragments of [root] searching for an instance of [T]
     */
    private inline fun <reified T> findListener(root: Fragment): T? {
        val queue: Queue<Fragment>? = LinkedList(listOf(root))
        while (queue!!.isNotEmpty()) {
            for (fragment in queue.remove().childFragmentManager.fragments) {
                if (fragment is T) return fragment
                queue.offer(fragment)
            }
        }
        return null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}