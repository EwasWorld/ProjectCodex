package eywa.projectcodex.database.migrations.dsl

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.database.migrations.DatabaseMigrations

@DslMarker
annotation class DbMigrationDslMarker

//language=RoomSql
@DbMigrationDslMarker
class DbMigrationDsl(
        private val db: SupportSQLiteDatabase,
) {
    private val sqlStrings = mutableListOf<String>()

    fun renameTable(oldName: String, newName: String) {
        sqlStrings.add("ALTER TABLE $oldName RENAME TO $newName")
    }

    fun createTable(name: String, config: DbCreateTableDsl.() -> Unit) {
        sqlStrings.addAll(DbCreateTableDsl(name).apply { config() }.build())
    }

    fun dropTable(name: String) {
        sqlStrings.add("DROP TABLE `$name`")
    }

    fun dropView(name: String) {
        sqlStrings.add("DROP VIEW `$name`")
    }

    fun customSql(sqlString: String) {
        sqlStrings.add(sqlString)
    }

    fun addColumn(tableName: String, column: DbTableColumn) {
        sqlStrings.add("ALTER TABLE `$tableName` ADD ${column.getDefinition()}")
    }

    fun removeColumn(tableName: String, columnName: String) {
        sqlStrings.add("ALTER TABLE `$tableName` DROP COLUMN $columnName")
    }

    fun renameColumn(tableName: String, oldColumnName: String, newColumnName: String) {
        sqlStrings.add("ALTER TABLE `$tableName` RENAME COLUMN $oldColumnName TO $newColumnName")

        // https://stackoverflow.com/questions/62269319/sqlite-syntax-error-code-1-when-renaming-a-column-name
        // Seems like this is poorly supported therefore should not be used, will need to create a new table instead
        throw UnsupportedOperationException()
    }

    fun addIndex(tableName: String, isUnique: Boolean, columnNames: List<String>) {
        //language=
        val uniqueString = if (isUnique) " UNIQUE" else null
        val indexName = listOf("index", tableName).plus(columnNames).joinToString("_")
        //language=RoomSql
        sqlStrings.add("CREATE$uniqueString INDEX $indexName ON $tableName (${columnNames.joinToString()})")
    }

    fun runQueryImmediately(sqlString: String) = db.query(sqlString)

    companion object {
        fun createMigration(
                from: Int,
                to: Int,
                config: DbMigrationDsl.() -> Unit,
        ) = object : Migration(from, to) {
            override fun migrate(db: SupportSQLiteDatabase) {
                DatabaseMigrations.executeMigrations(
                        sqlStrings = DbMigrationDsl(db).apply(config).sqlStrings,
                        database = db,
                        startVersion = startVersion,
                        endVersion = endVersion,
                )
            }
        }
    }
}

