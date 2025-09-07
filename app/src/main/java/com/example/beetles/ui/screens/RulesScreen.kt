package com.example.beetles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.InputStream

@Composable
fun RulesScreen() {
    val context = LocalContext.current
    val rulesText = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val inputStream: InputStream = context.assets.open("rules.txt")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            rulesText.value = String(buffer)
        } catch (e: Exception) {
            rulesText.value = """
                Правила игры "Тараканы":

                Цель игры:
                - Уничтожайте тараканов, нажимая на них
                - Собирайте бонусы для увеличения счета
                - Избегайте препятствий

                Управление:
                - Тап по таракану - убить (+10 очков)
                - Тап по бонусу - собрать (+50 очков)
                - Избегайте ядовитых тараканов (-20 очков)

                Уровни сложности:
                - Легкий: медленные тараканы
                - Средний: обычная скорость
                - Сложный: быстрые тараканы

                Удачи в игре!
            """.trimIndent()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Правила игры",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = rulesText.value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify
        )
    }
}