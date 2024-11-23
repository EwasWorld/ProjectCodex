package eywa.projectcodex.database.shootData

import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.newScore.NewScoreType
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.arrows.ArrowCounterRepo
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.bow.DEFAULT_BOW_ID
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.HeadToHeadRepo
import eywa.projectcodex.database.views.PersonalBest
import eywa.projectcodex.database.views.ShootWithScore
import eywa.projectcodex.model.FullShootInfo
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ShootsRepo(
        private val shootDao: ShootDao,
        private val shootDetailDao: ShootDetailDao,
        private val shootRoundDao: ShootRoundDao,
        private val arrowCounterRepo: ArrowCounterRepo,
        private val headToHeadRepo: HeadToHeadRepo,
) {
    val mostRecentRoundShot = shootDao.getMostRecentRoundShot()

    @Transaction
    fun getQualifyingRoundId(dateShot: Calendar, roundId: Int): Flow<Int?> {
        val date = SimpleDateFormat("dd-MM", Locale.UK).format(dateShot.time)
        return shootDao.getQualifyingRoundId(date, roundId)
    }

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
                            (personalBest.isTiedPb) as isTiedPersonalBest,
                            bow.type as bow
                    FROM ${ShootWithScore.TABLE_NAME} as shoot
                    LEFT JOIN ${PersonalBest.TABLE_NAME} as personalBest
                            ON shoot.roundId = personalBest.roundId AND shoot.nonNullSubTypeId = personalBest.roundSubTypeId
                    LEFT JOIN ${DatabaseBow.TABLE_NAME} as bow ON bow.bowId = $DEFAULT_BOW_ID
                    $wheresString
                """,
                params.toTypedArray()
        )

        return shootDao.getAllFullShootInfo(query)
    }

    fun getFullShootInfo(shootIds: List<Int>) = shootDao.getFullShootInfo(shootIds)
    fun getFullShootInfo(shootId: Int) = shootDao.getFullShootInfo(shootId)

    fun getJoinedFullShoots(shootId: Int) = shootDao.getJoinedFullShoots(shootId)

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
            headToHead: DatabaseHeadToHead?,
            type: NewScoreType,
    ): Long {
        require(
                listOfNotNull(
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                ).distinct().size == 1,
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }
        require(type != NewScoreType.HEAD_TO_HEAD || headToHead != null)

        val id = shootDao.insert(shoot)
        if (shootRound != null) shootRoundDao.insert(shootRound.copy(shootId = id.toInt()))
        if (shootDetail != null) shootDetailDao.insert(shootDetail.copy(shootId = id.toInt()))
        when (type) {
            NewScoreType.SCORING -> Unit
            NewScoreType.COUNTING -> arrowCounterRepo.insert(DatabaseArrowCounter(id.toInt(), 0))
            NewScoreType.HEAD_TO_HEAD -> headToHeadRepo.insert(headToHead!!.copy(shootId = id.toInt()))
        }
        return id
    }

    suspend fun deleteRound(shootId: Int) {
        shootDao.deleteRound(shootId)
    }

    suspend fun updateShootRound(shootRound: DatabaseShootRound) {
        shootRoundDao.update(shootRound)
    }

    @Transaction
    suspend fun update(
            original: FullShootInfo,
            shoot: DatabaseShoot,
            shootRound: DatabaseShootRound?,
            shootDetail: DatabaseShootDetail?,
            headToHead: DatabaseHeadToHead?,
            type: NewScoreType,
    ) {
        require(
                listOfNotNull(
                        original.shoot.shootId,
                        shoot.shootId,
                        shootRound?.shootId,
                        shootDetail?.shootId,
                        headToHead?.shootId,
                ).distinct().size == 1,
        ) { "Mismatched shootIds" }
        require(shootRound == null || shootDetail == null) { "Clashing details/round" }

        val updates = mutableListOf<suspend () -> Unit>()

        updates.add {
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
        }

        val message = "Cannot change type if arrows have been shot"
        when (type) {
            NewScoreType.SCORING -> {
                if (original.arrowCounter != null) {
                    require(original.arrowCounter.shotCount == 0) { "$message score/count" }
                    updates.add { arrowCounterRepo.delete(original.arrowCounter) }
                }
                if (original.h2h != null) {
                    require(!original.h2h.hasStarted) { "$message score/h2h" }
                    updates.add { headToHeadRepo.delete(original.shoot.shootId) }
                }
            }

            NewScoreType.COUNTING -> {
                require(original.arrowsShot != 0) { "$message count/score" }
                if (original.arrowCounter == null) {
                    updates.add { arrowCounterRepo.insert(DatabaseArrowCounter(original.shoot.shootId, 0)) }
                }
                if (original.h2h != null) {
                    require(!original.h2h.hasStarted) { "$message count/h2h" }
                    updates.add { headToHeadRepo.delete(original.shoot.shootId) }
                }
            }

            NewScoreType.HEAD_TO_HEAD -> {
                require(original.arrowsShot != 0) { "$message h2h/score" }
                if (original.arrowCounter != null) {
                    require(original.arrowCounter.shotCount == 0) { "$message h2h/count" }
                    updates.add { arrowCounterRepo.delete(original.arrowCounter) }
                }
                if (original.h2h == null) {
                    require(headToHead != null) { "h2h info required" }
                    updates.add { headToHeadRepo.insert(headToHead) }
                }
            }
        }

        updates.forEach { it() }
    }
}
