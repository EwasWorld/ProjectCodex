package eywa.projectcodex.model

sealed class Either<Left, Right> {
    abstract val left: Left?
    abstract val right: Right?

    data class Left<A, B>(override val left: A) : Either<A, B>() {
        override val right: B? = null
    }

    data class Right<A, B>(override val right: B) : Either<A, B>() {
        override val left: A? = null
    }
}
