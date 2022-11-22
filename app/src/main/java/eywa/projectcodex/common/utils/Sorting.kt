package eywa.projectcodex.common.utils

object Sorting {
    /**
     * Groups strings into numerical and non-numerical sequences then compares those.
     *
     * The list: ["1", "10", "2"] would be sorted to ["1", "2", "10"]
     */
    val NumericalStringSort = Comparator { string0: String, string1: String ->
        fun String.sliced(): List<TextOrNumber> {
            val slicedList = mutableListOf<TextOrNumber>()

            var currentIsText = false
            var current = ""
            forEach { char ->
                val charIsText = !char.isDigit()
                if (current.isBlank()) {
                    current = "$char"
                    currentIsText = charIsText
                }
                else if (currentIsText == charIsText) {
                    current += char
                }
                else {
                    slicedList.add(TextOrNumber.fromString(current, currentIsText))
                    current = "$char"
                    currentIsText = charIsText
                }
            }

            if (current.isNotBlank()) {
                slicedList.add(TextOrNumber.fromString(current, currentIsText))
            }
            return slicedList
        }

        val sliced0 = string0.sliced()
        val sliced1 = string1.sliced()

        var result = 0
        for (pair in sliced0.zip(sliced1)) {
            result = pair.first.compareTo(pair.second)
            if (result != 0) break
        }

        result.takeIf { it != 0 } ?: sliced0.size.compareTo(sliced1.size)
    }

    /**
     * Helper class for [NumericalStringSort]
     */
    private sealed class TextOrNumber : Comparable<TextOrNumber> {
        data class Text(val value: String) : TextOrNumber() {
            override fun compareTo(other: TextOrNumber) = when (other) {
                is Number -> value.compareTo(other.value.toString())
                is Text -> value.compareTo(other.value)
            }
        }

        data class Number(val value: Int) : TextOrNumber() {
            constructor(value: String) : this(Integer.parseInt(value))

            override fun compareTo(other: TextOrNumber) = when (other) {
                is Number -> value.compareTo(other.value)
                is Text -> value.toString().compareTo(other.value)
            }
        }

        companion object {
            fun fromString(value: String, isText: Boolean) = if (isText) Text(value) else Number(value)
        }
    }
}