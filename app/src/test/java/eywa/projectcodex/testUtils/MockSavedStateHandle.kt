package eywa.projectcodex.testUtils

import androidx.lifecycle.SavedStateHandle
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock

class MockSavedStateHandle {
    var values = mutableMapOf<String, Any?>()

    private fun getMocked(key: Any): Any? = values[key as String]

    val mock = mock<SavedStateHandle> {
        on { get<Any>(any()) } doAnswer { getMocked(it.arguments.first()) }
    }
}
