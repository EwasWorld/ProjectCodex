package eywa.projectcodex.common.utils.classificationTables

import android.content.Context
import eywa.projectcodex.R

object ClassificationTablesPreviewHelper {
    fun get(context: Context) =
            ClassificationTablesUseCase(
                    context.resources
                            .openRawResource(R.raw.classification_round_scores_2023)
                            .bufferedReader()
                            .use { it.readText() }
            )

}
