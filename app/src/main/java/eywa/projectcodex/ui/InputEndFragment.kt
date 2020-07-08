package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.End
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.logic.getRemainingArrowsPerDistance
import eywa.projectcodex.viewModels.InputEndViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_input_end.*

class InputEndFragment : Fragment() {
    private val args: InputEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private var arrows = emptyList<ArrowValue>()
    private var arrowCounts = emptyList<RoundArrowCount>()
    private var distances = emptyList<RoundDistance>()
    private var distanceUnit: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.input_end__title)

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(activity!!.application, args.archerRoundId)
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

        val end =
            End(
                    6,
                    getString(R.string.end_to_string_arrow_placeholder),
                    getString(R.string.end_to_string_arrow_deliminator)
            )

        // Set the place holder text for the arrow scores
        view.findViewById<TextView>(R.id.text_arrow_scores).text = end.toString()

        button_score_pad.setOnClickListener {
            val action = InputEndFragmentDirections.actionInputEndFragmentToScorePadFragment(
                    end.arrowsPerEnd,
                    args.archerRoundId
            )
            view.findNavController().navigate(action)
        }

        // TODO better way to do this? Tags?
        val scoreButtons = arrayOf(
                button_score_0,
                button_score_1,
                button_score_2,
                button_score_3,
                button_score_4,
                button_score_5,
                button_score_6,
                button_score_7,
                button_score_8,
                button_score_9,
                button_score_10,
                button_score_x
        )
        for (button in scoreButtons) {
            button.setOnClickListener {
                try {
                    end.addArrowToEnd(view.findViewById<Button>(button.id).text.toString())
                    updateEndStringAndTotal(view, end)
                }
                catch (e: IllegalStateException) {
                    Toast.makeText(context, getString(R.string.err_input_end__end_full), Toast.LENGTH_SHORT).show()
                }
            }
        }

        button_clear_end.setOnClickListener {
            end.clear()
            updateEndStringAndTotal(view, end)
        }

        button_backspace.setOnClickListener {
            try {
                end.removeLastArrowFromEnd()
                updateEndStringAndTotal(view, end)
            }
            catch (e: IllegalStateException) {
                Toast.makeText(context, getString(R.string.err_input_end__end_empty), Toast.LENGTH_SHORT).show()
            }
        }

        button_next_end.setOnClickListener {
            try {
                // Update database
                var highestArrowNumber = 0
                for (arrow in arrows) {
                    if (arrow.arrowNumber > highestArrowNumber) {
                        highestArrowNumber = arrow.arrowNumber
                    }
                }
                end.addArrowsToDatabase(args.archerRoundId, highestArrowNumber + 1, inputEndViewModel)
                updateEndStringAndTotal(view, end)
            }
            catch (e: IllegalStateException) {
                Toast.makeText(context, getString(R.string.err_input_end__end_not_full), Toast.LENGTH_SHORT).show()
            }
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            val action = InputEndFragmentDirections.actionInputEndFragmentToMainMenuFragment()
            view.findNavController().navigate(action)
        }
        callback.isEnabled = true
    }

    private fun updateEndStringAndTotal(view: View, end: End) {
        view.findViewById<TextView>(R.id.text_arrow_scores).text = end.toString()
        view.findViewById<TextView>(R.id.text_end_total).text = end.getScore().toString()
    }

    private fun updateRoundInfo(view: View) {
        view.findViewById<TextView>(R.id.text_table_score_1).text = roundTotal().toString()
        view.findViewById<TextView>(R.id.text_table_arrow_count_1).text = arrows.size.toString()

        val roundIndicatorSection = view.findViewById<LinearLayout>(R.id.layout_round_indicator)
        if (args.showRemaining && distances.size == arrowCounts.size && distanceUnit.isNotBlank()) {
            roundIndicatorSection.visibility = View.VISIBLE
            val roundIndicators = getRemainingArrowsPerDistance(
                    arrows.size, arrowCounts, distances, distanceUnit,
                    view.resources.getString(R.string.input_end__round_indicator_at)
            )
            val label = view.findViewById<TextView>(R.id.text_round_indicator_label)
            val large = view.findViewById<TextView>(R.id.text_round_indicator_large)
            val small = view.findViewById<TextView>(R.id.text_round_indicator_small)
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
        else {
            roundIndicatorSection.visibility = View.GONE
        }
    }

    private fun roundTotal(): Int {
        var total = 0
        for (arrow in arrows) {
            total += arrow.score
        }
        return total
    }
}

