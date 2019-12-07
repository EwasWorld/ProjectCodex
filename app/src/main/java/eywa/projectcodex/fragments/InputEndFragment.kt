package eywa.projectcodex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.EditEnd
import eywa.projectcodex.R
import eywa.projectcodex.database.ScoresViewModel
import eywa.projectcodex.database.entities.ArrowValue
import kotlinx.android.synthetic.main.activity_add_end.*

class InputEndFragment : Fragment() {
    private val arrowsPerEnd = 6
    private lateinit var scoresViewModel: ScoresViewModel
    private var allArrows = emptyList<ArrowValue>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_add_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scoresViewModel = ViewModelProvider(this).get(ScoresViewModel::class.java)
        scoresViewModel.allArrows.observe(viewLifecycleOwner, Observer { arrows -> arrows?.let { allArrows = arrows } })

        val editEnd = EditEnd(resources)
        val endTotalTextView = view.findViewById<TextView>(R.id.text_end_total)

        // Set the place holder text for the arrow scores
        view.findViewById<TextView>(R.id.text_arrow_scores).text = editEnd.getEmptyEnd(arrowsPerEnd)

        button_score_pad.setOnClickListener {
            val action = InputEndFragmentDirections.actionInputEndFragmentToScorePadFragment()
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
                val buttonText = view.findViewById<Button>(button.id).text.toString()
                val arrowScores = view.findViewById<TextView>(R.id.text_arrow_scores)
                try {
                    arrowScores.text = editEnd.addArrowToEnd(arrowScores.text.toString(), buttonText)
                    endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
                }
                catch (e: NullPointerException) {
                    Toast.makeText(context, resources.getString(R.string.err_end_full), Toast.LENGTH_SHORT).show()
                }
            }
        }

        button_clear_end.setOnClickListener {
            clearEnd(view, editEnd)
        }

        button_backspace.setOnClickListener {
            val arrowScores = view.findViewById<TextView>(R.id.text_arrow_scores)
            try {
                arrowScores.text = editEnd.removeLastArrowFromEnd(arrowScores.text.toString())
            }
            catch (e: NullPointerException) {
                Toast.makeText(context, resources.getString(R.string.err_end_empty), Toast.LENGTH_SHORT).show()
            }
            endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
        }

        button_next_end.setOnClickListener {
            if (editEnd.getArrowCount(view.findViewById<TextView>(R.id.text_arrow_scores).text.toString()) != arrowsPerEnd) {
                Toast.makeText(context, resources.getString(R.string.err_end_not_full), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update database
            val arrowScores = view.findViewById<TextView>(R.id.text_arrow_scores)
            val arrowList = arrowScores.text.replace(resources.getString(R.string.arrow_value_m).toRegex(), "0")
                    .split(resources.getString(R.string.arrow_deliminator)).toMutableList()
            arrowList.removeAll { arrow -> arrow == resources.getString(R.string.arrow_deliminator) }
            var highestArrowNumber = 0
            for (arrow in allArrows) {
                if (arrow.arrowNumber > highestArrowNumber) {
                    highestArrowNumber = arrow.arrowNumber
                }
            }
            for (arrow in arrowList) {
                val isX = arrow == resources.getString(R.string.arrow_value_x)
                val arrowScore = when (isX) {
                    true -> 10
                    false -> Integer.parseInt(arrow)
                }
                scoresViewModel.insert(ArrowValue(1, ++highestArrowNumber, arrowScore, isX))
            }

            // Update archer's score
            val endTotal = Integer.parseInt(endTotalTextView.text.toString())
            val archerTotal = view.findViewById<TextView>(R.id.text_table_score_1)
            archerTotal.text = (Integer.parseInt(archerTotal.text.toString()) + endTotal).toString()

            // Update archer's arrow count
            val arrowCount = view.findViewById<TextView>(R.id.text_table_arrow_count_1)
            arrowCount.text = (arrowsPerEnd + Integer.parseInt(arrowCount.text.toString())).toString()

            clearEnd(view, editEnd)
        }
    }

    private fun clearEnd(view: View, editEnd: EditEnd) {
        val endTotalTextView = view.findViewById<TextView>(R.id.text_end_total)
        val arrowScores = view.findViewById<TextView>(R.id.text_arrow_scores)
        arrowScores.text = editEnd.getEmptyEnd(arrowsPerEnd)
        endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
    }
}

