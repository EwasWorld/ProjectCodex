package eywa.projectcodex.database.daos

interface RoundTypeDao<T> {
    fun updateSingle(updateItem: T)

    suspend fun insert(insertItem: T)
}