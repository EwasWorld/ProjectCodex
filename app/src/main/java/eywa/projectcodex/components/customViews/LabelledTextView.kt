package eywa.projectcodex.components.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import eywa.projectcodex.R


class LabelledTextView : LinearLayout {
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
        val displayColon = styledAttributes.getBoolean(R.styleable.LabelledTextView_display_colon, true)
        styledAttributes.recycle()

        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.labelled_text, this, true) as LinearLayout

        updateText(text, label, displayColon)
    }

    fun updateText(newText: String? = null, label: String? = null, displayColon: Boolean = true) {
        val layout = getChildAt(0)

        if (newText != null) layout.findViewById<TextView>(R.id.labelled_text__text).text = newText
        if (label != null) layout.findViewById<TextView>(R.id.labelled_text__label).text = label
        layout.findViewById<TextView>(R.id.labelled_text__colon).visibility = if (displayColon) VISIBLE else GONE

        invalidate()
        requestLayout()
    }
}

