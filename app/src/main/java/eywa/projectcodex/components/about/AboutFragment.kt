package eywa.projectcodex.components.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.updateDefaultRounds.asDisplayString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AboutFragment : Fragment() {
    private val aboutViewModel: AboutViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.about__title)

        // TODO Add 'Retry' buttons for TemporaryErrors
        aboutViewModel.viewModelScope.launch {
            aboutViewModel.updateDefaultRoundsTask.state.collectLatest {
                view.findViewById<TextView>(R.id.text_about__update_default_rounds_progress).text =
                        it.asDisplayString(resources)

                view.findViewById<TextView>(R.id.text_about__default_rounds_version).text =
                        (it?.databaseVersion ?: -1).toString()
            }
        }
        view.findViewById<TextView>(R.id.text_about__app_version).text = BuildConfig.VERSION_NAME
    }
}
