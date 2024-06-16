package eywa.projectcodex.common.utils.classificationTables

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import eywa.projectcodex.R

object ClassificationTablesPreviewHelper {
    @Composable
    fun get() = get(LocalContext.current)

    fun get(context: Context) =
            ClassificationTablesUseCase(
                    context.resources
                            .openRawResource(R.raw.classification_round_scores_2023)
                            .bufferedReader()
                            .use { it.readText() }
            )

}
