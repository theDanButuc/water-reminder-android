package com.example.waterreminder.data.db.entity

enum class DrinkType(val hydrationFactor: Float, val emoji: String, val displayName: String) {
    WATER(1.0f, "\uD83D\uDCA7", "Water"),
    COFFEE(0.8f, "\u2615", "Coffee"),
    TEA(0.9f, "\uD83C\uDF75", "Tea"),
    JUICE(0.85f, "\uD83E\uDDC3", "Juice"),
    MILK(0.9f, "\uD83E\uDD5B", "Milk"),
    ENERGY_DRINK(0.7f, "\u26A1", "Energy"),
    BEER(0.85f, "\uD83C\uDF3B", "Beer"),
    WINE(0.75f, "\uD83C\uDF77", "Wine"),
    OTHER(0.8f, "\uD83E\uDD64", "Other")
}
