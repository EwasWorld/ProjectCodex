package eywa.projectcodex.database.migrations

import androidx.room.ForeignKey
import eywa.projectcodex.database.migrations.DbMigrationDsl.CreateTableDsl.ColumnType
import eywa.projectcodex.database.migrations.DbMigrationDsl.TableColumn
import eywa.projectcodex.database.migrations.DbMigrationDsl.TableForeignKey

val MIGRATION_10_11 = DbMigrationDsl.createMigration(10, 11) {
    dropView("completed_round_scores")
    dropView("personal_bests")

    bowMigration()
    archerMigration()
    shootSubTablesMigration()
    shootMigration()
    arrowMigration()

    createViews()
}

private fun DbMigrationDsl.bowMigration() {
    renameTable("bows", "bows_old")

    createTable("bows") {
        // TODO Rename id to bowId
        addColumn(TableColumn("id", ColumnType.INTEGER))
        addColumn(TableColumn("name", ColumnType.TEXT, default = "'Default'"))
        addColumn(TableColumn("description", ColumnType.TEXT, nullable = true, default = "NULL"))
        addColumn(TableColumn("type", ColumnType.ENUM))
        addColumn(TableColumn("isSightMarkDiagramHighestAtTop", ColumnType.BOOLEAN))
        singlePrimaryKey("id")
    }

    customQuery(
            """
                INSERT INTO `bows` (`id`, `name`, `description`, `type`, `isSightMarkDiagramHighestAtTop`)
                SELECT `id`, "Default", NULL, 0, `isSightMarkDiagramHighestAtTop` 
                FROM bows_old;
            """
    )

    dropTable("bows_old")
}

private fun DbMigrationDsl.archerMigration() {
    addColumn("archers", TableColumn("isGent", ColumnType.BOOLEAN, default = "1"))
    addColumn("archers", TableColumn("age", ColumnType.ENUM, default = "1"))

    createTable("archer_handicaps") {
        addColumn(TableColumn("archerHandicapId", ColumnType.INTEGER))
        addColumn(TableColumn("archerId", ColumnType.INTEGER, indexed = true))
        addColumn(TableColumn("bowStyle", ColumnType.BOOLEAN))
        addColumn(TableColumn("handicapType", ColumnType.BOOLEAN))
        addColumn(TableColumn("handicap", ColumnType.INTEGER))
        addColumn(TableColumn("dateSet", ColumnType.CALENDAR))
        addColumn(TableColumn("shootId", ColumnType.INTEGER, nullable = true, indexed = true))
        addForeignKey(
                TableForeignKey(
                        foreignTableName = "archers",
                        foreignTableColumn = listOf("archerId"),
                        tableColumn = listOf("archerId"),
                )
        )
        addForeignKey(
                TableForeignKey(
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
        addColumn(TableColumn("shootId", ColumnType.INTEGER))
        addColumn(TableColumn("roundId", ColumnType.INTEGER, indexed = true))
        addColumn(TableColumn("roundSubTypeId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(TableColumn("faces", ColumnType.LIST, nullable = true))
        addColumn(TableColumn("sightersCount", ColumnType.INTEGER))

        singlePrimaryKey("shootId")

        addForeignKey(
                TableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
        addForeignKey(
                TableForeignKey(
                        foreignTableName = "round_sub_types",
                        foreignTableColumn = listOf("roundId", "subTypeId"),
                        tableColumn = listOf("roundId", "roundSubTypeId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
        addForeignKey(
                TableForeignKey(
                        foreignTableName = "rounds",
                        foreignTableColumn = listOf("roundId"),
                        tableColumn = listOf("roundId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
    }
    // TODO Move data from archerRounds to shoot_rounds

    createTable("shoot_details") {
        addColumn(TableColumn("shootId", ColumnType.INTEGER))
        addColumn(TableColumn("face", ColumnType.ENUM, nullable = true))
        addColumn(TableColumn("distance", ColumnType.INTEGER, nullable = true))
        addColumn(TableColumn("isDistanceInMeters", ColumnType.BOOLEAN))
        addColumn(TableColumn("faceSizeInCm", ColumnType.REAL, nullable = true))

        singlePrimaryKey("shootId")

        addForeignKey(
                TableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }
}

private fun DbMigrationDsl.shootMigration() {
//    renameColumn("archer_rounds", "archerRoundId", "shootId")

    createTable("shoots") {
        addColumn(TableColumn("shootId", ColumnType.INTEGER))
        addColumn(TableColumn("dateShot", ColumnType.CALENDAR))
        addColumn(TableColumn("archerId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(TableColumn("countsTowardsHandicap", ColumnType.BOOLEAN))
        addColumn(TableColumn("bowId", ColumnType.INTEGER, nullable = true, indexed = true))
        addColumn(TableColumn("goalScore", ColumnType.INTEGER, nullable = true))
        addColumn(TableColumn("shootStatus", ColumnType.TEXT, nullable = true))
        addColumn(TableColumn("joinWithPrevious", ColumnType.BOOLEAN, default = "0"))

        singlePrimaryKey("shootId")

        addForeignKey(
                TableForeignKey(
                        foreignTableName = "bows",
                        foreignTableColumn = listOf("id"),
                        tableColumn = listOf("bowId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
        addForeignKey(
                TableForeignKey(
                        foreignTableName = "archers",
                        foreignTableColumn = listOf("archerId"),
                        tableColumn = listOf("archerId"),
                        onDelete = ForeignKey.SET_NULL,
                )
        )
    }
    // TODO Move data from archerRounds to shoots
    dropTable("archer_rounds")
}

private fun DbMigrationDsl.arrowMigration() {
    createTable("arrow_counters") {
        addColumn(TableColumn("shootId", ColumnType.INTEGER))
        addColumn(TableColumn("shotCount", ColumnType.INTEGER))

        singlePrimaryKey("shootId")

        addForeignKey(
                TableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }

    renameTable("arrow_values", "arrow_values_old")
    createTable("arrow_values") {
        addColumn(TableColumn("shootId", ColumnType.INTEGER))
        addColumn(TableColumn("arrowNumber", ColumnType.INTEGER))
        addColumn(TableColumn("score", ColumnType.INTEGER))
        addColumn(TableColumn("isX", ColumnType.BOOLEAN))

        compositePrimaryKey("shootId", "arrowNumber")

        addForeignKey(
                TableForeignKey(
                        foreignTableName = "shoots",
                        foreignTableColumn = listOf("shootId"),
                        tableColumn = listOf("shootId"),
                )
        )
    }
    // TODO Move data
    dropTable("arrow_values_old")
}

// TODO Db schema for 10 needs rerecording, I'm pretty sure it's not up to date
// Note the view migration seems to be sensitive to whitespace changes, copy directly from new schema
private fun DbMigrationDsl.createViews() {
    customQuery(
            "CREATE VIEW `completed_round_scores` AS SELECT \n                    shoot.*, \n                    arrows.score,\n                    shootRound.roundId,\n                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,\n                    ((NOT shootRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete,\n                    ( \n                        -- Find the latest date earlier than or equal to this one that doesn't join with previous\n                        -- This will be the first round (inclusive) in the sequence\n                        SELECT MAX(dateShot)\n                        FROM shoots\n                        WHERE dateShot <= shoot.dateShot AND NOT joinWithPrevious\n                    ) as joinedDate\n                FROM shoots as shoot\n                LEFT JOIN shoot_rounds as shootRound \n                        ON shootRound.shootId = shoot.shootId\n                LEFT JOIN (\n                    SELECT SUM(arrowCount) as count, roundId\n                    FROM round_arrow_counts\n                    GROUP BY roundId\n                ) as roundCount ON shootRound.roundId = roundCount.roundId\n                LEFT JOIN (\n                    SELECT COUNT(*) as count, SUM(score) as score, shootId\n                    FROM arrow_values\n                    GROUP BY shootId\n                ) as arrows ON shoot.shootId = arrows.shootId"
    )
    customQuery(
            "CREATE VIEW `personal_bests` AS SELECT\n                    pbs.roundId as roundId,\n                    pbs.roundSubTypeId as roundSubTypeId,\n                    pbs.pbScore as score,\n                    COUNT(*) > 1 as isTiedPb\n                FROM completed_round_scores as shoot\n                LEFT JOIN (\n                    SELECT\n                        roundId,\n                        nonNullSubTypeId as roundSubTypeId,\n                        MAX(score) as pbScore\n                    FROM completed_round_scores\n                    WHERE isComplete AND NOT roundId IS NULL\n                    GROUP BY roundId, roundSubTypeId\n                ) as pbs ON shoot.roundId = pbs.roundId AND shoot.nonNullSubTypeId = pbs.roundSubTypeId\n                WHERE shoot.score = pbs.pbScore\n                GROUP BY pbs.roundId, pbs.roundSubTypeId"
    )
}
