package eywa.projectcodex.database.migrations.dsl

import androidx.room.ForeignKey

class DbTableForeignKey(
        private val foreignTableName: String,
        private val foreignTableColumn: List<String>,
        private val tableColumn: List<String>,
        private val onDelete: Int = ForeignKey.CASCADE,
        private val onUpdate: Int = ForeignKey.NO_ACTION,
        private val foreignKeyName: String = "fk_${foreignTableColumn.first()}",
) {
    /**
     * Foreign key definition ready for a CREATE statement
     */
    //language=RoomSql
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
            ForeignKey.SET_NULL -> "SET NULL"
            else -> throw IllegalArgumentException()
        }
    }
}
