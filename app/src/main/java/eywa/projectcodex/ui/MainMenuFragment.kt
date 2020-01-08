package eywa.projectcodex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import eywa.projectcodex.R
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.title = getString(R.string.main_menu__title)
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_start_new_round.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToNewRoundFragment()
            view.findNavController().navigate(action)
        }

        button_view_rounds.setOnClickListener {
            val action = MainMenuFragmentDirections.actionMainMenuFragmentToViewRoundsFragment()
            view.findNavController().navigate(action)
        }
    }
}
