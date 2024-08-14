package eywa.projectcodex.api

sealed class DataState<out Data, out Error> {
    data object Loading : DataState<Nothing, Nothing>() {
        override fun data(): Nothing? = null
    }

    data class Error<E>(val error: E) : DataState<Nothing, E>() {
        override fun data(): Nothing? = null
    }

    data class Success<D>(val data: D) : DataState<D, Nothing>() {
        override fun data(): D? = data
    }

    abstract fun data(): Data?
}
