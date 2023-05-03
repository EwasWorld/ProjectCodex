package eywa.projectcodex.components.sightMarks.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.sightMarks.SightMark
import java.util.*

class SightMarkDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                                .background(CodexTheme.colors.appBackground)
                                .verticalScroll(rememberScrollState())
                ) {
                    SightMarkDetail(
                            SightMark(
                                    distance = 50,
                                    isMetric = false,
                                    dateSet = Calendar.getInstance(),
                                    sightMark = 2.3f,
                                    note = "This is a note",
                                    marked = false,
                            )
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.sight_marks__detail_title)
    }
}
