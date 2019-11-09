package eywa.projectcodex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_end.*

class AddEndActivity : AppCompatActivity() {
    private val arrowsPerEnd = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_end)
        val editEnd = EditEnd(resources)
        val endTotalTextView = findViewById<TextView>(R.id.text_end_total)

        // Set the place holder text for the arrow scores
        findViewById<TextView>(R.id.text_arrow_scores).text = editEnd.getEmptyEnd(arrowsPerEnd)

        // TODO better way to do this? Tags?
        val scoreButtons = arrayOf(
            score_button_0,
            score_button_1,
            score_button_2,
            score_button_3,
            score_button_4,
            score_button_5,
            score_button_6,
            score_button_7,
            score_button_8,
            score_button_9,
            score_button_10,
            score_button_x
        )

        for (button in scoreButtons) {
            button.setOnClickListener {
                val buttonText = findViewById<Button>(button.id).text.toString()
                val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
                try {
                    arrowScores.text = editEnd.addArrowToEnd(arrowScores.text.toString(), buttonText)
                    endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
                } catch (e: NullPointerException) {
                    Toast.makeText(this, resources.getString(R.string.err_end_full), Toast.LENGTH_SHORT).show()
                }
            }
        }

        button_clear_end.setOnClickListener {
            clearEnd(editEnd)
        }

        button_backspace.setOnClickListener {
            val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
            try {
                arrowScores.text = editEnd.removeLastArrowFromEnd(arrowScores.text.toString())
            } catch (e: NullPointerException) {
                Toast.makeText(this, resources.getString(R.string.err_end_empty), Toast.LENGTH_SHORT).show()
            }
            endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
        }

        button_next_end.setOnClickListener {
            if (editEnd.getArrowCount(findViewById<TextView>(R.id.text_arrow_scores).text.toString()) != arrowsPerEnd) {
                Toast.makeText(this, resources.getString(R.string.err_end_not_full), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update archer's score
            val endTotal = Integer.parseInt(endTotalTextView.text.toString())
            val archerTotal = findViewById<TextView>(R.id.text_table_score_1)
            archerTotal.text = (Integer.parseInt(archerTotal.text.toString()) + endTotal).toString()

            // Update archer's arrow count
            val arrowCount = findViewById<TextView>(R.id.text_table_arrow_count_1)
            arrowCount.text = (arrowsPerEnd + Integer.parseInt(arrowCount.text.toString())).toString()

            clearEnd(editEnd)
        }
    }

    private fun clearEnd(editEnd: EditEnd) {
        val endTotalTextView = findViewById<TextView>(R.id.text_end_total)
        val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
        arrowScores.text = editEnd.getEmptyEnd(arrowsPerEnd)
        endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
    }
}
