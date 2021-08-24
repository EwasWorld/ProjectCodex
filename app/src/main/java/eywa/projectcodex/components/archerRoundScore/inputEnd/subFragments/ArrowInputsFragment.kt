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
import eywa.projectcodex.common.utils.findInstanceOf
import eywa.projectcodex.components.archerRoundScore.inputEnd.ScoreButtonPressedListener
import eywa.projectcodex.database.rounds.Round


class ArrowInputsFragment(private val type: ArrowInputsType) : Fragment() {
    companion object {
        private const val LOG_TAG = "ArrowInputsFragment"
    }

    private var listener: ScoreButtonPressedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(type.layoutId, container, false)
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

    enum class ArrowInputsType(val layoutId: Int) {
        TEN_ZONE_WITH_X(R.layout.frag_arrow_inputs_10_zone_with_x), FIVE_ZONE(R.layout.frag_arrow_inputs_5_zone),
        WORCESTER(R.layout.frag_arrow_inputs_worcester);

        companion object {
            fun getType(round: Round): ArrowInputsType {
                return when {
                    round.displayName.contains(WORCESTER.toString(), ignoreCase = true) -> WORCESTER
                    round.isMetric -> TEN_ZONE_WITH_X
                    !round.isOutdoor -> TEN_ZONE_WITH_X
                    else -> FIVE_ZONE
                }
            }
        }
    }
}