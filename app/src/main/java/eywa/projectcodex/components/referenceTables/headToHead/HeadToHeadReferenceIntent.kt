package eywa.projectcodex.components.referenceTables.headToHead

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class HeadToHeadReferenceIntent {
    data class ArcherRankChanged(val value: String?) : HeadToHeadReferenceIntent()
    data class OpponentRankChanged(val value: String?) : HeadToHeadReferenceIntent()
    data class TotalArchersChanged(val value: String?) : HeadToHeadReferenceIntent()
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : HeadToHeadReferenceIntent()
}
