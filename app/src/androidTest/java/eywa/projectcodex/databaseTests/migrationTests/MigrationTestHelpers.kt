package eywa.projectcodex.databaseTests.migrationTests

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

object MigrationTestHelpers {
    const val SQL_FALSE = 0
    const val SQL_TRUE = 1

    fun convertBoolean(value: Boolean) = if (value) SQL_TRUE else SQL_FALSE

    fun checkValues(cursor: Cursor, expectedValues: Map<String, Any?>) {
        expectedValues.forEach { (columnName, expectedValue) ->
            checkValue(cursor, columnName, expectedValue)
        }
    }

    private fun checkValue(cursor: Cursor, columnName: String, expectedValue: Any?) {
        when (expectedValue) {
            is Int -> assertEquals(columnName, expectedValue, cursor.getIntOrNull(cursor.getColumnIndex(columnName)))
            is String -> assertEquals(
                    columnName,
                    expectedValue,
                    cursor.getStringOrNull(cursor.getColumnIndex(columnName))
            )

            null -> assertTrue(columnName, cursor.isNull(cursor.getColumnIndex(columnName)))
        }
    }

    @Deprecated("Used for debugging only")
    private fun printValues(cursor: Cursor, expectedValues: Map<String, Any?>) {
        expectedValues.forEach { (columnName, expectedValue) ->
            @Suppress("DEPRECATION")
            printValue(cursor, columnName, expectedValue)
        }
    }

    @Deprecated("Used for debugging only")
    private fun printValue(cursor: Cursor, columnName: String, expectedValue: Any?) {
        val actual = when (expectedValue) {
            is Int -> cursor.getIntOrNull(cursor.getColumnIndex(columnName))
            is String -> cursor.getStringOrNull(cursor.getColumnIndex(columnName))
            null -> cursor.isNull(cursor.getColumnIndex(columnName))
            else -> throw UnsupportedOperationException()
        }

        val equalsText = if (expectedValue == actual) "" else "========================"
        println("ECHDEBUG $equalsText $columnName, exp: ${expectedValue}, act: $actual")
    }

    @Deprecated("Used for debugging only")
    fun printResponses(
            db: SupportSQLiteDatabase,
            sqlQuery: String,
            newValues: List<Map<String, Any?>>,
    ) {
        val response: Cursor = db.query(sqlQuery)

        response.moveToFirst()
        newValues.forEachIndexed { i, it ->
            @Suppress("DEPRECATION")
            printValues(response, it)
            if (response.isLast) {
                assertEquals(newValues.lastIndex, i)
            }
            else {
                response.moveToNext()
            }
        }
    }

    fun checkResponses(
            db: SupportSQLiteDatabase,
            sqlQuery: String,
            newValues: List<Map<String, Any?>>,
    ) {
        val response: Cursor = db.query(sqlQuery)

        assertEquals(newValues.size, response.count)

        response.moveToFirst()
        newValues.forEachIndexed { i, it ->
            checkValues(response, it)
            if (response.isLast) {
                assertEquals(newValues.lastIndex, i)
            }
            else {
                response.moveToNext()
            }
        }
    }
}
