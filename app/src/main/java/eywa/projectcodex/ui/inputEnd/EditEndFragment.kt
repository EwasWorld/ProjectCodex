package eywa.projectcodex.ui.inputEnd

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import eywa.projectcodex.Log
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.exceptions.UserException
import eywa.projectcodex.logic.End
import eywa.projectcodex.viewModels.InputEndViewModel
import eywa.projectcodex.viewModels.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_edit_end.*


class EditEndFragment : Fragment() {
    private val args: EditEndFragmentArgs by navArgs()
    private lateinit var inputEndViewModel: InputEndViewModel
    private lateinit var endInputsFragment: EndInputsFragment
    private var arrows = emptyList<ArrowValue>()

    companion object {
        private const val LOG_TAG = "EditEndFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_edit_end, container, false)
        view.findViewById<TextView>(R.id.text_edit_end__title).text =
                getString(R.string.edit_end__edit_info).format((args.firstArrowId - 1) / args.endSize + 1)

        endInputsFragment =
                childFragmentManager.findFragmentById(R.id.fragment_edit_end__end_inputs)!! as EndInputsFragment
        endInputsFragment.showResetButton = true

        inputEndViewModel = ViewModelProvider(this, ViewModelFactory {
            InputEndViewModel(requireActivity().application, args.archerRoundId)
        }).get(InputEndViewModel::class.java)
        inputEndViewModel.arrows.observe(viewLifecycleOwner, Observer { arrowsJava ->
            arrowsJava?.let { arrows ->
                this.arrows = arrows

                val originalEnd = this.arrows.filter {
                    it.arrowNumber >= args.firstArrowId && it.arrowNumber < args.firstArrowId + args.endSize
                }
                endInputsFragment.end = End(
                        originalEnd,
                        getString(R.string.end_to_string_arrow_placeholder),
                        getString(R.string.end_to_string_arrow_deliminator)
                )
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.edit_end__title)
        val action = EditEndFragmentDirections.actionEditEndFragmentToScorePadFragment(
                args.endSize, args.archerRoundId
        )

        button_edit_end__cancel.setOnClickListener {
            view.findNavController().navigate(action)
        }

        button_edit_end__complete.setOnClickListener {
            try {
                // Update database
                var highestArrowNumber = 0
                for (arrow in arrows) {
                    if (arrow.arrowNumber > highestArrowNumber) {
                        highestArrowNumber = arrow.arrowNumber
                    }
                }
                endInputsFragment.end.addArrowsToDatabase(
                        args.archerRoundId, highestArrowNumber + 1, inputEndViewModel
                )
                // TODO Revert to `activity?.onBackPressed()` if I can work out how to make this and cancel both work
                //    with the table refreshing bug (above as well)
                view.findNavController().navigate(action)
            }
            catch (e: UserException) {
                Toast.makeText(context, e.getMessage(resources), Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception) {
                if (!e.message.isNullOrBlank()) Log.e(LOG_TAG, e.message!!)
                Toast.makeText(context, getString(R.string.err__internal_error), Toast.LENGTH_SHORT).show()
            }
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            view.findNavController().navigate(action)
        }
        callback.isEnabled = true
    }
}
