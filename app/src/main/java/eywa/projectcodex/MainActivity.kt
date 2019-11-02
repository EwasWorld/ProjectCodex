package eywa.projectcodex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val editEnd = EditEnd(resources)
        val endTotalTextView = findViewById<TextView>(R.id.text_end_total)

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
                    arrowScores.text = editEnd.rewriteScores(arrowScores.text.toString())
                } catch (e: NullPointerException) {
                    Toast.makeText(this, resources.getString(R.string.err_end_full), Toast.LENGTH_SHORT).show()
                }
            }
        }

        button_clear_end.setOnClickListener {
            val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
            arrowScores.text = resources.getString(R.string.empty_end)
            endTotalTextView.text = editEnd.getEndScore(arrowScores.text.toString()).toString()
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
    }
}
