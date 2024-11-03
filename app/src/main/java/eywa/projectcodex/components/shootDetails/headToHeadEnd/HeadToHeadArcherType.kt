package eywa.projectcodex.components.shootDetails.headToHeadEnd

import eywa.projectcodex.common.utils.ResOrActual

enum class HeadToHeadArcherType(
        val text: ResOrActual<String>,
        /**
         * The archer's team (including the archer themselves)
         */
        val isTeam: Boolean,
) {
    /**
     * The archer alone
     */
    SELF_ARROW(
            text = ResOrActual.Actual("Self"),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize
    },

    /**
     * The archer's team mates (not including the archer themselves)
     */
    TEAM_MATE_ARROW(
            text = ResOrActual.Actual("Team"),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * (teamSize - 1)
    },

    /**
     * The archer's team (including the archer themselves)
     */
    TEAM_ARROW(
            text = ResOrActual.Actual("Team"),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * teamSize
    },

    /**
     * The opponent(s)
     */
    OPPONENT_ARROW(
            text = ResOrActual.Actual("Opponent"),
            isTeam = false,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * teamSize
    },

    /**
     * Set points for the archer's team (including the archer themselves)
     */
    TEAM_POINTS(
            text = ResOrActual.Actual("Points"),
            isTeam = false,
    )
    ;

    open fun expectedArrowCount(endSize: Int, teamSize: Int): Int = 1
}
