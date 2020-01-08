package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.viewModels.NewRoundViewModel
import kotlinx.android.synthetic.main.fragment_new_round.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class NewRoundFragment : Fragment() {
    private lateinit var newRoundViewModel: NewRoundViewModel
    private var initialId: Boolean = true
    private var maxId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_round, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.create_round__title)

        newRoundViewModel = ViewModelProvider(this).get(NewRoundViewModel::class.java)
        newRoundViewModel.maxId.observe(viewLifecycleOwner, Observer { id ->
            if (initialId) {
                initialId = false
            }
            else {
                maxId = id
                // When the new round entry has been added, open the input end dialog
                val action = NewRoundFragmentDirections.actionNewRoundFragmentToInputEndFragment(maxId)
                view.findNavController().navigate(action)
            }
        })

        button_create_round.setOnClickListener {
            // TODO Check date locales (I want to store in UTC)
            newRoundViewModel.insert(ArcherRound(0, Date(), 1, false))
        }
    }
}
