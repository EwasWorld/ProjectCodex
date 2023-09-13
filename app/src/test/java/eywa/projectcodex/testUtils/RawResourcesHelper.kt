package eywa.projectcodex.testUtils

import android.content.res.Resources
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.UpdateType
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.database.rounds.RoundSubType
import kotlinx.coroutines.flow.flow
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.FileInputStream
import kotlin.reflect.KClass

object RawResourcesHelper {
    val classificationTables = ClassificationTablesUseCase(
            FileInputStream("src\\main\\res\\general\\raw\\classification_round_scores_2023.csv")
                    .bufferedReader()
                    .use { it.readText() }
    )

    suspend fun getDefaultRounds(): List<FullRoundInfo> {
        val allItems = mutableListOf<Any>()

        val repo = mock<RoundRepo> {
            on { fullRoundsInfo } doReturn flow { emit(emptyList()) }
            onBlocking { updateRounds(any()) } doAnswer {
                @Suppress("UNCHECKED_CAST")
                allItems.addAll((it.arguments[0] as Map<Any, UpdateType>).keys)
                Unit
            }
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        val inputStream = FileInputStream("src\\main\\res\\general\\raw\\default_rounds_data.json")
        val resources = mock<Resources> { on { openRawResource(any()) } doReturn inputStream }

        UpdateDefaultRoundsTask(
                repository = repo,
                resources = resources,
                datastore = MockDatastore().mock,
                logger = mock {},
        ).runTask()

        return allItems.asFullRoundInfo()
    }

    private fun List<Any>.asFullRoundInfo(): List<FullRoundInfo> {
        val groupedByClass = groupBy { it::class }
        val info = mutableListOf<FullRoundInfo>()

        for (round in groupedByClass.get<Round>()) {
            val roundId = round.roundId

            info.add(
                    FullRoundInfo(
                            round = round,
                            roundSubTypes = groupedByClass.get<RoundSubType>().filter { it.roundId == roundId },
                            roundArrowCounts = groupedByClass.get<RoundArrowCount>().filter { it.roundId == roundId },
                            roundDistances = groupedByClass.get<RoundDistance>().filter { it.roundId == roundId },
                    )
            )
        }

        return info
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> Map<KClass<out Any>, List<Any>>.get() = get(T::class) as List<T>

}
