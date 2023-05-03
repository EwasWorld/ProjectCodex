package eywa.projectcodex.components.sightMarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.sightMarks.ui.SightMarks
import java.util.*

class SightMarksFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                var currentSet by remember { mutableStateOf(0) }
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                                .background(CodexTheme.colors.appBackground)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                ) {
                    FloatingActionButton(
                            onClick = {
                                val new = currentSet + 1
                                currentSet = if (new !in fakeSightMarks.indices) 0 else new
                            },
                            modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                    ) {
                        Icon(
                                Icons.Default.Radio,
                                ""
                        )
                    }
                    Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(15.dp)
                    ) {
                        SightMarks(
                                state = SightMarksState(fakeSightMarks[currentSet]),
                                onClick = {
                                    findNavController().navigate(SightMarksFragmentDirections.actionSightMarksFragmentToSightMarkDetailFragment())
                                }
                        )
                    }
                }
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
                SightMark(10, true, Calendar.getInstance(), 3.35f, isArchive = true),
                SightMark(10, true, Calendar.getInstance(), 3.3f, marked = true),
                SightMark(10, true, Calendar.getInstance(), 3.25f),
                SightMark(20, true, Calendar.getInstance(), 3.2f, note = "", marked = true),
                SightMark(30, true, Calendar.getInstance(), 3.15f),
                SightMark(50, false, Calendar.getInstance(), 4f),
                SightMark(50, false, Calendar.getInstance(), 4f),
                SightMark(50, false, Calendar.getInstance(), 2.01f, note = ""),
                SightMark(50, false, Calendar.getInstance(), 2f, marked = true, isArchive = true),
                SightMark(20, false, Calendar.getInstance(), 2.55f),
                SightMark(30, false, Calendar.getInstance(), 2.5f),
                SightMark(40, false, Calendar.getInstance(), 2.45f),
        ),
        listOf(
                SightMark(80, false, Calendar.getInstance(), 1f, marked = true),
                SightMark(60, false, Calendar.getInstance(), 1.4f),
                SightMark(50, false, Calendar.getInstance(), 2f),
                SightMark(40, false, Calendar.getInstance(), 3.15f),
                SightMark(30, false, Calendar.getInstance(), 3.1f),
                SightMark(20, false, Calendar.getInstance(), 3.9f),
                SightMark(70, true, Calendar.getInstance(), 1.375f),
                SightMark(60, true, Calendar.getInstance(), 1.2f, isArchive = true),
                SightMark(50, true, Calendar.getInstance(), 1.9f),
                SightMark(30, true, Calendar.getInstance(), 3.35f),
                SightMark(25, true, Calendar.getInstance(), 4.1f),
                SightMark(20, true, Calendar.getInstance(), 4.09f),
                SightMark(18, true, Calendar.getInstance(), 4.5f),
        ),
)
