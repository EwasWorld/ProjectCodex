package eywa.projectcodex.testUtils

import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

val mockUpdateDefaultRoundsTask = mock<UpdateDefaultRoundsTask> {
    on { state } doReturn MutableStateFlow(UpdateDefaultRoundsStatePreviewHelper.complete)
}
