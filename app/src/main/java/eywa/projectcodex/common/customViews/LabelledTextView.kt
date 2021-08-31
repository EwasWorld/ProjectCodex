package eywa.projectcodex.common.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.getColourResource


class LabelledTextView : LinearLayout {
    private lateinit var mainTextView: TextView
    private lateinit var labelTextView: TextView

    constructor(context: Context) : super(context) {
        initialise(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialise(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialise(context, attrs)
    }

    private fun initialise(context: Context, attrs: AttributeSet?) {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.LabelledTextView)
        val label = styledAttributes.getString(R.styleable.LabelledTextView_label) ?: ""
        val text = styledAttributes.getString(R.styleable.LabelledTextView_text) ?: ""
        val textSize = styledAttributes.getDimension(
                R.styleable.LabelledTextView_text_size,
                resources.getDimension(R.dimen.small_text_size)
        )
        val textColour = styledAttributes.getColor(
                R.styleable.LabelledTextView_text_color,
                getColourResource(resources, R.color.white, context.theme)
        )
        val displayColon = styledAttributes.getBoolean(R.styleable.LabelledTextView_display_colon, true)
        styledAttributes.recycle()

        val layout = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.labelled_text, this, true) as LinearLayout
        mainTextView = layout.findViewById(R.id.labelled_text__text)
        labelTextView = layout.findViewById(R.id.labelled_text__label)
        val colonTextView = layout.findViewById<TextView>(R.id.labelled_text__colon)

        for (textView in listOf(mainTextView, labelTextView, colonTextView)) {
            // Not sure why setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize) doesn't work...
            textView.textSize = textSize / resources.displayMetrics.scaledDensity
            textView.setTextColor(textColour)
        }

        labelTextView.text = label
        colonTextView.visibility = if (displayColon) VISIBLE else GONE
        updateText(text)
    }

    fun updateText(newText: String) {
        mainTextView.text = newText
        invalidate()
        requestLayout()
    }

    fun updateLabel(newText: String) {
        labelTextView.text = newText
        invalidate()
        requestLayout()
    }
}
