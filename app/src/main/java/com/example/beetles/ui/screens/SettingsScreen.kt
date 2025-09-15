package com.example.beetles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class GameSettings(
    val gameSpeed: Float = 1f,
    val maxCockroaches: Int = 10,
    val bonusInterval: Int = 30,
    val roundDuration: Int = 60
)

@Composable
fun SettingsScreen(
    onSettingsChanged: (GameSettings) -> Unit = {}
) {
    var settings by remember { mutableStateOf(GameSettings()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Настройки игры",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Скорость игры
        Text("Скорость игры: ${"%.1f".format(settings.gameSpeed)}x",
            style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = settings.gameSpeed,
            onValueChange = { settings = settings.copy(gameSpeed = it) },
            valueRange = 0.5f..3f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Максимальное количество тараканов
        Text("Макс. тараканов: ${settings.maxCockroaches}",
            style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = settings.maxCockroaches.toFloat(),
            onValueChange = { settings = settings.copy(maxCockroaches = it.toInt()) },
            valueRange = 5f..30f,
            steps = 25,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Интервал бонусов
        Text("Интервал бонусов: ${settings.bonusInterval} сек",
            style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = settings.bonusInterval.toFloat(),
            onValueChange = { settings = settings.copy(bonusInterval = it.toInt()) },
            valueRange = 10f..60f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Длительность раунда
        Text("Длительность раунда: ${settings.roundDuration} сек",
            style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = settings.roundDuration.toFloat(),
            onValueChange = { settings = settings.copy(roundDuration = it.toInt()) },
            valueRange = 30f..180f,
            steps = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                onSettingsChanged(settings)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить настройки")
        }
    }
}