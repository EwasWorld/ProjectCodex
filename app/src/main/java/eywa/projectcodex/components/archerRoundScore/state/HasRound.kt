package eywa.projectcodex.components.archerRoundScore.state

import eywa.projectcodex.database.rounds.Round

interface HasRound {
    fun getRound(): Round?
}