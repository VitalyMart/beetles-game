package com.example.beetles.models

import androidx.compose.ui.geometry.Offset

data class Insect(
    val id: Int,
    val position: Offset,
    val velocity: Offset,
    val type: InsectType,
    val size: Float = 50f
)

enum class InsectType {
    COCKROACH,    // +10 очков
    POISONOUS     // -20 очков
}
