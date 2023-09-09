package eywa.projectcodex.database.archer

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DatabaseArcherHandicap.Companion.TABLE_NAME
import eywa.projectcodex.database.shootData.DatabaseShoot
import java.util.Calendar

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseArcher::class,
                    parentColumns = ["archerId"],
                    childColumns = ["archerId"],
                    onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
        ],
)
data class DatabaseArcherHandicap(
        @PrimaryKey(autoGenerate = true) val archerHandicapId: Int,
        @ColumnInfo(index = true) val archerId: Int,
        val bowStyle: ClassificationBow,
        val handicapType: HandicapType,
        val handicap: Int,
        val dateSet: Calendar,
        /**
         * The id of the [DatabaseShoot] that caused the handicap to update (if any)
         */
        @ColumnInfo(index = true) val shootId: Int? = null,
) {
    companion object {
        const val TABLE_NAME = "archer_handicaps"
    }
}

enum class HandicapType(@StringRes val text: Int) {
    OUTDOOR(R.string.archer_handicaps__type_outdoor),
    INDOOR(R.string.archer_handicaps__type_indoor),
    OUTDOOR_TOURNAMENT(R.string.archer_handicaps__type_outdoor_tournament),
    INDOOR_TOURNAMENT(R.string.archer_handicaps__type_indoor_tournament),
    ;

    fun toDbData() = ordinal

    companion object {
        fun fromDbData(value: Int) = values()[value]
    }
}
