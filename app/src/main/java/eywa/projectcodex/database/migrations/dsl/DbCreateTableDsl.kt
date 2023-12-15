package eywa.projectcodex.database.migrations.dsl

//language=RoomSql
@DbMigrationDslMarker
class DbCreateTableDsl internal constructor(private val tableName: String) {
    private var primaryKey: String? = null
    private val columns = mutableListOf<String>()
    private val foreignKeys = mutableListOf<String>()
    private val indexes = mutableListOf<String>()

    fun addColumn(column: DbTableColumn) {
        columns.add(column.getDefinition())
        column.getIndex(tableName)?.let { indexes.addAll(it) }
    }

    fun addForeignKey(foreignKey: DbTableForeignKey) {
        foreignKeys.add(foreignKey.getDefinition())
    }

    fun singlePrimaryKey(columnName: String) {
        primaryKey = "PRIMARY KEY(`$columnName`)"
    }

    fun compositePrimaryKey(vararg columnNames: String) {
        primaryKey = "CONSTRAINT PK_$tableName PRIMARY KEY (${columnNames.joinToString()})"
    }

    fun build(): List<String> {
        val items = columns.plus(primaryKey!!).plus(foreignKeys).joinToString()
        return listOf("CREATE TABLE `$tableName` ($items)").plus(indexes)
    }

    enum class ColumnType {
        INTEGER,
        TEXT,
        REAL,
        ;

        companion object {
            val CALENDAR = INTEGER
            val ENUM = INTEGER
            val BOOLEAN = INTEGER
            val LIST = TEXT
        }
    }
}
