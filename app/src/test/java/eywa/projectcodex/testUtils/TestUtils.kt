package eywa.projectcodex.testUtils

import android.content.res.Resources
import eywa.projectcodex.common.archeryObjects.ScorePadData
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.kotlin.anyVararg

class TestUtils {
    companion object {
        val defaultColumnHeaderOrder = listOf(
                ScorePadData.ColumnHeader.ARROWS,
                ScorePadData.ColumnHeader.HITS,
                ScorePadData.ColumnHeader.SCORE,
                ScorePadData.ColumnHeader.GOLDS,
                ScorePadData.ColumnHeader.RUNNING_TOTAL
        )

        /**
         * Use this when the usual argumentCaptor.capture() apparently throws a null
         *
         * Source:
         * - https://stackoverflow.com/a/46064204
         * - https://github.com/android/architecture-components-samples/blob/master/BasicRxJavaSampleKotlin/app/src/test/java/com/example/android/observability/MockitoKotlinHelpers.kt
         *
         * @return ArgumentCaptor.capture() as nullable type to avoid java.lang.IllegalStateException when null is
         * returned
         */
        fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

        /**
         * Source: https://github.com/mockito/mockito-kotlin/blob/main/mockito-kotlin/src/main/kotlin/org/mockito/kotlin/internal/CreateInstance.kt
         * TODO Use kotlin mockito library ^^
         */
        fun <T> anyMatcher(): T = Mockito.any() ?: castNull()

        @Suppress("UNCHECKED_CAST")
        private fun <T> castNull(): T = null as T

        fun createResourceMock(map: Map<Int, String>): Resources {
            val resources = Mockito.mock(Resources::class.java)
            Mockito.`when`(resources.getString(Mockito.anyInt())).thenAnswer { invocation ->
                val resourceId = invocation.getArgument<Int>(0)
                map[resourceId] ?: throw IllegalStateException("Unknown resource: $resourceId")
            }
            Mockito.`when`(resources.getString(Mockito.anyInt(), anyVararg())).thenAnswer { invocation ->
                val resourceId = invocation.getArgument<Int>(0)
                map[resourceId]
                        ?.format(*invocation.arguments.drop(1).toTypedArray())
                        ?: throw IllegalStateException("Unknown resource: $resourceId")
            }
            return resources
        }
    }
}
