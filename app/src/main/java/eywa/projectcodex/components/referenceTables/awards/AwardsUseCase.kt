package eywa.projectcodex.components.referenceTables.awards

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow

object AwardsUseCase {
    fun getAgbAwards(
            bow: ClassificationBow,
            defaultRoundId: Int?,
    ) = agbAwards
            .filter {
                (it.bow == null || it.bow == bow) && (defaultRoundId == null || it.defaultRoundId == defaultRoundId)
            }
            .distinctBy { it.name + it.defaultRoundId }

    fun getClub252Awards(bow: ClassificationBow) = Club252Award.backwardsMap[bow]!!
    fun getFrostbiteAwards(bow: ClassificationBow) = FrostbiteAward.backwardsMap[bow]!!

    /**
     * Source: https://oldoundlebowmenarcheryclub.com/wp-content/uploads/2018/03/Frostbite_Personal_Archery_Challenge.pdf
     */
    private val agbAwards = listOf(
            AwardsEntry(
                    "Rose",
                    "York Hereford",
                    1,
                    ClassificationBow.COMPOUND,
                    listOf(800, 900, 1000, 1100, 1200, 1250),
            ),
            AwardsEntry(
                    "Rose",
                    "York Hereford",
                    1,
                    ClassificationBow.RECURVE,
                    listOf(800, 900, 1000, 1100, 1200, 1250),
            ),
            AwardsEntry("Rose", "York Hereford", 1, ClassificationBow.LONGBOW, listOf(225, 300, 375, 450, 525, 600)),
            AwardsEntry("Rose", "York Hereford", 1, ClassificationBow.BAREBOW, listOf(500, 600, 700, 800, 900, 1000)),
            AwardsEntry("WA Star", "WA 1440", 8, null, listOf(1000, 1100, 1200, 1300, 1350, 1400)),
            AwardsEntry("WA Target", "WA 70m", 13, null, listOf(500, 550, 600, 650, 675, 700)),
            AwardsEntry("WA Target", "WA 900", 12, null, listOf(750, 800, 830, 860, 875, 890)),
            AwardsEntry("WA Target", "25m Indoor", 25, null, listOf(500, 525, 550, 575, 585, 595)),
            AwardsEntry("WA Target", "18m Indoor", 24, ClassificationBow.BAREBOW, listOf(480, 500, 520, 540, 550, 560)),
            AwardsEntry("WA Target", "18m Indoor", 24, null, listOf(500, 525, 550, 575, 585, 595)),
            AwardsEntry("WA Target", "WA 50m", 15, ClassificationBow.COMPOUND, listOf(500, 550, 600, 650, 675, 700)),
            AwardsEntry("WA Target", "WA 50m", 15, ClassificationBow.BAREBOW, listOf(480, 500, 550, 600, 625, 640)),
    )

    enum class Club252Award(
            val bow: ClassificationBow,
            val scores: List<Int>,
    ) {
        RECURVE(ClassificationBow.RECURVE, listOf(252, 252, 252, 252, 252, 252, 252, 252)),
        LONGBOW(ClassificationBow.LONGBOW, listOf(164, 164, 164, 164, 164, 164, 126, 101)),
        COMPOUND(ClassificationBow.COMPOUND, listOf(280, 280, 280, 280, 280, 280, 280, 280)),
        BAREBOW(ClassificationBow.BAREBOW, listOf(189, 189, 189, 189, 189, 189, 164, 139)),
        ;

        companion object {
            val backwardsMap = entries.associateBy { it.bow }
        }
    }

    enum class FrostbiteAward(
            val bow: ClassificationBow,
            val scores: List<Int>,
    ) {
        RECURVE(ClassificationBow.RECURVE, listOf(200, 225, 250, 275, 300, 315, 330, 340, 350, 355)),
        COMPOUND(ClassificationBow.COMPOUND, listOf(257, 275, 293, 309, 324, 333, 341, 347, 352, 357)),
        BAREBOW(ClassificationBow.BAREBOW, listOf(137, 165, 194, 226, 261, 285, 310, 329, 345, 354)),
        LONGBOW(ClassificationBow.LONGBOW, listOf(101, 128, 159, 193, 234, 264, 295, 319, 338, 351)),
        ;

        companion object {
            val backwardsMap = entries.associateBy { it.bow }
        }
    }
}

data class AwardsEntry(
        val name: String,
        val roundNameHint: String,
        val defaultRoundId: Int,
        val bow: ClassificationBow?,
        val scores: List<Int>,
)

enum class AwardColor(val title: ResOrActual<String>) {
    WHITE(ResOrActual.StringResource(R.string.awards__score_white_title)),
    BLACK(ResOrActual.StringResource(R.string.awards__score_black_title)),
    BLUE(ResOrActual.StringResource(R.string.awards__score_blue_title)),
    RED(ResOrActual.StringResource(R.string.awards__score_red_title)),
    GOLD(ResOrActual.StringResource(R.string.awards__score_gold_title)),
    PURPLE(ResOrActual.StringResource(R.string.awards__score_purple_title)),
    ORANGE(ResOrActual.StringResource(R.string.awards__score_orange_title)),
    GREEN(ResOrActual.StringResource(R.string.awards__score_green_title)),
}
