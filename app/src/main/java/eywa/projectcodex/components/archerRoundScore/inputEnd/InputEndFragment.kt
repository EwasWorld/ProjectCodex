package eywa.projectcodex.components.archerRoundScore.inputEnd

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.ArrowInputsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.EndInputsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.ScoreIndicatorFragment
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.fragment_input_end.*


class InputEndFragment : Fragment(), ActionBarHelp, ArcherRoundBottomNavigationInfo {
    companion object {
        const val LOG_TAG = "InputEndFragment"
    }

    private val args: InputEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private lateinit var endInputsFragment: EndInputsFragment
    private var showRemainingArrows = false
    private var arrows = emptyList<ArrowValue>()
    private var arrowCounts = emptyList<RoundArrowCount>()
    private var distances = emptyList<RoundDistance>()
    private var distanceUnit: String = ""
    private var roundName: String? = null

    private val roundCompleteDialog by lazy {
        AlertDialog.Builder(requireView().context)
                .setTitle(R.string.input_end__round_complete)
                .setMessage(R.string.input_end__go_to_summary)
                .setPositiveButton(R.string.general_ok) { dialogInterface, _ ->
                    dialogInterface.cancel()
                    requireView().findNavController().navigate(
                            InputEndFragmentDirections
                                    .actionInputEndFragmentToArcherRoundStatsFragment(args.archerRoundId)
                    )
                }
                // Prevents clicking off the dialog and pressing the back button to cancel
                .setCancelable(false)
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CustomLogger.customLogger.d(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_input_end, container, false)
        endInputsFragment =
                childFragmentManager.findFragmentById(R.id.fragment_input_end__end_inputs)!! as EndInputsFragment

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(requireActivity().application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.archerRoundWithInfo.observe(viewLifecycleOwner, { archerRoundInfo ->
            if (archerRoundInfo == null) {
                return@observe
            }
            roundName = archerRoundInfo.displayName ?: ""
            setFragmentTitle()
            showRemainingArrows = archerRoundInfo.round != null
            archerRoundInfo.round?.let { round ->
                inputEndViewModel.getArrowCountsForRound(round.roundId).observe(viewLifecycleOwner, {
                    arrowCounts = it
                    updateRoundInfo(view)
                })
                inputEndViewModel.getDistancesForRound(round.roundId, archerRoundInfo.archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, {
                            distances = it
                            updateRoundInfo(view)
                        })
                endInputsFragment.setScoreButtons(ArrowInputsFragment.ArrowInputsType.getType(round))
                distanceUnit =
                        getString(if (round.isMetric) R.string.units_meters_short else R.string.units_yards_short)
                updateRoundInfo(view)
            }
        })
        inputEndViewModel.arrows.observe(viewLifecycleOwner, { arrows ->
            arrows?.let {
                this.arrows = arrows
                updateRoundInfo(view)
            }
        })

        return view
    }

    private fun setFragmentTitle() {
        activity?.title = roundName ?: getString(R.string.input_end__title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomLogger.customLogger.d(LOG_TAG, "onViewCreated")
        setFragmentTitle()

        (childFragmentManager.findFragmentById(R.id.fragment_input_end__score_indicator)!! as ScoreIndicatorFragment)
                .onClickListener = View.OnClickListener {
            val action = InputEndFragmentDirections.actionInputEndFragmentToScorePadFragment(
                    args.archerRoundId
            )
            view.findNavController().navigate(action)
        }

        button_input_end__next_end.setOnClickListener {
            if (isRoundComplete()) {
                CustomLogger.customLogger.w(LOG_TAG, "Input end's next end button pressed when round is complete")
                if (!roundCompleteDialog.isShowing) {
                    roundCompleteDialog.show()
                }
                return@setOnClickListener
            }

            try {
                // Update database
                var highestArrowNumber = 0
                for (arrow in arrows) {
                    if (arrow.arrowNumber > highestArrowNumber) {
                        highestArrowNumber = arrow.arrowNumber
                    }
                }
                endInputsFragment.end.addArrowsToDatabase(
                        args.archerRoundId, highestArrowNumber + 1, inputEndViewModel
                ) { endInputsFragment.clearEnd() }
            }
            catch (e: UserException) {
                ToastSpamPrevention.displayToast(requireContext(), e.getUserMessage(resources))
            }
            catch (e: Exception) {
                ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err__internal_error))
            }
        }
    }

    /**
     * Updates round indicators (72 arrows left at 100yd, etc.)
     * Updates the scores indicator table (current score, arrow count, etc.)
     * Updates the remaining arrows in [endInputsFragment]
     */
    private fun updateRoundInfo(view: View) {
        /*
         * Scores indicator table
         */
        val scoreIndicatorFragment = childFragmentManager.findFragmentById(R.id.fragment_input_end__score_indicator)!!
                as ScoreIndicatorFragment
        scoreIndicatorFragment.update(arrows)

        /*
         * Round Indicators
         */
        val roundInfoLayout = view.findViewById<LinearLayout>(R.id.layout_input_end__round_info)
        if (!showRemainingArrows || arrowCounts.sumOf { it.arrowCount } == 0 || distances.size != arrowCounts.size
            || distanceUnit.isBlank()
        ) {
            roundInfoLayout.visibility = View.GONE
            return
        }

        roundInfoLayout.visibility = View.VISIBLE
        val remainingArrows = RemainingArrows(arrows.size, arrowCounts, distances, distanceUnit)
        endInputsFragment.end.distanceRemainingArrows = remainingArrows.getFirstRemainingArrowCount()
        val roundIndicators =
                remainingArrows.toString(view.resources.getString(R.string.input_end__round_indicator_at))
        val label = view.findViewById<TextView>(R.id.text_input_end__remaining_arrows_label)
        val large = view.findViewById<TextView>(R.id.text_input_end__remaining_arrows_current_distance)
        val small = view.findViewById<TextView>(R.id.text_input_end__remaining_arrows_later_distances)
        if (roundIndicators.first.isNotBlank()) {
            label.text = view.resources.getString(R.string.input_end__round_indicator_label)
            large.text = roundIndicators.first
            small.text = roundIndicators.second
            large.visibility = View.VISIBLE
            small.visibility = View.VISIBLE
        }
        else {
            label.text = view.resources.getString(R.string.input_end__round_complete)
            large.visibility = View.GONE
            small.visibility = View.GONE

            if (!roundCompleteDialog.isShowing) {
                roundCompleteDialog.show()
            }
        }
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        val main = mutableListOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_input_end__next_end,
                        getString(R.string.help_input_end__next_end_title),
                        getString(R.string.help_input_end__next_end_body)
                )
        )
        if (requireView().findViewById<TextView>(R.id.layout_input_end__round_info).isVisible) {
            main.add(
                    ActionBarHelp.HelpShowcaseItem(
                            R.id.layout_input_end__round_info,
                            getString(R.string.help_input_end__remaining_arrows_title),
                            getString(R.string.help_input_end__remaining_arrows_body),
                            priority = 20
                    )
            )
        }
        return main
    }

    override fun getHelpPriority(): Int? {
        return null
    }

    override fun getArcherRoundId(): Int {
        return args.archerRoundId
    }

    override fun isRoundComplete(): Boolean {
        return false
    }
}
