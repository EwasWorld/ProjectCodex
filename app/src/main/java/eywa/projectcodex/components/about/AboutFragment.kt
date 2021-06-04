package eywa.projectcodex.components.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.R
import eywa.projectcodex.components.commonUtils.SharedPrefs
import eywa.projectcodex.components.commonUtils.SharedPrefs.Companion.getSharedPreferences
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds.UpdateTaskState
import kotlinx.android.synthetic.main.fragment_about.*


class AboutFragment : Fragment() {
    private lateinit var aboutViewModel: AboutViewModel
    private var defaultRoundsState = UpdateTaskState.NOT_STARTED
    private var defaultRoundsVersion = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.about__title)

        val sharedPreferences = requireActivity().getSharedPreferences()
        val roundVersionTextView = view.findViewById<TextView>(R.id.text_about__default_rounds_version)

        aboutViewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
        aboutViewModel.updateDefaultRoundsState.observe(viewLifecycleOwner, Observer { state ->
            defaultRoundsState = state
            if (state == UpdateTaskState.COMPLETE) {
                // Force re-retrieval as version has updated
                defaultRoundsVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
                roundVersionTextView.text = defaultRoundsVersion.toString()
            }
        })
        aboutViewModel.updateDefaultRoundsProgressMessage.observe(viewLifecycleOwner, Observer { message ->
            val progressText = message ?: resources.getString(
                    when (defaultRoundsState) {
                        UpdateTaskState.UP_TO_DATE -> R.string.about__update_default_rounds_up_to_date
                        UpdateTaskState.COMPLETE -> R.string.about__update_default_rounds_up_to_date
                        UpdateTaskState.NOT_STARTED -> R.string.about__update_default_rounds_not_started
                        UpdateTaskState.IN_PROGRESS -> R.string.about__update_default_rounds_in_progress
                        UpdateTaskState.ERROR -> R.string.err__internal_error
                    }
            )
            text_about__update_default_rounds_progress.text = progressText
        })

        view.findViewById<TextView>(R.id.text_about__app_version).text = BuildConfig.VERSION_NAME

        if (defaultRoundsVersion < 0) {
            defaultRoundsVersion = sharedPreferences.getInt(SharedPrefs.DEFAULT_ROUNDS_VERSION.key, -1)
        }
        roundVersionTextView.text = defaultRoundsVersion.toString()
    }
}
