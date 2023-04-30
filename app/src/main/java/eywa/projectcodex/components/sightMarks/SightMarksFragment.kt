package eywa.projectcodex.components.sightMarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import java.util.*

class SightMarksFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                SightMarks(
                        SightMarksState(
                                sightMarks = listOf(
                                        SightMark(10, true, Calendar.getInstance(), 3.25f),
                                        SightMark(20, true, Calendar.getInstance(), 3.2f),
                                        SightMark(30, true, Calendar.getInstance(), 3.15f),
                                        SightMark(50, false, Calendar.getInstance(), 2f),
                                ),
                        ),
                )
            }
        }
    }
}
