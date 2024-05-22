package eywa.projectcodex.database.shootData

import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.arrows.ArrowCounterRepo
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ShootsRepo(
        private val shootDao: ShootDao,
        private val shootDetailDao: ShootDetailDao,
        private val shootRoundDao: ShootRoundDao,
        private val arrowCounterRepo: ArrowCounterRepo,
) {
    val mostRecentRoundShot = shootDao.getMostRecentRoundShot()

    @Transaction
    fun getMostRecentShootsForRound(count: Int, roundId: Int, subTypeId: Int = 1) =
            shootDao.getMostRecentShootsForRound(count, roundId, subTypeId)

    @Transaction
    fun getHighestScoreShootsForRound(count: Int, roundId: Int, subTypeId: Int = 1) =
            shootDao.getHighestScoreShootsForRound(count, roundId, subTypeId)

    @Transaction
    fun getFullShootInfo(filters: Filters<ShootFilter> = Filters()): Flow<List<DatabaseFullShootInfo>> {
        val shootAlias = "shoot"

        val params = mutableListOf<Any>()
        val wheres = mutableListOf<String>()

        fun Range<*, *>.handle(field: String): String {
            validate()
            val from = fromDb?.let {
                params.add(it)
                "$field >= ?"
            }
            val to = toDb?.let {
                params.add(it)
                "$field <= ?"
            }
            return listOfNotNull(from, to).joinToString(" AND ") { "($it)" }
        }

        var actualFilters = filters
        if (actualFilters.contains(ShootFilter.ScoreRange::class)) {
            actualFilters += ShootFilter.ArrowCounts(false)
        }
        if (actualFilters.contains(ShootFilter.PersonalBests::class)) {
            actualFilters += ShootFilter.ArrowCounts(false)
            actualFilters += ShootFilter.CompleteRounds
        }

        actualFilters.forEach { filter ->
            val filterString = when (filter) {
                ShootFilter.FirstRoundOfDay -> {
                    //language=RoomSql
                    val firstShoots = """
                        SELECT s1.shootId 
                        FROM (
                            SELECT
                                strftime("%d-%m-%Y", s.dateShot / 1000, 'unixepoch') as time,
                                s.shootId,
                                MIN(s.dateShot)
                            FROM ${DatabaseShoot.TABLE_NAME} as s
                            GROUP BY time
                        ) as s1
                    """.trimIndent()
                    "($shootAlias.shootId IN ($firstShoots)) AND ($shootAlias.joinWithPrevious = 0)"
                }

                ShootFilter.CompleteRounds -> "$shootAlias.scoringArrowCount = $shootAlias.roundCount OR $shootAlias.counterCount = $shootAlias.roundCount"
                is ShootFilter.DateRange -> filter.handle("$shootAlias.dateShot")
                is ShootFilter.ScoreRange -> filter.handle("$shootAlias.score")

                is ShootFilter.ArrowCounts -> {
                    val truth = if (filter.only) "" else "NOT"
                    "$truth $shootAlias.shootId IN (SELECT shootId FROM ${DatabaseArrowCounter.TABLE_NAME})"
                }

                ShootFilter.PersonalBests -> "isPersonalBest = 1"
                is ShootFilter.Round -> {
                    if (filter.roundId == null) {
                        "$shootAlias.roundId IS NULL"
                    }
                    else {
                        var str = "$shootAlias.roundId = ?"
                        params.add(filter.roundId)

                        if (filter.subtypeId != null) {
                            params.add(filter.subtypeId)
                            str += " AND $shootAlias.nonNullSubTypeId = ?"
                        }
                        str
                    }
                }
            }
            wheres.add(filterString)
        }

        val wheresString = ("WHERE " + wheres.joinToString(" AND ") { "($it)" })
                .takeIf { wheres.isNotEmpty() } ?: ""

        val query = SimpleSQLiteQuery(
                //language=RoomSql
                """
                    SELECT 
                            shoot.*,
                            (shoot.scoringArrowCount = shoot.roundCount AND shoot.score = personalBest.score) as isPersonalBest,
                            (personalBest.isTiedPb) as isTiedPersonalBest
                    FROM ${ShootWithScore.TABLE_NAME} as shoot
                    LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                            ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                    $wheresString
                """,
                params.toTypedArray()
        )

        return shootDao.getAllFullShootInfo(query)
    }

    fun getFullShootInfo(shootIds: List<Int>) = shootDao.getFullShootInfo(shootIds)
    fun getFullShootInfo(shootId: Int) = shootDao.getFullShootInfo(shootId)

    /**
     * Grabs all data from the month of [date], plus 8 days before and after
     */
    fun getCountsForCalendar(date: Calendar): Flow<List<DatabaseArrowCountCalendarData>> {
        val month = date.get(Calendar.MONTH) + 1
        val year = date.get(Calendar.YEAR)
        val from = DateTimeFormat.SHORT_DATE_TIME.parse("1/$month/$year 00:01")
        from.add(Calendar.DATE, -8)

        val nextMonth = (month + 1).let { if (it > 12) 1 else it }
        val nextYear = if (nextMonth == 1) year + 1 else year
        val to = DateTimeFormat.SHORT_DATE_TIME.parse("8/$nextMonth/$nextYear 23:59")

        return shootDao.getCountsForCalendar(from, to)
    }

    suspend fun insert(shoot: DatabaseShoot) = shootDao.insert(shoot)

    @Transaction
    suspend fun insert(
            shoot: DatabaseShoot,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
            isScoringNotCounting: Boolean,
    ): Long {
        require(
                listOfNotNull(
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                ).distinct().size == 1
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }

        val id = shootDao.insert(shoot)
        if (shootRound != null) shootRoundDao.insert(shootRound.copy(shootId = id.toInt()))
        if (shootDetail != null) shootDetailDao.insert(shootDetail.copy(shootId = id.toInt()))
        if (!isScoringNotCounting) arrowCounterRepo.insert(DatabaseArrowCounter(id.toInt(), 0))
        return id
    }

    suspend fun deleteRound(shootId: Int) {
        shootDao.deleteRound(shootId)
    }

    @Transaction
    suspend fun update(
            original: FullShootInfo,
            shoot: DatabaseShoot,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
            isScoringNotCounting: Boolean,
    ) {
        require(
                listOfNotNull(
                        original.shoot.shootId,
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                ).distinct().size == 1
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }
        require(
                isScoringNotCounting == (original.arrowCounter == null) || original.arrowsShot == 0
        ) { "Cannot change type if arrows have been shot" }

        shootDao.update(shoot)

        if (shootRound != null) {
            if (original.shootRound != null) shootRoundDao.update(shootRound)
            else shootRoundDao.insert(shootRound)

            if (original.shootDetail != null) shootDetailDao.delete(shoot.shootId)
        }
        else if (shootDetail != null) {
            if (original.shootDetail != null) shootDetailDao.update(shootDetail)
            else shootDetailDao.insert(shootDetail)

            if (original.shootRound != null) shootRoundDao.delete(shoot.shootId)
        }
        else {
            shootRoundDao.delete(shoot.shootId)
            shootDetailDao.delete(shoot.shootId)
        }

        if (isScoringNotCounting && original.arrowCounter != null) {
            arrowCounterRepo.delete(original.arrowCounter)
        }
        else if (!isScoringNotCounting && original.arrowCounter == null) {
            arrowCounterRepo.insert(DatabaseArrowCounter(original.shoot.shootId, 0))
        }
    }
}
