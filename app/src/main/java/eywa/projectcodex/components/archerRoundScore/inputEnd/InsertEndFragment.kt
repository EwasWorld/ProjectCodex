package eywa.projectcodex.components.archerRoundScore.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.components.archerRoundScore.inputEnd.subFragments.EndInputsFragment
import eywa.projectcodex.components.archeryObjects.End
import eywa.projectcodex.components.commonUtils.ActionBarHelp
import eywa.projectcodex.components.commonUtils.ToastSpamPrevention
import eywa.projectcodex.components.commonUtils.ViewModelFactory
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.exceptions.UserException
import kotlinx.android.synthetic.main.fragment_insert_end.*


class InsertEndFragment : Fragment(), ActionBarHelp {
    private val args: InsertEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
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

        endInputsFragment =
                childFragmentManager.findFragmentById(R.id.fragment_insert_end__end_inputs)!! as EndInputsFragment
        endInputsFragment.showResetButton = true

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(requireActivity().application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.arrows.observe(viewLifecycleOwner, Observer { arrowsJava ->
            arrowsJava?.let { arrows ->
                this.arrows = arrows

                endInputsFragment.end = End(
                        args.endSize,
                        getString(R.string.end_to_string_arrow_placeholder),
                        getString(R.string.end_to_string_arrow_deliminator)
                )
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

        button_insert_end__cancel.setOnClickListener {
            view.findNavController().navigate(action)
        }

        button_insert_end__complete.setOnClickListener {
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

    override fun getHelpShowcases(): List<ActionBarHelp.HelpShowcaseItem> {
        return listOf(
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_insert_end__complete,
                        getString(R.string.help_edit_end__complete_title),
                        getString(R.string.help_edit_end__complete_body)
                ),
                ActionBarHelp.HelpShowcaseItem(
                        R.id.button_insert_end__cancel,
                        getString(R.string.help_edit_end__cancel_title),
                        getString(R.string.help_edit_end__cancel_body)
                )
        )
    }

    override fun getHelpPriority(): Int? {
        return null
    }
}
