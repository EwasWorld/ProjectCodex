package eywa.projectcodex.components.viewScores.data

/**
 * Helper class to display a round on ViewScoresScreen
 */
data class ViewScoresRoundNameInfo(
        val displayName: String?,
        val strikethrough: Boolean = false,

        /**
         * If 2: prefix name with 'double'
         * If >2: prefix name with 'multiple'
         * Otherwise: no prefix
         */
        val identicalCount: Int = 1,
        val prefixWithAmpersand: Boolean = false,
        val semanticDisplayName: String?,
)
