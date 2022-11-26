package eywa.projectcodex.database.rounds

interface RoundTypeDao<T> {
    fun updateSingle(updateItem: T)

    suspend fun insert(insertItem: T)

    suspend fun deleteSingle(deleteItem: T)
}