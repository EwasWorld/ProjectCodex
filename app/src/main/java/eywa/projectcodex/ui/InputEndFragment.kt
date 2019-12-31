package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import eywa.projectcodex.viewModels.InputEndViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_input_end.*

class InputEndFragment : Fragment() {
    private val args: InputEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private var allArrows = emptyList<ArrowValue>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(activity!!.application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.allArrows.observe(viewLifecycleOwner, Observer { arrows ->
            arrows?.let {
                allArrows = arrows
                updateRoundTotals(view)
            }
        })

        val end =
            End(
                    6,
                    resources.getString(R.string.end_to_string_arrow_placeholder),
                    resources.getString(R.string.end_to_string_arrow_deliminator)
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
                    Toast.makeText(context, resources.getString(R.string.err_end_full), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, resources.getString(R.string.err_end_empty), Toast.LENGTH_SHORT).show()
            }
        }

        button_next_end.setOnClickListener {
            try {
                // Update database
                var highestArrowNumber = 0
                for (arrow in allArrows) {
                    if (arrow.arrowNumber > highestArrowNumber) {
                        highestArrowNumber = arrow.arrowNumber
                    }
                }
                end.addArrowsToDatabase(args.archerRoundId, highestArrowNumber + 1, inputEndViewModel)
                updateEndStringAndTotal(view, end)
            }
            catch (e: IllegalStateException) {
                Toast.makeText(context, resources.getString(R.string.err_end_not_full), Toast.LENGTH_SHORT).show()
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

    private fun updateRoundTotals(view: View) {
        view.findViewById<TextView>(R.id.text_table_score_1).text = roundTotal().toString()
        view.findViewById<TextView>(R.id.text_table_arrow_count_1).text = allArrows.size.toString()
    }

    private fun roundTotal(): Int {
        var total = 0
        for (arrow in allArrows) {
            total += arrow.score
        }
        return total
    }
}

