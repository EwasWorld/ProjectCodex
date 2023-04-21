package eywa.projectcodex.testUtils

import androidx.annotation.VisibleForTesting
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.flow.flow
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

/**
 * In the main package so it can be used in unit tests and instrumented tests. Maybe there's a better spot for it?
 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class MockDatastore {
    var values: Map<DatastoreKey<out Any>, Any> = emptyMap()

    val mock: CodexDatastore = mock {
        on { get<Any>(any()) } doAnswer {
            flow { emit(values[it.arguments.first() as DatastoreKey<*>]!!) }
        }
    }
}
