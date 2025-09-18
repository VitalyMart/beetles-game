package com.example.beetles.models

import androidx.compose.ui.geometry.Offset

data class Insect(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val type: InsectType,
    val size: Float = 50f,
    val isAffectedByGravity: Boolean = false,
    val hasScreamed: Boolean = false
)

enum class InsectType {
    COCKROACH,
    POISONOUS
}

data class Bonus(
    val id: Int,
    val position: Offset,
    val isActive: Boolean = true,
    val type: BonusType = BonusType.GRAVITY
)

enum class BonusType {
    GRAVITY
}
