package eywa.projectcodex.components.referenceTables.awards

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.components.referenceTables.awards.ui.AgbAwardsRow
import eywa.projectcodex.components.referenceTables.awards.ui.ClubAwardsRow
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundRepo

data class AwardsState(
        val allRounds: List<Round>? = null,
        val bow: ClassificationBow = ClassificationBow.RECURVE,
        val bowDropdownExpanded: Boolean = false,
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,
) {
    val club252Row: ClubAwardsRow?
        get() = getAwards(RoundRepo.CLUB_252_DEFAULT_ROUND_ID) { getClub252Awards(it).scores }

    val frostbiteRow: ClubAwardsRow?
        get() = getAwards(RoundRepo.FROSTBITE_DEFAULT_ROUND_ID) { getFrostbiteAwards(it).scores }

    val agbRows: List<AgbAwardsRow>?
        get() {
            if (allRounds == null) return null

            return AwardsUseCase
                    .getAgbAwards(bow, null)
                    .map { entry ->
                        val round = allRounds.find { it.defaultRoundId == entry.defaultRoundId }!!.displayName
                        AgbAwardsRow(entry.name, round, entry.scores)
                    }
        }

    private fun getAwards(
            defaultRoundId: Int,
            getAwards: AwardsUseCase.(ClassificationBow) -> List<Int>,
    ): ClubAwardsRow? {
        if (allRounds == null) return null
        val round = allRounds.find { it.defaultRoundId == defaultRoundId }!!

        return ClubAwardsRow(round.displayName, AwardsUseCase.getAwards(bow))
    }

    val hasLoaded = updateDefaultRoundsState.hasTaskFinished && allRounds != null
}
