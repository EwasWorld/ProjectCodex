package eywa.projectcodex.database.shootData.headToHead

import eywa.projectcodex.model.FullHeadToHead
import kotlinx.coroutines.flow.Flow

class HeadToHeadRepo {
    fun get(shootId: Int): Flow<FullHeadToHead> {
        TODO()
    }

    fun delete(shootId: Int) {
        TODO()
    }

    fun insert(headToHead: DatabaseHeadToHead) {
        TODO()
    }

    fun insert(heat: DatabaseHeadToHeadHeat) {
        TODO()
    }

    fun insert(vararg detail: DatabaseHeadToHeadDetail) {
        TODO()
    }

    fun update(vararg detail: DatabaseHeadToHeadDetail) {
        TODO()
    }
}
