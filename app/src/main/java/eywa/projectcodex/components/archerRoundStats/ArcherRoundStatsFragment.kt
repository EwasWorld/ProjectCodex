package eywa.projectcodex.components.archerRoundStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.commonUtils.ArcherRoundBottomNavigationInfo
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_archer_round_stats.*


class ArcherRoundStatsFragment : Fragment(), ArcherRoundBottomNavigationInfo {
    private val args: ArcherRoundStatsFragmentArgs by navArgs()
    private lateinit var archerRoundStatsViewModel: ArcherRoundStatsViewModel

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
            text_archer_round_stats__hits.updateText(newText = arrows.count { it.score != 0 }.toString())
            text_archer_round_stats__score.updateText(newText = arrows.sumOf { it.score }.toString())
            text_archer_round_stats__golds.updateText(newText = arrows.count { GoldsType.TENS.isGold(it) }.toString())
            text_archer_round_stats__total_arrows.updateText(newText = arrows.size.toString())
        })
        text_archer_round_stats__round.updateText(newText = "") // TODO
        text_archer_round_stats__remaining_arrows.updateText(newText = "") // TODO
    }

    override fun getArcherRoundId(): Int {
        return args.archerRoundId
    }

    override fun isRoundComplete(): Boolean {
        // TODO_CURRENT
        return true
    }
}
