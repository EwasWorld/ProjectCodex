package eywa.projectcodex.common

class CommonStrings {
    companion object {
        const val testDatabaseName = "test_database"
        const val testSharedPrefsName = "test_prefs"
    }

    class Menus {
        companion object {
            const val scorePadInsertEnd = "Insert end above"
            const val scorePadDeleteEnd = "Delete end"
            const val scorePadEditEnd = "Edit end"

            const val viewRoundsShowScorePad = "Show score pad"
            const val viewRoundsContinue = "Continue round"
            const val viewRoundsDelete = "Delete round"
            const val viewRoundsConvert = "Convert"
            const val viewRoundsConvertToTens = "Xs to 10s"
            const val viewRoundsConvertToFiveZone = "10-zone to 5-zone"
        }
    }

    class Dialogs {
        companion object {
            const val emptyTable = "Table is empty"
            const val inputEndRoundComplete = "Round Complete"
            const val viewRoundsConvertTitle = "Convert score"
        }
    }
}