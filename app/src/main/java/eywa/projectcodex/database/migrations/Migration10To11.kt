package eywa.projectcodex.database.migrations

import androidx.room.ForeignKey
import eywa.projectcodex.database.migrations.dsl.DbCreateTableDsl.ColumnType
import eywa.projectcodex.database.migrations.dsl.DbMigrationDsl
import eywa.projectcodex.database.migrations.dsl.DbTableColumn
import eywa.projectcodex.database.migrations.dsl.DbTableForeignKey

val MIGRATION_10_11 = DbMigrationDsl.createMigration(10, 11) {
    dropView("completed_round_scores")
    dropView("personal_bests")

    bowMigration()
    archerMigration()
    shootSubTablesMigration()
    shootMigration()
    arrowMigration()
    sightMarkMigration()

    createViews()
}

private fun DbMigrationDsl.bowMigration() {
    renameTable("bows", "bows_old")

    createTable("bows") {
        addColumn(DbTableColumn("bowId", ColumnType.INTEGER))
        addColumn(DbTableColumn("name", ColumnType.TEXT, default = "'Default'"))
        addColumn(DbTableColumn("description", ColumnType.TEXT, nullable = true, default = "NULL"))
        addColumn(DbTableColumn("type", ColumnType.ENUM))
        addColumn(DbTableColumn("isSightMarkDiagramHighestAtTop", ColumnType.BOOLEAN))
        singlePrimaryKey("bowId")
    }

    customQuery(
            //language=RoomSql
            """
                INSERT INTO `bows` (`bowId`, `name`, `description`, `type`, `isSightMarkDiagramHighestAtTop`)
                SELECT `id`, "Default", NULL, 0, `isSightMarkDiagramHighestAtTop` 
                FROM bows_old;
            """
    )

    dropTable("bows_old")
}

private fun DbMigrationDsl.archerMigration() {
    addColumn("archers", DbTableColumn("isGent", ColumnType.BOOLEAN, default = "1"))
    addColumn("archers", DbTableColumn("age", ColumnType.ENUM, default = "1"))

    createTable("archer_handicaps") {
        addColumn(DbTableColumn("archerHandicapId", ColumnType.INTEGER))
        addColumn(DbTableColumn("archerId", ColumnType.INTEGER, indexed = true))
        addColumn(DbTableColumn("bowStyle", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("handicapType", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("handicap", ColumnType.INTEGER))
        addColumn(DbTableColumn("dateSet", ColumnType.CALENDAR))
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER, nullable = true, indexed = true))
        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "archers",
                        foreignTableColumn = listOf("archerId"),
                        tableColumn = listOf("archerId"),
                )
        )
        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
        singlePrimaryKey("archerHandicapId")
    }
}

private fun DbMigrationDsl.shootSubTablesMigration() {
    createTable("shoot_rounds") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("roundId", ColumnType.INTEGER, indexed = true))
        addColumn(DbTableColumn("roundSubTypeId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(DbTableColumn("faces", ColumnType.LIST, nullable = true))
        addColumn(DbTableColumn("sightersCount", ColumnType.INTEGER))

        singlePrimaryKey("shootId")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "round_sub_types",
                        foreignTableColumn = listOf("roundId", "subTypeId"),
                        tableColumn = listOf("roundId", "roundSubTypeId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "rounds",
                        foreignTableColumn = listOf("roundId"),
                        tableColumn = listOf("roundId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
    }

    customQuery(
            //language=RoomSql
            """
                INSERT INTO `shoot_rounds` (`shootId`, `roundId`, `roundSubTypeId`, `faces`, `sightersCount`)
                SELECT `archerRoundId`, `roundId`, `roundSubTypeId`, `faces`, 0 
                FROM archer_rounds
                WHERE NOT roundId IS NULL 
                ;
            """
    )

    createTable("shoot_details") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("face", ColumnType.ENUM, nullable = true))
        addColumn(DbTableColumn("distance", ColumnType.INTEGER, nullable = true))
        addColumn(DbTableColumn("isDistanceInMeters", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("faceSizeInCm", ColumnType.REAL, nullable = true))

        singlePrimaryKey("shootId")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }

    customQuery(
            //language=RoomSql
            """
                INSERT INTO `shoot_details` (`shootId`, `face`, `distance`, `isDistanceInMeters`, `faceSizeInCm`)
                SELECT `archerRoundId`, substr(`faces`, 1, 1), NULL, 1, NULL
                FROM archer_rounds
                WHERE roundId IS NULL AND NOT faces IS NULL  
                ;
            """
    )

}

private fun DbMigrationDsl.shootMigration() {
    createTable("shoots") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("dateShot", ColumnType.CALENDAR))
        addColumn(DbTableColumn("archerId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(DbTableColumn("countsTowardsHandicap", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("bowId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(DbTableColumn("goalScore", ColumnType.INTEGER, nullable = true))
        addColumn(DbTableColumn("shootStatus", ColumnType.TEXT, nullable = true))
        addColumn(DbTableColumn("joinWithPrevious", ColumnType.BOOLEAN, default = "0"))

        singlePrimaryKey("shootId")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "bows",
                        foreignTableColumn = listOf("bowId"),
                        tableColumn = listOf("bowId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "archers",
                        foreignTableColumn = listOf("archerId"),
                        tableColumn = listOf("archerId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
    }

    customQuery(
            //language=RoomSql
            """
                INSERT INTO `shoots` (`shootId`, `dateShot`, `archerId`, `countsTowardsHandicap`, `bowId`, `goalScore`, `shootStatus`, `joinWithPrevious`)
                SELECT `archerRoundId`, `dateShot`, `archerId`, `countsTowardsHandicap`, `bowId`, `goalScore`, `shootStatus`, `joinWithPrevious` 
                FROM archer_rounds;
            """
    )

    dropTable("archer_rounds")
}

private fun DbMigrationDsl.arrowMigration() {
    createTable("arrow_counters") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("shotCount", ColumnType.INTEGER))

        singlePrimaryKey("shootId")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }

    createTable("arrow_scores") {
        addColumn(DbTableColumn("shootId", ColumnType.INTEGER))
        addColumn(DbTableColumn("arrowNumber", ColumnType.INTEGER))
        addColumn(DbTableColumn("score", ColumnType.INTEGER))
        addColumn(DbTableColumn("isX", ColumnType.BOOLEAN))

        compositePrimaryKey("shootId", "arrowNumber")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }

    customQuery(
            //language=RoomSql
            """
                INSERT INTO `arrow_scores` (`shootId`, `arrowNumber`, `score`, `isX`)
                SELECT `archerRoundId`, `arrowNumber`, `score`, `isX` 
                FROM arrow_values;
            """
    )

    dropTable("arrow_values")
}

fun DbMigrationDsl.sightMarkMigration() {
    renameTable("sight_marks", "sight_marks_old")

    createTable("sight_marks") {
        addColumn(DbTableColumn("sightMarkId", ColumnType.INTEGER))
        addColumn(DbTableColumn("bowId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(DbTableColumn("distance", ColumnType.INTEGER))
        addColumn(DbTableColumn("isMetric", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("dateSet", ColumnType.CALENDAR))
        addColumn(DbTableColumn("sightMark", ColumnType.REAL))
        addColumn(DbTableColumn("note", ColumnType.TEXT, nullable = true))
        addColumn(DbTableColumn("isMarked", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("isArchived", ColumnType.BOOLEAN))
        addColumn(DbTableColumn("useInPredictions", ColumnType.BOOLEAN))

        singlePrimaryKey("sightMarkId")

        addForeignKey(
                DbTableForeignKey(
                        foreignTableName = "bows",
                        foreignTableColumn = listOf("bowId"),
                        tableColumn = listOf("bowId"),
                        onDelete = ForeignKey.CASCADE,
                )
        )

    }
    customQuery(
            //language=RoomSql
            """
                INSERT INTO `sight_marks` (
                    `sightMarkId`, `bowId`, `distance`, `isMetric`, `dateSet`, `sightMark`, 
                    `note`, `isMarked`, `isArchived`, `useInPredictions`
                )
                SELECT 
                    `id`, `bowId`, `distance`, `isMetric`, `dateSet`, `sightMark`, 
                    `note`, `isMarked`, `isArchived`, `useInPredictions` 
                FROM sight_marks_old;
            """
    )

    dropTable("sight_marks_old")
}

// Note the view migration seems to be sensitive to whitespace changes, copy directly from new schema
//language=RoomSql
private fun DbMigrationDsl.createViews() {
    customQuery(
            "CREATE VIEW `shoots_with_score` AS SELECT \n                    shoot.*, \n                    arrows.score,\n                    shootRound.roundId,\n                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,\n                    ((NOT shootRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete,\n                    ( \n                        -- Find the latest date earlier than or equal to this one that doesn't join with previous\n                        -- This will be the first round (inclusive) in the sequence\n                        SELECT MAX(dateShot)\n                        FROM shoots\n                        WHERE dateShot <= shoot.dateShot AND NOT joinWithPrevious\n                    ) as joinedDate\n                FROM shoots as shoot\n                LEFT JOIN shoot_rounds as shootRound \n                        ON shootRound.shootId = shoot.shootId\n                LEFT JOIN (\n                    SELECT SUM(arrowCount) as count, roundId\n                    FROM round_arrow_counts\n                    GROUP BY roundId\n                ) as roundCount ON shootRound.roundId = roundCount.roundId\n                LEFT JOIN (\n                    SELECT COUNT(*) as count, SUM(score) as score, shootId\n                    FROM arrow_scores\n                    GROUP BY shootId\n                ) as arrows ON shoot.shootId = arrows.shootId"
    )
    customQuery(
            "CREATE VIEW `personal_bests` AS SELECT\n                    pbs.roundId as roundId,\n                    pbs.roundSubTypeId as roundSubTypeId,\n                    pbs.pbScore as score,\n                    COUNT(*) > 1 as isTiedPb\n                FROM shoots_with_score as shoot\n                LEFT JOIN (\n                    SELECT\n                        roundId,\n                        nonNullSubTypeId as roundSubTypeId,\n                        MAX(score) as pbScore\n                    FROM shoots_with_score\n                    WHERE isComplete AND NOT roundId IS NULL\n                    GROUP BY roundId, roundSubTypeId\n                ) as pbs ON shoot.roundId = pbs.roundId AND shoot.nonNullSubTypeId = pbs.roundSubTypeId\n                WHERE shoot.score = pbs.pbScore\n                GROUP BY pbs.roundId, pbs.roundSubTypeId"
    )
}
