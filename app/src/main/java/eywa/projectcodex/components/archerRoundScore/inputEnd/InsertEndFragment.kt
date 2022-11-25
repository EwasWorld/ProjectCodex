package eywa.projectcodex.components.archerRoundScore.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.End
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ViewHelpShowcaseItem
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.ArrowInputsFragment
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.EndInputsFragment
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.exceptions.UserException


@AndroidEntryPoint
class InsertEndFragment : Fragment(), ActionBarHelp {
    private val args: InsertEndFragmentArgs by navArgs()
    private val inputEndViewModel: ArcherRoundScoreViewModel by activityViewModels()
    private lateinit var endInputsFragment: EndInputsFragment
    private var arrows = emptyList<ArrowValue>()

    companion object {
        private const val LOG_TAG = "InsertEndFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CustomLogger.customLogger.d(LOG_TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_insert_end, container, false)
        val insertEndAt = (args.firstArrowId - 1) / args.endSize + 1
        view.findViewById<TextView>(R.id.text_insert_end__title).text =
                if (insertEndAt == 1) {
                    getString(R.string.insert_end__info_at_start)
                }
                else {
                    resourceStringReplace(
                            getString(R.string.insert_end__info),
                            mapOf("end before" to (insertEndAt - 1).toString(), "end after" to insertEndAt.toString())
                    )
                }
        inputEndViewModel.archerRoundIdMutableLiveData.postValue(args.archerRoundId)

        endInputsFragment =
                childFragmentManager.findFragmentById(R.id.fragment_insert_end__end_inputs)!! as EndInputsFragment
        endInputsFragment.showResetButton = true

        inputEndViewModel.arrowsForRound.observe(viewLifecycleOwner, { arrowsJava ->
            arrowsJava?.let { arrows ->
                this.arrows = arrows

                endInputsFragment.end = End(
                        args.endSize,
                        getString(R.string.end_to_string_arrow_placeholder),
                        getString(R.string.end_to_string_arrow_deliminator)
                )
            }
        })
        inputEndViewModel.archerRoundWithInfo.observe(viewLifecycleOwner, { archerRoundInfo ->
            archerRoundInfo?.round?.let { round ->
                endInputsFragment.setScoreButtons(ArrowInputsFragment.ArrowInputsType.getType(round))
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CustomLogger.customLogger.d(LOG_TAG, "onViewCreated")
        activity?.title = getString(R.string.insert_end__title)
        val action = InsertEndFragmentDirections.actionInsertEndFragmentToScorePadFragment(
                args.archerRoundId
        )

        view.findViewById<Button>(R.id.button_insert_end__cancel).setOnClickListener {
            view.findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.button_insert_end__complete).setOnClickListener {
            try {
                val arrowValues = endInputsFragment.end.toArrowValues(args.archerRoundId, args.firstArrowId)
                inputEndViewModel.insertEnd(arrows, arrowValues).invokeOnCompletion {
                    // TODO Revert to `activity?.onBackPressed()` if I can work out how to make this and cancel both work
                    //    with the table refreshing bug (above as well)
                    view.findNavController().navigate(action)
                }
            }
            catch (e: UserException) {
                ToastSpamPrevention.displayToast(requireContext(), e.getUserMessage(resources))
            }
            catch (e: Exception) {
                if (!e.message.isNullOrBlank()) CustomLogger.customLogger.e(LOG_TAG, e.message!!)
                ToastSpamPrevention.displayToast(requireContext(), getString(R.string.err__internal_error))
            }
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            view.findNavController().navigate(action)
        }
        callback.isEnabled = true
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        return listOf(
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_insert_end__complete)
                        .setHelpTitleId(R.string.help_edit_end__complete_title)
                        .setHelpBodyId(R.string.help_edit_end__complete_body)
                        .build(),
                ViewHelpShowcaseItem.Builder()
                        .setViewId(R.id.button_insert_end__cancel)
                        .setHelpTitleId(R.string.help_edit_end__cancel_title)
                        .setHelpBodyId(R.string.help_edit_end__cancel_body)
                        .build()
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
