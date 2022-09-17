package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.customViews.LabelledTextView
import eywa.projectcodex.common.utils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance


class ArcherRoundStatsFragment : Fragment(), ArcherRoundBottomNavigationInfo {
    companion object {
        private const val LOG_TAG = "ArcherRoundStatsFragment"
    }

    private val args: ArcherRoundStatsFragmentArgs by navArgs()
    private val archerRoundStatsViewModel: ArcherRoundScoreViewModel by activityViewModels()
    private var arrows: List<ArrowValue>? = null
    private var round: Round? = null
    private var arrowCounts: List<RoundArrowCount>? = null
    private var roundDistances: List<RoundDistance>? = null
    private var roundName: String? = null
    private var archerRound: ArcherRound? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_archer_round_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomLogger.customLogger.d(LOG_TAG, "onViewCreated")
        setFragmentTitle()

        archerRoundStatsViewModel.archerRoundIdMutableLiveData.postValue(args.archerRoundId)
        archerRoundStatsViewModel.arrowsForRound.observe(viewLifecycleOwner, Observer { dbArrows ->
            if (dbArrows.isNullOrEmpty()) {
                return@Observer
            }
            arrows = dbArrows
            setRemainingArrowsText()

            val hits = dbArrows.count { it.score != 0 }
            val totalArrows = dbArrows.size
            view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__hits)
                    .updateText("$hits" + if (totalArrows > hits) " (of $totalArrows)" else "")
            view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__score)
                    .updateText(dbArrows.sumOf { it.score }.toString())
            setGolds()
            calculateHandicapAndPredictedScore()
        })
        archerRoundStatsViewModel.archerRoundWithInfo.observe(viewLifecycleOwner) { info ->
            info?.let { archerRoundWithInfo ->
                val archerRound = archerRoundWithInfo.archerRound
                this.archerRound = archerRound

                view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__date).updateText(
                        DateTimeFormat.LONG_DATE_TIME.format(archerRound.dateShot)
                )

                if (archerRound.roundId == null) {
                    view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__round).visibility = View.GONE
                    view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__remaining_arrows).visibility =
                            View.GONE
                    return@observe
                }
                roundName = archerRoundWithInfo.displayName
                setFragmentTitle()

                // TODO Better null safety in this class :O
                if (archerRoundWithInfo.round == null) return@observe
                round = archerRoundWithInfo.round!!
                view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__round)
                        .updateText(archerRoundWithInfo.roundSubTypeName ?: round!!.displayName)
                archerRoundStatsViewModel.getArrowCountsForRound(round!!.roundId)
                        .observe(viewLifecycleOwner) { counts ->
                            counts?.let { dbArrowCounts ->
                                arrowCounts = dbArrowCounts
                                setRemainingArrowsText()
                                calculateHandicapAndPredictedScore()
                            }
                        }
                archerRoundStatsViewModel.getDistancesForRound(round!!.roundId, archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner) {
                            roundDistances = it
                            calculateHandicapAndPredictedScore()
                        }

                view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__round).visibility = View.VISIBLE
                view.findViewById<LabelledTextView>(R.id.text_archer_round_stats__remaining_arrows).visibility =
                        View.VISIBLE
                setGolds()
                calculateHandicapAndPredictedScore()
            }
        }
    }

    private fun setFragmentTitle() {
        activity?.title = roundName ?: getString(R.string.archer_round_stats__title)
    }

    private fun setGolds() {
        if (arrows.isNullOrEmpty()) {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__golds)
                    .updateLabel(resources.getString(GoldsType.NINES.longStringId))
            return
        }
        val goldsType = if (round != null) GoldsType.getGoldsType(round!!) else GoldsType.defaultGoldsType
        requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__golds)
                .updateLabel(resources.getString(goldsType.longStringId))
        requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__golds)
                .updateText(arrows!!.count { goldsType.isGold(it) }.toString())
    }

    private fun calculateHandicapAndPredictedScore(innerTenArcher: Boolean = false) {
        if (round == null || arrowCounts.isNullOrEmpty() || roundDistances.isNullOrEmpty() || arrows.isNullOrEmpty()) {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__handicap).visibility = View.GONE
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__predicted_score).visibility =
                    View.GONE
            return
        }

        check(arrowCounts!!.all { it.roundId == round!!.roundId }) { "Arrow counts id mismatch" }
        check(roundDistances!!.all { it.roundId == round!!.roundId }) { "Distances id mismatch" }
        check(roundDistances!!.distinctBy { it.subTypeId }.size == 1) { "Multiple distance subtypes" }
        check(roundDistances!!.size == arrowCounts!!.size) { "Distances arrow counts size mismatch" }

        /*
         * Calculate
         */
        var handicap: Int? = null
        var predictedScore: Int? = null
        try {
            handicap = Handicap.getHandicapForRound(
                    round!!,
                    arrowCounts!!,
                    roundDistances!!,
                    arrows!!.sumOf { it.score },
                    innerTenArcher,
                    arrows!!.count()
            )
            // No need to predict a score if round is already completed
            if (!isRoundComplete()) {
                predictedScore = Handicap.getScoreForRound(
                        round!!, arrowCounts!!, roundDistances!!, handicap, innerTenArcher, null
                )
            }
        }
        catch (e: IllegalArgumentException) {
            CustomLogger.customLogger.e(
                    ViewScoresEntry.LOG_TAG,
                    "Failed to get handicap for round with id ${args.archerRoundId} (date shot: %s)"
                            .format(DateTimeFormat.SHORT_DATE_TIME.format(archerRound!!.dateShot))
            )
            CustomLogger.customLogger.e(ViewScoresEntry.LOG_TAG, "Handicap Error: " + e.message)
        }

        /*
         * Display
         */
        if (handicap != null) {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__handicap)
                    .updateText(handicap.toString())
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__handicap).visibility =
                    View.VISIBLE
        }
        else {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__handicap).visibility = View.GONE
        }
        if (predictedScore != null) {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__predicted_score)
                    .updateText(predictedScore.toString())
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__predicted_score).visibility =
                    View.VISIBLE
        }
        else {
            requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__predicted_score).visibility =
                    View.GONE
        }
    }

    private fun setRemainingArrowsText() {
        val roundArrowCounts = arrowCounts?.sumOf { it.arrowCount }
        if (roundArrowCounts == null || roundArrowCounts <= 0) {
            return
        }
        val remainingArrows = (roundArrowCounts - (arrows?.count() ?: 0))
        val remainingText = when {
            remainingArrows == 0 -> resources.getString(R.string.input_end__round_complete)
            remainingArrows < 0 -> (remainingArrows * -1).toString()
            else -> remainingArrows.toString()
        }
        val labelId = when {
            remainingArrows >= 0 -> R.string.archer_round_stats__remaining_arrows
            else -> R.string.archer_round_stats__surplus_arrows
        }

        requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__remaining_arrows)
                .updateLabel(resources.getString(labelId))
        requireView().findViewById<LabelledTextView>(R.id.text_archer_round_stats__remaining_arrows)
                .updateText(remainingText)
    }

    override fun getArcherRoundId(): Int {
        return args.archerRoundId
    }

    override fun isRoundComplete(): Boolean {
        val roundArrowCounts = arrowCounts?.sumOf { it.arrowCount }
        if (roundArrowCounts == null || roundArrowCounts <= 0) {
            return false
        }
        return roundArrowCounts <= (arrows?.count() ?: 0)
    }
}
