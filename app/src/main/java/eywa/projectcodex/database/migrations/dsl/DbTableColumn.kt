package eywa.projectcodex.database.migrations.dsl

class DbTableColumn(
        private val name: String,
        private val type: DbCreateTableDsl.ColumnType,
        private val nullable: Boolean = false,
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
            if (nullable) null else "NOT NULL",
    ).joinToString(" ")

    /**
     * CREATE INDEX statement
     */
    fun getIndex(tableName: String) =
            if (indexed) listOf(
                    "DROP INDEX IF EXISTS index_${tableName}_${name}",
                    "CREATE INDEX index_${tableName}_${name} ON $tableName ($name)"
            )
            else null
}
