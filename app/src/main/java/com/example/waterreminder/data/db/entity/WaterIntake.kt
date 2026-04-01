package com.example.waterreminder.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intake")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val drinkType: String = DrinkType.WATER.name,
    val presetLabel: String? = null
) {
    val type: DrinkType
        get() = try {
            DrinkType.valueOf(drinkType)
        } catch (_: IllegalArgumentException) {
            DrinkType.WATER
        }

    val effectiveAmount: Int
        get() = (amount * type.hydrationFactor).toInt()

    val displayLabel: String
        get() = presetLabel ?: "${amount}ml"
}
