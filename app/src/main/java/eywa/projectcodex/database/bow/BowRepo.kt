package eywa.projectcodex.database.bow

import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow

const val DEFAULT_BOW_ID = -1

class BowRepo(private val bowDao: BowDao) {
    val defaultBow = bowDao.getDefaultBow()

    suspend fun insertDefaultBowIfNotExist() {
        bowDao.insertOrIgnore(DatabaseBow(id = DEFAULT_BOW_ID, name = "Default"))
    }

    suspend fun updateDefaultBow(isHighestAtTop: Boolean) {
        bowDao.setHighestAtTop(DEFAULT_BOW_ID, isHighestAtTop)
    }

    suspend fun updateDefaultBow(type: ClassificationBow) = bowDao.updateDefaultBow(type)
}
