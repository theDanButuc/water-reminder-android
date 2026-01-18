
 package com.example.waterreminder.data

 import android.content.Context
 import androidx.room.Dao
 import androidx.room.Database
 import androidx.room.Entity
 import androidx.room.Insert
 import androidx.room.PrimaryKey
 import androidx.room.Query
 import androidx.room.Room
 import androidx.room.RoomDatabase
 import kotlinx.coroutines.flow.Flow
 import java.util.Date

 @Entity(tableName = "water_intake")
 data class WaterIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int, // in ml
    val timestamp: Long = System.currentTimeMillis()
 )

 @Dao
 interface WaterIntakeDao {
    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    @Query("SELECT * FROM water_intake WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getIntakeFrom(startTime: Long): Flow<List<WaterIntake>>
 }

 @Database(entities = [WaterIntake::class], version = 1, exportSchema = false)
 abstract class AppDatabase : RoomDatabase() {
    abstract fun waterIntakeDao(): WaterIntakeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_reminder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
 }

 class WaterRepository(private val waterIntakeDao: WaterIntakeDao) {
    suspend fun addWaterIntake(amount: Int) {
        waterIntakeDao.insert(WaterIntake(amount = amount))
    }

    fun getTodayIntake(todayStart: Long): Flow<List<WaterIntake>> {
        return waterIntakeDao.getIntakeFrom(todayStart)
    }

    fun getWeekIntake(weekStart: Long): Flow<List<WaterIntake>> {
        return waterIntakeDao.getIntakeFrom(weekStart)
    }

    fun getMonthIntake(monthStart: Long): Flow<List<WaterIntake>> {
        return waterIntakeDao.getIntakeFrom(monthStart)
    }
 }
