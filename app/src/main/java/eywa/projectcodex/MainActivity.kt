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
                scoreButtonPressed(findViewById<Button>(button.id).text.toString())
            }
        }

        button_clear_end.setOnClickListener {
            findViewById<TextView>(R.id.text_arrow_scores).text = resources.getString(R.string.empty_end)
            updateEndTotal()
        }

        button_backspace.setOnClickListener {
            val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
            val scores = arrowScores.text.toString().split("-").toMutableList()
            if (arrowScores.text[0] == '.') {
                Toast.makeText(this, "No arrows entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            for (i in (scores.size - 1) downTo 0) {
                if (scores[i] != ".") {
                    scores[i] = "."
                    break
                }
            }
            arrowScores.text = scores.joinToString("-")
            updateEndTotal()
        }
    }

    private fun scoreButtonPressed(buttonText: String) {
        val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
        if (!arrowScores.text.contains('.')) {
            Toast.makeText(this, "Arrows already added", Toast.LENGTH_SHORT).show()
            return
        }
        arrowScores.text = arrowScores.text.replaceFirst("\\.".toRegex(), buttonText)
        updateEndTotal()
//        rewriteScores()
    }

    private fun rewriteScores() {
        val arrowScores = findViewById<TextView>(R.id.text_arrow_scores)
        var scores = arrowScores.text.toString().split("-")
        arrowScores.text = scores.sorted().joinToString("-")
    }

    private fun updateEndTotal() {
        val arrowScores = findViewById<TextView>(R.id.text_arrow_scores).text.toString()
        val scores = arrowScores.split("-")
        var total = 0
        for (score in scores) {
            total += getScore(score)
        }
        findViewById<TextView>(R.id.text_end_total).text = total.toString()
    }

    private fun getScore(text: String): Int {
        return when (text) {
            "X" -> 10
            "." -> 0
            else -> Integer.parseInt(text)
        }
    }

    private fun addArrowToEnd(end: String, arrow: String) : String {
        if (!end.contains('.')) {
            Toast.makeText(this, "Arrows already added", Toast.LENGTH_SHORT).show()
            return end
        }
        return end.replaceFirst("\\.".toRegex(), arrow)
    }
}
