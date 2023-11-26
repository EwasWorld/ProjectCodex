package eywa.projectcodex.database.migrations

import eywa.projectcodex.database.migrations.DbMigrationDsl.CreateTableDsl.ColumnType
import eywa.projectcodex.database.migrations.DbMigrationDsl.TableColumn
import eywa.projectcodex.database.migrations.DbMigrationDsl.TableForeignKey

val MIGRATION_10_11 = DbMigrationDsl.createMigration(10, 11) {
    bowMigration()
    archerMigration()

    viewMigration()
}

private fun DbMigrationDsl.bowMigration() {
    renameTable("bows", "bows_old")

    createTable("bows") {
        // TODO Rename id to bowId
        addColumn(TableColumn("id", ColumnType.INTEGER))
        addColumn(TableColumn("name", ColumnType.TEXT, default = "'Default'"))
        addColumn(TableColumn("description", ColumnType.TEXT, isNullable = true, default = "NULL"))
        addColumn(TableColumn("type", ColumnType.INTEGER))
        addColumn(TableColumn("isSightMarkDiagramHighestAtTop", ColumnType.INTEGER))
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
    // TODO Add database shoot back in

    addColumn("archers", TableColumn("isGent", ColumnType.INTEGER, default = "1"))
    addColumn("archers", TableColumn("age", ColumnType.INTEGER, default = "1"))

    createTable("archer_handicaps") {
        addColumn(TableColumn("archerHandicapId", ColumnType.INTEGER))
        addColumn(TableColumn("archerId", ColumnType.INTEGER, indexed = true))
        addColumn(TableColumn("bowStyle", ColumnType.INTEGER))
        addColumn(TableColumn("handicapType", ColumnType.INTEGER))
        addColumn(TableColumn("handicap", ColumnType.INTEGER))
        addColumn(TableColumn("dateSet", ColumnType.INTEGER))
        addColumn(TableColumn("shootId", ColumnType.INTEGER, isNullable = true, indexed = true))
        addForeignKey(
                TableForeignKey(
                        foreignTableName = "archers",
                        foreignTableColumn = listOf("archerId"),
                        tableColumn = listOf("archerId"),
                )
        )
        singlePrimaryKey("archerHandicapId")
    }
}

// TODO Db schema for 10 needs rerecording, I'm pretty sure it's not up to date
// Note the view migration seems to be sensitive to whitespace changes, copy directly from new schema
private fun DbMigrationDsl.viewMigration() {
    dropView("completed_round_scores")
    customQuery(
            "CREATE VIEW `completed_round_scores` AS SELECT \n                    archerRound.*, \n                    arrows.score,\n                    (CASE WHEN roundSubTypeId IS NULL THEN 1 else roundSubTypeId END) as nonNullSubTypeId,\n                    ((NOT archerRound.roundId IS NULL) AND arrows.count = roundCount.count) as isComplete,\n                    ( \n                        -- Find the latest date earlier than or equal to this one that doesn't join with previous\n                        -- This will be the first round (inclusive) in the sequence\n                        SELECT MAX(dateShot)\n                        FROM archer_rounds\n                        WHERE dateShot <= archerRound.dateShot AND NOT joinWithPrevious\n                    ) as joinedDate\n                FROM archer_rounds as archerRound\n                LEFT JOIN (\n                    SELECT SUM(arrowCount) as count, roundId\n                    FROM round_arrow_counts\n                    GROUP BY roundId\n                ) as roundCount ON archerRound.roundId = roundCount.roundId\n                LEFT JOIN (\n                    SELECT COUNT(*) as count, SUM(score) as score, archerRoundId\n                    FROM arrow_values\n                    GROUP BY archerRoundId\n                ) as arrows ON archerRound.archerRoundId = arrows.archerRoundId"
    )
}
