package eywa.projectcodex.databaseTests.migrationTests

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import eywa.projectcodex.database.ScoresRoomDatabaseImpl
import eywa.projectcodex.database.migrations.MIGRATION_13_14
import eywa.projectcodex.databaseTests.migrationTests.MigrationTestHelpers.SQL_FALSE
import eywa.projectcodex.databaseTests.migrationTests.MigrationTestHelpers.SQL_TRUE
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V13To14MigrationTests {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ScoresRoomDatabaseImpl::class.java,
            listOf(),
            FrameworkSQLiteOpenHelperFactory(),
    )

    /**
     * Creates the version 11 database and runs the [setup] on it,
     * then runs the migration to version 12 and returns the result
     */
    private fun createAndRunMigrations(setup: SupportSQLiteDatabase.() -> Unit): SupportSQLiteDatabase {
        helper.createDatabase(GeneralMigrationTests.TEST_DB_NAME, 13).apply {
            setup()
            close()
        }

        return helper.runMigrationsAndValidate(
                GeneralMigrationTests.TEST_DB_NAME,
                14,
                true,
                MIGRATION_13_14,
        )
    }

    @Test
    fun testMigrateIndividual() {
        val h2hs = listOf(
                HeadToHead(
                        shootId = 5,
                        isRecurveStyle = true,
                        teamSize = 1,
                        qualificationRank = 8,
                ).apply {
                    // Bye
                    +Match(
                            heat = 4,
                            matchNumber = 1,
                            opponent = null,
                            opponentQualificationRank = null,
                            shootOffSetNumber = null,
                            isShootOffWin = false,
                            sightersCount = 12,
                            isBye = true,
                    )
                    // Normal match
                    +Match(
                            heat = 3,
                            matchNumber = 2,
                            opponent = "Jess",
                            opponentQualificationRank = 15,
                            shootOffSetNumber = null,
                            isShootOffWin = true,
                            sightersCount = 8,
                            isBye = false,
                    ).apply {
                        +Detail(
                                setNumber = 1,
                                selfArrows = listOf(10, 9, 8),
                                opponentTotal = 15,
                        )
                        +Detail(
                                setNumber = 2,
                                selfArrows = listOf(7, 6, 5),
                                opponentTotal = 16,
                        )
                        +Detail(
                                setNumber = 3,
                                selfArrows = listOf(10, 10, 10),
                                opponentTotal = 17,
                        )
                    }

                    // Obey isShootOffWin
                    addShootOffMatch(
                            heat = 2,
                            matchNumber = 3,
                            shootOffSetNumber = 6,
                            isShootOffWin = true,
                            selfArrows = listOf(10),
                            opponentTotal = 10,
                    )
                    addShootOffMatch(
                            heat = 1,
                            matchNumber = 4,
                            shootOffSetNumber = 6,
                            isShootOffWin = false,
                            selfArrows = listOf(10),
                            opponentTotal = 10,
                    )

                    // Obey score over isShootOffWin
                    addShootOffMatch(
                            heat = 0,
                            matchNumber = 5,
                            shootOffSetNumber = 6,
                            isShootOffWin = true,
                            selfArrows = listOf(9),
                            opponentTotal = 10,
                    )
                },
        )

        var dbId = 0
        val db = createAndRunMigrations {
            h2hs.map { it.asOldHeadToHeadRow() }
                    .plus(h2hs.flatMap { h2h -> h2h.heats.map { it.asOldMatchRows() } })
                    .plus(h2hs.flatMap { h2h ->
                        h2h.heats.flatMap {
                            it.asOldDetailsRows(nextDbId = {
                                ++dbId
                            })
                        }
                    })
                    .forEach { (table, content) ->
                        insert(table, SQLiteDatabase.CONFLICT_ABORT, content)
                    }
        }

        MigrationTestHelpers.checkResponses(
                db = db,
                //language=RoomSql
                sqlQuery = "SELECT * FROM head_to_head",
                newValues = h2hs.map { it.asNewHeadToHeadRow() }
        )

        MigrationTestHelpers.checkResponses(
                db = db,
                //language=RoomSql
                sqlQuery = "SELECT * FROM head_to_head_match ORDER BY shootId, matchNumber",
                newValues = h2hs.flatMap { h2h -> h2h.heats.map { it.asNewMatchRows() } },
        )

        dbId = 0
        val expectedItems = h2hs.flatMap { h2h -> h2h.heats.flatMap { it.asNewDetailsRows { ++dbId } } }
                .plus(h2hs.flatMap { h2h ->
                    val so = h2h.heats.mapNotNull { it.shootOffRow { ++dbId } }
                    so
                })
                .sortedWith { t1, t2 ->
                    t1!!
                    t2!!

                    fun compareInts(column: String) =
                            (t1[column]!! as Int).compareTo(t2[column] as Int).takeIf { it != 0 }

                    compareInts("shootId")
                            ?: compareInts("matchNumber")
                            ?: compareInts("setNumber")
                            ?: (t1["type"]!! as String).compareTo(t2["type"] as String).let { -it }.takeIf { it != 0 }
                            ?: compareInts("arrowNumber")
                            ?: 0
                }

        MigrationTestHelpers.checkResponses(
                db = db,
                //language=RoomSql
                sqlQuery = "SELECT * FROM head_to_head_detail ORDER BY shootId, matchNumber, setNumber, type DESC, arrowNumber",
                newValues = expectedItems,
        )
    }

    data class HeadToHead(
            private val shootId: Int,
            private val isRecurveStyle: Boolean,
            private val teamSize: Int,
            private val qualificationRank: Int?,
    ) {
        private val matches = mutableListOf<Match>()
        val heats get() = matches.toList()

        operator fun Match.unaryPlus() {
            matches.add(this)
        }

        fun asOldHeadToHeadRow() =
                HEAD_TO_HEAD_TABLE to ContentValues().apply {
                    put("shootId", shootId)
                    put("isRecurveStyle", MigrationTestHelpers.convertBoolean(isRecurveStyle))
                    put("teamSize", teamSize)
                    if (qualificationRank != null) {
                        put("qualificationRank", qualificationRank)
                    }
                }

        fun asNewHeadToHeadRow() = mapOf(
                "shootId" to shootId,
                "isSetPoints" to MigrationTestHelpers.convertBoolean(isRecurveStyle),
                "teamSize" to teamSize,
                "qualificationRank" to qualificationRank,
                "endSize" to null,
                "totalArchers" to null,
        )

        fun addShootOffMatch(
                heat: Int,
                matchNumber: Int,
                shootOffSetNumber: Int,
                isShootOffWin: Boolean,
                selfArrows: List<Int>,
                opponentTotal: Int,
        ) {
            +Match(
                    heat = heat,
                    matchNumber = matchNumber,
                    opponent = null,
                    opponentQualificationRank = null,
                    shootOffSetNumber = shootOffSetNumber,
                    isShootOffWin = isShootOffWin,
                    sightersCount = 3,
                    isBye = false,
            ).apply {
                repeat(shootOffSetNumber - 1) {
                    +Detail(
                            setNumber = it + 1,
                            selfArrows = listOf(10, 10, 10),
                            opponentTotal = 30,
                    )
                }
                +Detail(
                        setNumber = shootOffSetNumber,
                        selfArrows = selfArrows,
                        opponentTotal = opponentTotal,
                )
            }
        }

        inner class Match(
                private val heat: Int,
                private val matchNumber: Int,
                private val opponent: String?,
                private val opponentQualificationRank: Int?,
                private val isShootOffWin: Boolean,
                private val sightersCount: Int,
                private val isBye: Boolean,
                private val shootOffSetNumber: Int?,
        ) {
            private val details = mutableListOf<Detail>()

            operator fun Detail.unaryPlus() {
                details.add(this)
            }

            fun asOldMatchRows() = HEAT_TABLE to ContentValues().apply {
                put("shootId", shootId)
                put("heat", heat)
                if (opponent != null) {
                    put("opponent", opponent)
                }
                if (opponentQualificationRank != null) {
                    put("opponentQualificationRank", opponentQualificationRank)
                }
                put("isShootOffWin", MigrationTestHelpers.convertBoolean(isShootOffWin))
                put("sightersCount", sightersCount)
                put("isBye", MigrationTestHelpers.convertBoolean(isBye))
            }

            fun asNewMatchRows() = mapOf(
                    "shootId" to shootId,
                    "matchNumber" to matchNumber,
                    "heat" to heat,
                    "maxPossibleRank" to null,
                    "opponent" to opponent,
                    "opponentQualificationRank" to opponentQualificationRank,
                    "sightersCount" to sightersCount,
                    "isBye" to isBye,
            )

            fun shootOffRow(
                    nextDbId: () -> Int,
            ) =
                    shootOffSetNumber?.let {
                        mapOf(
                                "headToHeadArrowScoreId" to nextDbId(),
                                "shootId" to shootId,
                                "matchNumber" to matchNumber,
                                "setNumber" to it,
                                "type" to "SHOOT_OFF",
                                "isTotal" to SQL_TRUE,
                                "arrowNumber" to 1,
                                "score" to
                                        if (!details.last().isScoreTied()) 3
                                        else if (isShootOffWin) 2
                                        else 0,
                                "isX" to SQL_FALSE,
                        )
                    }

            fun asOldDetailsRows(nextDbId: () -> Int) = details.flatMap { it.asOldDetails(nextDbId) }
            fun asNewDetailsRows(nextDbId: () -> Int) = details.flatMap { it.asNewDetails(nextDbId) }

            inner class Detail(
                    val setNumber: Int,
                    private val selfArrows: List<Int>,
                    private val opponentTotal: Int,
            ) {
                fun isScoreTied() = selfArrows.sum() == opponentTotal

                fun asOldDetails(nextDbId: () -> Int) = selfArrows.mapIndexed { i, it ->
                    DETAIL_TABLE to ContentValues().apply {
                        put("headToHeadArrowScoreId", nextDbId())
                        put("shootId", shootId)
                        put("heat", heat)
                        put("setNumber", setNumber)
                        put("type", "SELF")
                        put("isTotal", SQL_FALSE)
                        put("arrowNumber", i + 1)
                        put("score", it)
                        put("isX", SQL_FALSE)
                    }
                }.plus(
                        DETAIL_TABLE to ContentValues().apply {
                            put("headToHeadArrowScoreId", nextDbId())
                            put("shootId", shootId)
                            put("heat", heat)
                            put("setNumber", setNumber)
                            put("type", "OPPONENT")
                            put("isTotal", SQL_TRUE)
                            put("arrowNumber", 1)
                            put("score", opponentTotal)
                            put("isX", SQL_FALSE)
                        }
                )

                fun asNewDetails(nextDbId: () -> Int) = selfArrows.mapIndexed { i, it ->
                    mapOf(
                            "headToHeadArrowScoreId" to nextDbId(),
                            "shootId" to shootId,
                            "matchNumber" to matchNumber,
                            "setNumber" to setNumber,
                            "type" to "SELF",
                            "isTotal" to SQL_FALSE,
                            "arrowNumber" to i + 1,
                            "score" to it,
                            "isX" to SQL_FALSE,
                    )
                }.plus(
                        mapOf(
                                "headToHeadArrowScoreId" to nextDbId(),
                                "shootId" to shootId,
                                "matchNumber" to matchNumber,
                                "setNumber" to setNumber,
                                "type" to "OPPONENT",
                                "isTotal" to SQL_TRUE,
                                "arrowNumber" to 1,
                                "score" to opponentTotal,
                                "isX" to SQL_FALSE,
                        )
                )
            }
        }
    }

    companion object {
        private const val HEAD_TO_HEAD_TABLE = "head_to_head"
        private const val HEAT_TABLE = "head_to_head_heat"
        private const val DETAIL_TABLE = "head_to_head_detail"
    }
}
