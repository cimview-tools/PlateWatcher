package be.habran.platewatcher

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "plate_records")
data class PlateRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plate: String,
    val country: String?,
    val confidence: Float,
    val detectedAt: Long,
    val imagePath: String?
)

@Dao
interface PlateDao {
    @Insert
    fun insert(record: PlateRecord)

    @Query("SELECT * FROM plate_records ORDER BY detectedAt DESC")
    fun getAll(): List<PlateRecord>

    @Query("DELETE FROM plate_records WHERE detectedAt < :limit")
    fun deleteOlderThan(limit: Long)
}

@Database(entities = [PlateRecord::class], version = 1, exportSchema = false)
abstract class PlateDatabase : RoomDatabase() {
    abstract fun plateDao(): PlateDao

    companion object {
        fun create(context: Context): PlateDatabase = Room.databaseBuilder(
            context.applicationContext,
            PlateDatabase::class.java,
            "plates.db"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }
}
