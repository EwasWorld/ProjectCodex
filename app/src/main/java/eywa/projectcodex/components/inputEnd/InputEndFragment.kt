package eywa.projectcodex.components.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.components.inputEnd.subFragments.EndInputsFragment
import eywa.projectcodex.components.inputEnd.subFragments.ScoreIndicatorFragment
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.fragment_input_end.*


class InputEndFragment : Fragment(), ActionBarHelp {
    private val args: InputEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private lateinit var endInputsFragment: EndInputsFragment
    private var arrows = emptyList<ArrowValue>()
    private var arrowCounts = emptyList<RoundArrowCount>()
    private var distances = emptyList<RoundDistance>()
    private var distanceUnit: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_input_end, container, false)

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(requireActivity().application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.archerRound.observe(viewLifecycleOwner, Observer { archerRound ->
            archerRound.roundId?.let { roundId ->
                inputEndViewModel.getArrowCountsForRound(roundId).observe(viewLifecycleOwner, Observer {
                    arrowCounts = it
                    updateRoundInfo(view)
                })
                inputEndViewModel.getDistancesForRound(roundId, archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, Observer {
                            distances = it
                            updateRoundInfo(view)
                        })
                inputEndViewModel.getRoundById(roundId).observe(viewLifecycleOwner, Observer {
                    distanceUnit =
                            getString(if (it.isMetric) R.string.units_meters_short else R.string.units_yards_short)
                    updateRoundInfo(view)
                })
            }
        })
        inputEndViewModel.arrows.observe(viewLifecycleOwner, Observer { arrows ->
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

        button_input_end__score_pad.setOnClickListener {
            val action = InputEndFragmentDirections.actionInputEndFragmentToScorePadFragment(
                    endInputsFragment.end.endSize,
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
                )
                endInputsFragment.clearEnd()
            }
            catch (e: UserException) {
                Toast.makeText(context, e.getUserMessage(resources), Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception) {
                Toast.makeText(context, getString(R.string.err__internal_error), Toast.LENGTH_SHORT).show()
            }
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = InputEndFragmentDirections.actionInputEndFragmentToMainMenuFragment()
            view.findNavController().navigate(action)
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
        val scoreIndicatorFragment =
                childFragmentManager.findFragmentById(R.id.fragment_input_end__score_indicator)!!
                        as ScoreIndicatorFragment
        scoreIndicatorFragment.update(arrows)

        /*
         * Round Indicators
         */
        val roundIndicatorSection = view.findViewById<LinearLayout>(R.id.layout_input_end__remaining_arrows)
        if (!args.showRemaining || distances.size != arrowCounts.size || distanceUnit.isBlank()) {
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
            label.text = view.resources.getString(R.string.input_end__round_indicator_complete)
            large.visibility = View.GONE
            small.visibility = View.GONE
        }
    }

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        TODO("Not yet implemented")
    }

    override fun getHelpPriority(): Int? {
        TODO("Not yet implemented")
    }
}
