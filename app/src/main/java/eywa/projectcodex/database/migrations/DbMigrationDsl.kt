package eywa.projectcodex.database.migrations

import androidx.room.ForeignKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eywa.projectcodex.database.migrations.DbMigrationDsl.CreateTableDsl.ColumnType.INTEGER

@DslMarker
annotation class DbMigrationDslMarker

@DbMigrationDslMarker
class DbMigrationDsl {
    private val sqlStrings = mutableListOf<String>()

    fun renameTable(oldName: String, newName: String) {
        sqlStrings.add("ALTER TABLE $oldName RENAME TO $newName")
    }

    fun createTable(name: String, config: CreateTableDsl.() -> Unit) {
        sqlStrings.addAll(CreateTableDsl(name).apply { config() }.build())
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

    fun addColumn(tableName: String, column: TableColumn) {
        sqlStrings.add("ALTER TABLE `$tableName` ADD ${column.getDefinition()}")
    }

    class TableColumn(
            private val name: String,
            private val type: CreateTableDsl.ColumnType,
            private val isNullable: Boolean = false,
            private val default: String? = null,
            private val indexed: Boolean = false,
    ) {
        /**
         * Column definition ready for a CREATE statement
         */
        fun getDefinition() = listOfNotNull(
                "`$name`",
                type.name,
                default?.let { "DEFAULT $default" },
                if (isNullable) null else "NOT NULL",
        ).joinToString(" ")

        /**
         * CREATE INDEX statement
         */
        fun getIndex(tableName: String) =
                if (indexed) "CREATE INDEX index_${tableName}_${name} ON $tableName ($name)"
                else null
    }

    class TableForeignKey(
            private val foreignTableName: String,
            private val foreignTableColumn: List<String>,
            private val tableColumn: List<String>,
            private val onDelete: Int = ForeignKey.CASCADE,
            private val onUpdate: Int = ForeignKey.NO_ACTION,
            private val foreignKeyName: String = "fk_${foreignTableColumn.first()}",
    ) {
        fun getDefinition() =
                """
                    CONSTRAINT $foreignKeyName 
                        FOREIGN KEY(${tableColumn.joinToString()})
                        REFERENCES $foreignTableName(${foreignTableColumn.joinToString()})
                        ON DELETE ${onDelete.asTypeString()}
                        ON UPDATE ${onUpdate.asTypeString()}
                    
                """.trimIndent()

        companion object {
            fun Int.asTypeString() = when (this) {
                ForeignKey.CASCADE -> "CASCADE"
                ForeignKey.NO_ACTION -> "NO ACTION"
                else -> throw IllegalArgumentException()
            }//.let { "'$it'" }
        }
    }

    @DbMigrationDslMarker
    class CreateTableDsl internal constructor(private val tableName: String) {
        private var primaryKey: String? = null
        private val columns = mutableListOf<String>()
        private val foreignKeys = mutableListOf<String>()
        private val indexes = mutableListOf<String>()

        fun addColumn(column: TableColumn) {
            columns.add(column.getDefinition())
            column.getIndex(tableName)?.let { indexes.add(it) }
        }

        fun addForeignKey(foreignKey: TableForeignKey) {
            foreignKeys.add(foreignKey.getDefinition())
        }

        fun singlePrimaryKey(columnName: String) {
            primaryKey = "PRIMARY KEY(`$columnName`)"
        }

        fun compositePrimaryKey(columnNames: List<String>) {
            primaryKey = "CONSTRAINT PK_$tableName PRIMARY KEY (${columnNames.joinToString()})"
        }

        fun build(): List<String> {
            val items = columns.plus(primaryKey!!).plus(foreignKeys).joinToString()
            return listOf("CREATE TABLE `$tableName` ($items)").plus(indexes)
        }

        /**
         * Use [INTEGER] for Calendar
         */
        enum class ColumnType {
            INTEGER, TEXT, REAL
        }
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
