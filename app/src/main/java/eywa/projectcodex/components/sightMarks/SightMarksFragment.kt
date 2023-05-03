package eywa.projectcodex.components.sightMarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eywa.projectcodex.R
import java.util.*

@AndroidEntryPoint
class SightMarksFragment : Fragment() {
    private val viewModel: SightMarksViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()
                SightMarksScreen(
                        state = state,
                        listener = { viewModel.handle(it) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.sight_marks__title)
    }
}

val fakeSightMarks = listOf(
        listOf(
                SightMark(1, 10, true, Calendar.getInstance(), 3.35f, isArchive = true),
                SightMark(1, 10, true, Calendar.getInstance(), 3.3f, marked = true),
                SightMark(1, 10, true, Calendar.getInstance(), 3.25f),
                SightMark(1, 20, true, Calendar.getInstance(), 3.2f, note = "", marked = true),
                SightMark(1, 30, true, Calendar.getInstance(), 3.15f),
                SightMark(1, 50, false, Calendar.getInstance(), 4f),
                SightMark(1, 50, false, Calendar.getInstance(), 4f),
                SightMark(1, 50, false, Calendar.getInstance(), 2.01f, note = ""),
                SightMark(1, 50, false, Calendar.getInstance(), 2f, marked = true, isArchive = true),
                SightMark(1, 20, false, Calendar.getInstance(), 2.55f),
                SightMark(1, 30, false, Calendar.getInstance(), 2.5f),
                SightMark(1, 40, false, Calendar.getInstance(), 2.45f),
        ),
        listOf(
                SightMark(1, 80, false, Calendar.getInstance(), 1f, marked = true),
                SightMark(1, 60, false, Calendar.getInstance(), 1.4f),
                SightMark(1, 50, false, Calendar.getInstance(), 2f),
                SightMark(1, 40, false, Calendar.getInstance(), 3.15f),
                SightMark(1, 30, false, Calendar.getInstance(), 3.1f),
                SightMark(1, 20, false, Calendar.getInstance(), 3.9f),
                SightMark(1, 70, true, Calendar.getInstance(), 1.375f),
                SightMark(1, 60, true, Calendar.getInstance(), 1.2f, isArchive = true),
                SightMark(1, 50, true, Calendar.getInstance(), 1.9f),
                SightMark(1, 30, true, Calendar.getInstance(), 3.35f),
                SightMark(1, 25, true, Calendar.getInstance(), 4.1f),
                SightMark(1, 20, true, Calendar.getInstance(), 4.09f),
                SightMark(1, 18, true, Calendar.getInstance(), 4.5f),
        ),
)
