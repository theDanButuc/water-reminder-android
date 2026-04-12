package com.example.waterreminder.util

import com.example.waterreminder.data.db.entity.DrinkType

data class DrinkPreset(val label: String, val amountMl: Int)

val DRINK_TYPE_PRESETS: Map<DrinkType, List<DrinkPreset>> = mapOf(

    DrinkType.WATER to listOf(
        DrinkPreset("100ml", 100),
        DrinkPreset("200ml", 200),
        DrinkPreset("250ml", 250),
        DrinkPreset("330ml", 330),
        DrinkPreset("500ml", 500),
        DrinkPreset("750ml", 750)
    ),

    DrinkType.COFFEE to listOf(
        DrinkPreset("Ristretto", 15),
        DrinkPreset("Espresso", 30),
        DrinkPreset("Lungo", 60),
        DrinkPreset("Americano", 240),
        DrinkPreset("Cappuccino", 150),
        DrinkPreset("Latte", 300)
    ),

    DrinkType.TEA to listOf(
        DrinkPreset("Cup", 200),
        DrinkPreset("Mug", 300),
        DrinkPreset("Carafe", 500),
        DrinkPreset("Large", 750)
    ),

    DrinkType.JUICE to listOf(
        DrinkPreset("Small", 150),
        DrinkPreset("Glass", 250),
        DrinkPreset("Bottle", 330),
        DrinkPreset("Large", 500)
    ),

    DrinkType.MILK to listOf(
        DrinkPreset("Small", 150),
        DrinkPreset("Glass", 250),
        DrinkPreset("Large", 350)
    ),

    DrinkType.ENERGY_DRINK to listOf(
        DrinkPreset("Can", 250),
        DrinkPreset("Large can", 355),
        DrinkPreset("Bottle", 500)
    ),

    DrinkType.BEER to listOf(
        DrinkPreset("Small", 330),
        DrinkPreset("0.5L", 500),
        DrinkPreset("1L", 1000),
        DrinkPreset("2L", 2000)
    ),

    DrinkType.WINE to listOf(
        DrinkPreset("Glass", 150),
        DrinkPreset("Carafe", 500),
        DrinkPreset("Bottle", 750)
    ),

    DrinkType.OTHER to listOf(
        DrinkPreset("Small", 100),
        DrinkPreset("Medium", 250),
        DrinkPreset("Large", 500)
    )
)

fun defaultPresetsFor(type: DrinkType): List<DrinkPreset> =
    DRINK_TYPE_PRESETS[type] ?: DRINK_TYPE_PRESETS[DrinkType.WATER]!!
