package eywa.projectcodex.database

import androidx.lifecycle.LiveData
import eywa.projectcodex.database.daos.ArrowValueDao
import eywa.projectcodex.database.entities.ArrowValue

class ScoresRepository(private val arrowValueDao: ArrowValueDao) {
    val arrowValuesRepo = ArrowValuesRepo(arrowValueDao)

    class ArrowValuesRepo(private val arrowValueDao: ArrowValueDao) {
        suspend fun insert(arrowValue: ArrowValue) {
            arrowValueDao.insert(arrowValue)
        }

        val allArrowValues: LiveData<List<ArrowValue>> = arrowValueDao.getAllArrowValues()

        suspend fun deleteAll() {
            arrowValueDao.deleteAll()
        }
    }
}