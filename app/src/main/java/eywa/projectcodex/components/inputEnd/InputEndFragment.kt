package eywa.projectcodex.components.inputEnd

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.components.inputEnd.subFragments.EndInputsFragment
import eywa.projectcodex.components.inputEnd.subFragments.ScoreIndicatorFragment
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.fragment_input_end.*


class InputEndFragment : Fragment(), ActionBarHelp, ArcherRoundBottomNavigationInfo {
    private val args: InputEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private lateinit var endInputsFragment: EndInputsFragment
    private var showRemainingArrows = false
    private var arrows = emptyList<ArrowValue>()
    private var arrowCounts = emptyList<RoundArrowCount>()
    private var distances = emptyList<RoundDistance>()
    private var distanceUnit: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_input_end, container, false)

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(requireActivity().application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.archerRound.observe(viewLifecycleOwner, { archerRound ->
            showRemainingArrows = archerRound.roundId != null
            archerRound.roundId?.let { roundId ->
                inputEndViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, {
                    arrowCounts = it
                    updateRoundInfo(view)
                })
                inputEndViewModel.getDistancesForRound(roundId, archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, {
                            distances = it
                            updateRoundInfo(view)
                        })
                inputEndViewModel.getRoundById(roundId).observe(viewLifecycleOwner, {
                    distanceUnit =
                            getString(if (it.isMetric) R.string.units_meters_short else R.string.units_yards_short)
                    updateRoundInfo(view)
                })
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.input_end__title)
        endInputsFragment =
                childFragmentManager.findFragmentById(R.id.fragment_input_end__end_inputs)!! as EndInputsFragment

        (childFragmentManager.findFragmentById(R.id.fragment_input_end__score_indicator)!! as ScoreIndicatorFragment)
                .onClickListener = View.OnClickListener {
            val action = InputEndFragmentDirections.actionInputEndFragmentToScorePadFragment(
                    args.archerRoundId
            )
            view.findNavController().navigate(action)
        }

        button_input_end__next_end.setOnClickListener {
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

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            returnToMainMenu(view)
        }
        callback.isEnabled = true
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
        val roundIndicatorSection = view.findViewById<LinearLayout>(R.id.layout_input_end__remaining_arrows)
        if (!showRemainingArrows || distances.size != arrowCounts.size || distanceUnit.isBlank()) {
            roundIndicatorSection.visibility = View.GONE
            return
        }

        roundIndicatorSection.visibility = View.VISIBLE
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

            AlertDialog.Builder(view.context)
                    .setTitle(R.string.input_end__round_complete)
                    .setMessage(R.string.input_end__return_to_main_menu)
                    .setPositiveButton(R.string.general_ok) { dialogInterface, _ ->
                        dialogInterface.cancel()
                        returnToMainMenu(view)
                    }.show()
        }
    }

    private fun returnToMainMenu(view: View) {
        val action = InputEndFragmentDirections.actionInputEndFragmentToMainMenuFragment()
        view.findNavController().navigate(action)
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        val main = mutableListOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_input_end__next_end,
                        getString(R.string.help_input_end__next_end_title),
                        getString(R.string.help_input_end__next_end_body)
                )
        )
        if (requireView().findViewById<TextView>(R.id.layout_input_end__remaining_arrows).isVisible) {
            main.add(
                    ActionBarHelp.HelpShowcaseItem(
                            R.id.layout_input_end__remaining_arrows,
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
