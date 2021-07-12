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
import eywa.projectcodex.components.MainActivity
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.DateTimeFormat
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_archer_round_stats.*


class ArcherRoundStatsFragment : Fragment(), ArcherRoundBottomNavigationInfo {
    private val args: ArcherRoundStatsFragmentArgs by navArgs()
    private lateinit var archerRoundStatsViewModel: ArcherRoundStatsViewModel
    private var roundArrowCounts = 0
    private var arrowsShot = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_archer_round_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.archer_round_stats__title)

        archerRoundStatsViewModel = ViewModelProvider(this, ViewModelFactory {
            ArcherRoundStatsViewModel(requireActivity().application, args.archerRoundId)
        }).get(ArcherRoundStatsViewModel::class.java)
        archerRoundStatsViewModel.arrows.observe(viewLifecycleOwner, Observer { arrows ->
            if (arrows.isNullOrEmpty()) {
                return@Observer
            }
            arrowsShot = arrows.count()
            setRemainingArrowsText()

            val hits = arrows.count { it.score != 0 }
            val totalArrows = arrows.size
            text_archer_round_stats__hits.updateText(
                    newText = "$hits" + if (totalArrows > hits) "(of $totalArrows)" else ""
            )
            text_archer_round_stats__score.updateText(newText = arrows.sumOf { it.score }.toString())
            text_archer_round_stats__golds.updateText(newText = arrows.count { GoldsType.TENS.isGold(it) }.toString())
        })
        archerRoundStatsViewModel.archerRoundWithRoundInfo.observe(viewLifecycleOwner, { info ->
            info?.let { archerRoundWithInfo ->
                text_archer_round_stats__date.updateText(
                        newText = DateTimeFormat.LONG_DATE_TIME_FORMAT.format(archerRoundWithInfo.archerRound.dateShot)
                )

                if (archerRoundWithInfo.archerRound.roundId == null) {
                    text_archer_round_stats__round.visibility = View.GONE
                    text_archer_round_stats__remaining_arrows.visibility = View.GONE
                    return@observe
                }

                text_archer_round_stats__round.updateText(
                        newText = archerRoundWithInfo.roundSubTypeName ?: archerRoundWithInfo.round!!.displayName
                )
                archerRoundStatsViewModel.getArrowCountsForRound(archerRoundWithInfo.round!!.roundId)
                        .observe(viewLifecycleOwner, { counts ->
                            counts?.let { arrowCounts ->
                                roundArrowCounts = arrowCounts.sumOf { it.arrowCount }
                                setRemainingArrowsText()
                            }
                        })

                text_archer_round_stats__round.visibility = View.VISIBLE
                text_archer_round_stats__remaining_arrows.visibility = View.VISIBLE
            }
        })

        if (requireActivity() is MainActivity) {
            (requireActivity() as MainActivity).setCustomBackButtonCallback()
        }
    }

    private fun setRemainingArrowsText() {
        if (roundArrowCounts <= 0) {
            return
        }
        var remainingText = (roundArrowCounts - arrowsShot).toString()
        if (remainingText == "0") {
            remainingText = resources.getString(R.string.input_end__round_complete)
        }
        text_archer_round_stats__remaining_arrows.updateText(newText = remainingText)
    }

    override fun getArcherRoundId(): Int {
        return args.archerRoundId
    }

    override fun isRoundComplete(): Boolean {
        // No arrow counts if no round is being tracked
        if (roundArrowCounts <= 0) {
            return false
        }
        return roundArrowCounts <= arrowsShot
    }
}
