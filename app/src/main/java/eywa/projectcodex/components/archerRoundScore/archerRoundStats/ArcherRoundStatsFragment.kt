package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.DateTimeFormat
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlinx.android.synthetic.main.fragment_archer_round_stats.*


class ArcherRoundStatsFragment : Fragment(), ArcherRoundBottomNavigationInfo {
    private val args: ArcherRoundStatsFragmentArgs by navArgs()
    private lateinit var archerRoundStatsViewModel: ArcherRoundStatsViewModel
    private var arrows: List<ArrowValue>? = null
    private var round: Round? = null
    private var arrowCounts: List<RoundArrowCount>? = null
    private var roundDistances: List<RoundDistance>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_archer_round_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.archer_round_stats__title)

        archerRoundStatsViewModel = ViewModelProvider(this, ViewModelFactory {
            ArcherRoundStatsViewModel(requireActivity().application, args.archerRoundId)
        }).get(ArcherRoundStatsViewModel::class.java)
        archerRoundStatsViewModel.arrows.observe(viewLifecycleOwner, Observer { dbArrows ->
            if (dbArrows.isNullOrEmpty()) {
                return@Observer
            }
            arrows = dbArrows
            setRemainingArrowsText()

            val hits = dbArrows.count { it.score != 0 }
            val totalArrows = dbArrows.size
            text_archer_round_stats__hits.updateText("$hits" + if (totalArrows > hits) " (of $totalArrows)" else "")
            text_archer_round_stats__score.updateText(dbArrows.sumOf { it.score }.toString())
            text_archer_round_stats__golds.updateText(dbArrows.count { GoldsType.TENS.isGold(it) }.toString())
            calculateHandicap()
        })
        archerRoundStatsViewModel.archerRoundWithRoundInfo.observe(viewLifecycleOwner, { info ->
            info?.let { archerRoundWithInfo ->
                val archerRound = archerRoundWithInfo.archerRound

                text_archer_round_stats__date.updateText(
                        DateTimeFormat.LONG_DATE_TIME_FORMAT.format(archerRound.dateShot)
                )

                if (archerRound.roundId == null) {
                    text_archer_round_stats__round.visibility = View.GONE
                    text_archer_round_stats__remaining_arrows.visibility = View.GONE
                    return@observe
                }

                round = archerRoundWithInfo.round!!
                text_archer_round_stats__round.updateText(archerRoundWithInfo.roundSubTypeName ?: round!!.displayName)
                archerRoundStatsViewModel.getArrowCountsForRound(round!!.roundId)
                        .observe(viewLifecycleOwner, { counts ->
                            counts?.let { dbArrowCounts ->
                                arrowCounts = dbArrowCounts
                                setRemainingArrowsText()
                                calculateHandicap()
                            }
                        })
                archerRoundStatsViewModel.getDistancesForRound(round!!.roundId, archerRound.roundSubTypeId)
                        .observe(viewLifecycleOwner, {
                            roundDistances = it
                            calculateHandicap()
                        })

                text_archer_round_stats__round.visibility = View.VISIBLE
                text_archer_round_stats__remaining_arrows.visibility = View.VISIBLE
                calculateHandicap()
            }
        })
    }

    private fun calculateHandicap(innerTenArcher: Boolean = false) {
        if (round == null || arrowCounts.isNullOrEmpty() || roundDistances.isNullOrEmpty() || arrows.isNullOrEmpty()) {
            text_archer_round_stats__handicap.visibility = View.GONE
            text_archer_round_stats__predicted_score.visibility = View.GONE
            return
        }

        check(arrowCounts!!.all { it.roundId == round!!.roundId }) { "Arrow counts id mismatch" }
        check(roundDistances!!.all { it.roundId == round!!.roundId }) { "Distances id mismatch" }
        check(roundDistances!!.distinctBy { it.subTypeId }.size == 1) { "Multiple distance subtypes" }
        check(roundDistances!!.size == arrowCounts!!.size) { "Distances arrow counts size mismatch" }

        val handicap = Handicap.getHandicapForRound(
                round!!,
                arrowCounts!!,
                roundDistances!!,
                arrows!!.sumOf { it.score },
                innerTenArcher,
                arrows!!.count()
        )
        text_archer_round_stats__handicap.updateText(handicap.toString())
        text_archer_round_stats__predicted_score.updateText(
                Handicap.getScoreForRound(
                        round!!,
                        arrowCounts!!,
                        roundDistances!!,
                        handicap,
                        innerTenArcher,
                        null
                ).toString()
        )
        text_archer_round_stats__handicap.visibility = View.VISIBLE
        text_archer_round_stats__predicted_score.visibility = View.VISIBLE
    }

    private fun setRemainingArrowsText() {
        val roundArrowCounts = arrowCounts?.sumOf { it.arrowCount }
        if (roundArrowCounts == null || roundArrowCounts <= 0) {
            return
        }
        var remainingText = (roundArrowCounts - (arrows?.count() ?: 0)).toString()
        if (remainingText == "0") {
            remainingText = resources.getString(R.string.input_end__round_complete)
        }
        text_archer_round_stats__remaining_arrows.updateText(remainingText)
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