package eywa.projectcodex.common.utils

object ListUtils {
    fun <T> List<List<T>>.transpose(): List<List<T>> {
        if (isEmpty()) return this
        check(all { it.size == first().size }) { "Must be rectangular" }
        if (first().isEmpty()) return listOf(emptyList())

        return first().indices.map { index -> map { it[index] } }
    }

    fun <T> List<T>.plusAtIndex(elements: List<T>, index: Int) =
            take(index) + elements + drop(index)

    fun <T> List<T>.containsDuplicates() = distinct().size != size
    fun <T> List<T>.getDuplicates() = groupBy { it }.filter { it.value.size > 1 }.keys

    /**
     * @return the item after [item]
     */
    fun <T> List<T>.next(item: T) = get(indexOf(item) % size)
}
