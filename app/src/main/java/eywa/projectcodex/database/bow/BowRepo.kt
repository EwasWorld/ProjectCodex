package eywa.projectcodex.database.bow

const val DEFAULT_BOW_ID = -1

class BowRepo(private val bowDao: BowDao) {
    val defaultBow = bowDao.getDefaultBow()

    suspend fun updateDefaultBow(isHighestAtTop: Boolean) {
        bowDao.setHighestAtTop(DEFAULT_BOW_ID, isHighestAtTop)
    }
}
