package eywa.projectcodex.components.newScore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import kotlinx.coroutines.launch

class NewScoreFragment : Fragment(), ActionBarHelp {
    private val args: NewScoreFragmentArgs by navArgs()

    private val viewModel: NewScoreViewModel by viewModels()
    private val screen = NewScoreScreen()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.handle(
                NewScoreIntent.Initialise(
                        roundBeingEditedId = args.archerRoundId.takeIf { it != -1 },
                )
        )

        lifecycleScope.launch {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is NewScoreEffect.NavigateToInputEnd -> findNavController().navigate(
                            NewScoreFragmentDirections.actionNewScoreFragmentToInputEndFragment(effect.archerRoundId)
                    )
                    NewScoreEffect.PopBackstack -> findNavController().popBackStack()
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                screen.ComposeContent(viewModel.state) { viewModel.handle(it) }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(
                if (viewModel.state.isEditing) R.string.create_round__title else R.string.create_round__edit_title
        )
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        val mainList = mutableListOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__date)
                        .setHelpTitleId(R.string.help_create_round__date_title)
                        .setHelpBodyId(R.string.help_create_round__date_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .setShapePadding(0)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.layout_create_round__round)
                        .setHelpTitleId(R.string.help_create_round__round_title)
                        .setHelpBodyId(R.string.help_create_round__round_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .setShapePadding(0)
                        .build()
        )
        if (true) {
//        if (requireView().findViewById<LinearLayout>(R.id.layout_create_round__round_sub_type).visibility == View.VISIBLE) {
            mainList.addAll(
                    listOf(
                            ViewHelpShowcaseItem.Builder()
//                                    .setViewId(R.id.layout_create_round__round_sub_type)
                                    .setHelpTitleId(R.string.help_create_round__sub_round_title)
                                    .setHelpBodyId(R.string.help_create_round__sub_round_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ViewHelpShowcaseItem.Builder()
//                                    .setViewId(R.id.text_create_round__arrow_count_indicator)
                                    .setHelpTitleId(R.string.help_create_round__arrow_count_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__arrow_count_indicator_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build(),
                            ViewHelpShowcaseItem.Builder()
//                                    .setViewId(R.id.text_create_round__distance_indicator)
                                    .setHelpTitleId(R.string.help_create_round__distance_indicator_title)
                                    .setHelpBodyId(R.string.help_create_round__distance_indicator_body)
                                    .setShape(HelpShowcaseItem.Shape.OVAL)
                                    .setShapePadding(0)
                                    .build()
                    )
            )
        }
        mainList.add(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_create_round__submit)
                        .setHelpTitleId(R.string.help_create_round__submit_title)
                        .setHelpBodyId(R.string.help_create_round__submit_body)
                        .setShape(HelpShowcaseItem.Shape.OVAL)
                        .build()
        )
        return mainList
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
