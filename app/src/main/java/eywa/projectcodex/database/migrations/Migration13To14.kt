package eywa.projectcodex.database.migrations

import eywa.projectcodex.database.migrations.dsl.DbCreateTableDsl.ColumnType
import eywa.projectcodex.database.migrations.dsl.DbMigrationDsl
import eywa.projectcodex.database.migrations.dsl.DbTableColumn
import eywa.projectcodex.database.migrations.dsl.DbTableForeignKey

val MIGRATION_13_14 = DbMigrationDsl.createMigration(13, 14) {
    createTempMatchHeatsTable()
    addShootOffRows()
    h2hMigration()
    matchMigration()
    detailMigration()
    dropTable("temp_match_heats")
}

/**
 * Creates temp_match_heats which stores a mapping of existing heat to new matchNumber
 */
private fun DbMigrationDsl.createTempMatchHeatsTable() {
    val result = runQueryImmediately(
            //language=RoomSql
            """
                SELECT shootId, heat
                FROM head_to_head_heat
            """.trimIndent(),
    )
    val shootIdToHeat = mutableListOf<Pair<Int, Int>>()
    if (result.moveToFirst()) {
        while (true) {
            shootIdToHeat.add(result.getInt(0) to result.getInt(1))
            if (result.isLast) break
            result.moveToNext()
        }
    }

    createTable("temp_match_heats") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("matchNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("heat", ColumnType.INTEGER))
        compositePrimaryKey("shootId", "heat")
    }

    // Create a table with a mapping of heat to matchNumber
    shootIdToHeat
            .groupBy { it.first }
            .flatMap { (shootId, values) ->
                val heats = values.map { it.second }.sortedDescending()
                heats.mapIndexed { index, heat -> Triple(shootId, heat, index + 1) }
            }
            .forEach { (shootId, heat, matchNumber) ->
                customSql(
                        //language=RoomSql
                        """
                            INSERT INTO `temp_match_heats` (`shootId`, `matchNumber`, `heat`)
                            VALUES ($shootId, $matchNumber, $heat)
                        """,
                )
            }
}

/**
 * Adds a SHOOT_OFF row to all head to head matches with 6 sets
 * Assumes all teamSizes are 1
 */
private fun DbMigrationDsl.addShootOffRows() {
    val result = runQueryImmediately(
            //language=RoomSql
            """
                SELECT shootId, heat, isShootOffWin
                FROM head_to_head_heat
            """.trimIndent(),
    )
    val shootIdToHeat = mutableListOf<Triple<Int, Int, Boolean>>()
    if (result.moveToFirst()) {
        while (true) {
            shootIdToHeat.add(Triple(result.getInt(0), result.getInt(1), result.getInt(2) != 0))
            if (result.isLast) break
            result.moveToNext()
        }
    }

    val result2 = runQueryImmediately(
            //language=RoomSql
            """
                SELECT 
                    shootId, 
                    heat,
                    SUM(CASE WHEN type = 'SELF' THEN score ELSE 0 END) as team,
                    SUM(CASE WHEN type = 'OPPONENT' THEN score ELSE 0 END) as opponent
                FROM head_to_head_detail
                WHERE setNumber = 6
                GROUP BY shootId, heat
                ORDER BY shootId, heat DESC
            """.trimIndent(),
    )
    if (result2.moveToFirst()) {
        while (true) {
            val shootId = result2.getInt(0)
            val heat = result2.getInt(1)
            val team = result2.getInt(2)
            val opponent = result2.getInt(3)
            val isShootOffWin = shootIdToHeat.find { it.first == shootId && it.second == heat }?.third ?: false
            val score = if (team != opponent) 3 else if (isShootOffWin) 2 else 0

            customSql(
                    """
                        INSERT INTO `head_to_head_detail` (`shootId`, `heat`, `type`, `isTotal`, `setNumber`, `arrowNumber`, `score`, `isX`)
                        VALUES ($shootId, $heat, 'SHOOT_OFF', 1, 6, 1, $score, 0)
                    """.trimIndent(),
            )
            if (result2.isLast) break
            result2.moveToNext()
        }
    }
}

private fun DbMigrationDsl.h2hMigration() {
    removeColumn("head_to_head", "isRecurveStyle")
    addColumn("head_to_head", DbTableColumn("isSetPoints", ColumnType.BOOLEAN, default = "1"))
    addColumn("head_to_head", DbTableColumn("endSize", ColumnType.INTEGER, nullable = true, default = "NULL"))
    addColumn("head_to_head", DbTableColumn("totalArchers", ColumnType.INTEGER, nullable = true, default = "NULL"))
}

private fun DbMigrationDsl.matchMigration() {
    createTable("head_to_head_match") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("matchNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("heat", ColumnType.INTEGER, nullable = true))
        addColumn(DbTableColumn("maxPossibleRank", ColumnType.INTEGER, nullable = true))
        addColumn(DbTableColumn("opponent", ColumnType.TEXT, nullable = true))
        addColumn(DbTableColumn("opponentQualificationRank", ColumnType.INTEGER, nullable = true))
        addColumn(DbTableColumn("sightersCount", ColumnType.INTEGER))
        addColumn(DbTableColumn("isBye", ColumnType.BOOLEAN))
        compositePrimaryKey("shootId", "matchNumber")
        addForeignKey(DbTableForeignKey("shoots", listOf("shootId"), listOf("shootId")))
    }

    customSql(
            //language=RoomSql
            """
                INSERT INTO `head_to_head_match` (`shootId`, `matchNumber`, `heat`, `maxPossibleRank`, `opponent`, `opponentQualificationRank`, `sightersCount`, `isBye`)
                SELECT `shootId`, `matchNumber`, `heat`, NULL, `opponent`, `opponentQualificationRank`, `sightersCount`, `isBye`
                FROM head_to_head_heat
                LEFT JOIN temp_match_heats USING (shootId, heat);
            """,
    )

    dropTable("head_to_head_heat")
}

private fun DbMigrationDsl.detailMigration() {
    renameTable("head_to_head_detail", "head_to_head_detail_old")

    createTable("head_to_head_detail") {
        addColumn(DbTableColumn("headToHeadArrowScoreId", ColumnType.INTEGER))
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("matchNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("type", ColumnType.TEXT))
        addColumn(DbTableColumn("isTotal", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("setNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("arrowNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("score", ColumnType.INTEGER))
        addColumn(DbTableColumn("isX", ColumnType.BOOLEAN))
        singlePrimaryKey("headToHeadArrowScoreId")
        addForeignKey(DbTableForeignKey("shoots", listOf("shootId"), listOf("shootId")))
    }
    addIndex("head_to_head_detail", true, listOf("shootId", "matchNumber", "setNumber", "arrowNumber", "type"))

    customSql(
            //language=RoomSql
            """
                INSERT INTO `head_to_head_detail` (`headToHeadArrowScoreId`, `shootId`, `matchNumber`, `type`, `isTotal`, `setNumber`, `arrowNumber`, `score`, `isX`)
                SELECT `headToHeadArrowScoreId`, `shootId`, `matchNumber`, `type`, `isTotal`, `setNumber`, `arrowNumber`, `score`, `isX`
                FROM head_to_head_detail_old
                LEFT JOIN temp_match_heats USING (shootId, heat);
            """,
    )

    dropTable("head_to_head_detail_old")
}
