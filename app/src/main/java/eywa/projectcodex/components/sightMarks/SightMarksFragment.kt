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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.sightMarks.ui.SightMarks
import java.util.*

class SightMarksFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                                .background(CodexTheme.colors.appBackground)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                ) {
                    Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(15.dp)
                    ) {
                        SightMarks(
                                state = SightMarksState(
                                        sightMarks = listOf(
                                                SightMark(10, true, Calendar.getInstance(), 3.25f),
                                                SightMark(20, true, Calendar.getInstance(), 3.2f),
                                                SightMark(30, true, Calendar.getInstance(), 3.15f),
                                                SightMark(50, false, Calendar.getInstance(), 2f),
                                                SightMark(50, false, Calendar.getInstance(), 2f, marked = true),
                                        ),
                                ),
                        )
                    }
                }
            }
        }
    }
}
