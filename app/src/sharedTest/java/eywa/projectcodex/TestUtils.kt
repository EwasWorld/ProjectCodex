package eywa.projectcodex

import org.mockito.ArgumentCaptor

class TestUtils {
    companion object {
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
    }
}