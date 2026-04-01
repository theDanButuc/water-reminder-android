package com.example.waterreminder.data.repository

import com.example.waterreminder.data.db.dao.WaterIntakeDao
import com.example.waterreminder.data.db.entity.DrinkType
import com.example.waterreminder.data.db.entity.WaterIntake
import kotlinx.coroutines.flow.Flow

class WaterRepository(private val waterIntakeDao: WaterIntakeDao) {
    suspend fun addWaterIntake(amount: Int, drinkType: DrinkType = DrinkType.WATER) {
        waterIntakeDao.insert(WaterIntake(amount = amount, drinkType = drinkType.name))
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
