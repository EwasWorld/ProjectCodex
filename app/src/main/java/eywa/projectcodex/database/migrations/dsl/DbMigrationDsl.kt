package eywa.projectcodex.database.migrations.dsl

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.database.migrations.DatabaseMigrations

@DslMarker
annotation class DbMigrationDslMarker

//language=RoomSql
@DbMigrationDslMarker
class DbMigrationDsl {
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

    fun customQuery(sqlString: String) {
        sqlStrings.add(sqlString)
    }

    fun addColumn(tableName: String, column: DbTableColumn) {
        sqlStrings.add("ALTER TABLE `$tableName` ADD ${column.getDefinition()}")
    }

    fun renameColumn(tableName: String, oldColumnName: String, newColumnName: String) {
        sqlStrings.add("ALTER TABLE `$tableName` RENAME COLUMN $oldColumnName TO $newColumnName")
    }

    companion object {
        fun createMigration(from: Int, to: Int, config: DbMigrationDsl.() -> Unit) = object : Migration(from, to) {
            override fun migrate(database: SupportSQLiteDatabase) {
                DatabaseMigrations.executeMigrations(
                        DbMigrationDsl().apply(config).sqlStrings,
                        database,
                        startVersion,
                        endVersion,
                )
            }
        }
    }
}

