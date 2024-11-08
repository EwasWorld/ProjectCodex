package eywa.projectcodex.components.shootDetails.headToHeadEnd

import eywa.projectcodex.R
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
    SELF(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_self),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize
    },

    /**
     * The archer's team mates (not including the archer themselves)
     */
    TEAM_MATE(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_team_mate),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * (teamSize - 1)
    },

    /**
     * The archer's team (including the archer themselves)
     */
    TEAM(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_team),
            isTeam = true,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * teamSize
    },

    /**
     * The opponent(s)
     */
    OPPONENT(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_opponent),
            isTeam = false,
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * teamSize
    },

    /**
     * Set points for the archer's team (including the archer themselves)
     * Note this always uses default points within the db, not shoot off points, which are adjusted for later
     * TODO
     */
    RESULT(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_result),
            isTeam = false,
    )
    ;

    open fun expectedArrowCount(endSize: Int, teamSize: Int): Int = 1
}
