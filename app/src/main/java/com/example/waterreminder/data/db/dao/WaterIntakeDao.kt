package com.example.waterreminder.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.waterreminder.data.db.entity.WaterIntake
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    @Query("SELECT * FROM water_intake WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getIntakeFrom(startTime: Long): Flow<List<WaterIntake>>

    @Query("DELETE FROM water_intake WHERE timestamp >= :startTime")
    suspend fun deleteFrom(startTime: Long)

    @Query("SELECT * FROM water_intake ORDER BY timestamp ASC")
    fun getAllIntake(): Flow<List<WaterIntake>>
}
