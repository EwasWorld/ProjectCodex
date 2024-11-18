package eywa.projectcodex.components.shootDetails.headToHeadEnd

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual

enum class HeadToHeadArcherType(
        val text: ResOrActual<String>,
        val selectorText: ResOrActual<String> = text,
        /**
         * The archer's team (including the archer themselves)
         */
        val isTeam: Boolean,
        val showForSelectorDialog: (isTeam: Boolean) -> Boolean = { true },
        val enabledOnSelectorDialog: (isTeam: Boolean, currentTypes: List<HeadToHeadArcherType>) -> Boolean =
                { _, _ -> true },
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
            showForSelectorDialog = { it },
    ) {
        override fun expectedArrowCount(endSize: Int, teamSize: Int): Int = endSize * (teamSize - 1)
    },

    /**
     * The archer's team (including the archer themselves)
     */
    TEAM(
            text = ResOrActual.StringResource(R.string.head_to_head_add_end__archer_team),
            isTeam = true,
            showForSelectorDialog = { it },
            enabledOnSelectorDialog = { _, types -> !types.contains(SELF) || !types.contains(TEAM_MATE) },
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
            enabledOnSelectorDialog = { isTeamMatch, types ->
                when {
                    !types.contains(OPPONENT) -> true
                    !isTeamMatch -> !types.contains(SELF)
                    types.contains(TEAM) -> false
                    else -> !types.contains(SELF) || !types.contains(TEAM_MATE)
                }
            },
    )
    ;

    open fun expectedArrowCount(endSize: Int, teamSize: Int): Int = 1
}
