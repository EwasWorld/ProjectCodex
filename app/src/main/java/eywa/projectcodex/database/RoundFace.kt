package eywa.projectcodex.database

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.database.rounds.Round

const val WORCESTER_DEFAULT_ID = 22

enum class RoundFace(
        @StringRes val text: Int,
        @StringRes val help: Int,
        val shouldShow: (Round) -> Boolean,
) {
    /**
     * Standard single face consisting of all scoring rings appropriate for the round
     */
    FULL(
            text = R.string.round_face_full,
            help = R.string.round_face_full,
            shouldShow = { true },
    ),

    /**
     * Three faces (usually aligned vertically) consisting of scoring rings 10 through 6 inclusive.
     * One arrow must be put in each face. 10-zone scoring only
     */
    TRIPLE(
            text = R.string.round_face_triple,
            help = R.string.round_face_triple_help,
            shouldShow = { it.defaultRoundId != WORCESTER_DEFAULT_ID },
    ),

    /**
     * Scoring rings 10 through 6 inclusive (same as triple). 10-zone scoring only
     */
    HALF(
            text = R.string.round_face_five,
            help = R.string.round_face_five_help,
            shouldShow = { it.defaultRoundId != WORCESTER_DEFAULT_ID },
    ),

    /**
     * Scoring rings 10 through 5 inclusive. 10-zone scoring only
     */
    FITA_SIX(
            text = R.string.round_face_six,
            help = R.string.round_face_six_help,
            shouldShow = { it.defaultRoundId != WORCESTER_DEFAULT_ID },
    ),

    /**
     * Five faces (in a dice pattern) consisting scoring rings 4 and 5 rings of a Worcester only.
     * One arrow must be put in each face
     */
    WORCESTER_FIVE(
            text = R.string.round_face_worcester,
            help = R.string.round_face_worcester_help,
            shouldShow = { it.defaultRoundId == WORCESTER_DEFAULT_ID },
    ),
}
