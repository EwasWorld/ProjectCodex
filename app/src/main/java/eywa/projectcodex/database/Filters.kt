package eywa.projectcodex.database

import kotlin.reflect.KClass

/**
 * A set of items that each have a different subtype. All items must have the same parent class
 */
data class Filters<T : Any> private constructor(private val items: Map<KClass<out T>, T> = mapOf()) {
    constructor() : this(emptyMap())
    constructor(items: Iterable<T>) : this(items.associateBy { it::class })

    @Suppress("UNCHECKED_CAST")
    fun <I : T> get(clazz: KClass<I>) = items[clazz] as? I
    fun <I : T> contains(clazz: KClass<I>) = items.containsKey(clazz)
    fun <I : T> minus(clazz: KClass<I>) = Filters(items.minus(clazz))
    fun <I : T> plus(item: I): Filters<T> = Filters(items.plus(item::class to item))

    inline fun <reified I : T> get() = get(I::class)
    inline fun <reified I : T> contains() = contains(I::class)
    inline fun <reified I : T> minus() = minus(I::class)

    val size
        get() = items.size
}
