package eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.findInstanceOf


class ArrowInputsFragment10ZoneWithX : Fragment() {
    companion object {
        private const val LOG_TAG = "ArrowInputsFragment10ZoneWithX"
    }

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
        listener = findInstanceOf(this)
        if (listener == null) {
            /*
             * Downgraded from throwing an exception to showing a warning because
             * if navigating away from an input end fragment (e.g. to the score pad),
             * the ScoreButtonPressedListener will no longer be locatable by findInstanceOf
             * but the app may still recreate this fragment causing a detach/attach action
             * where no listener can be found
             */
            CustomLogger.customLogger.w(LOG_TAG, "No ScoreButtonPressedListener found")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}