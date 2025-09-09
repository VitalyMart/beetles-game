package com.example.beetles.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beetles.R
import com.example.beetles.models.Insect
import com.example.beetles.models.InsectType
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(
    onBack: () -> Unit,
    playerName: String = "Игрок",
    settings: GameSettings = GameSettings(),
    difficulty: Int = 5
) {
    var score by remember { mutableStateOf(0) }
    var gameTime by remember { mutableStateOf(settings.roundDuration) }
    var isGameRunning by remember { mutableStateOf(false) } // Игра не начинается сразу
    var isPaused by remember { mutableStateOf(false) }
    var insects by remember { mutableStateOf(listOf<Insect>()) }
    var gameOver by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    
    // Автоматический запуск игры при открытии экрана
    LaunchedEffect(Unit) {
        delay(500) // Небольшая задержка для загрузки UI
        isGameRunning = true
        gameTime = settings.roundDuration
        score = 0
        insects = emptyList()
        gameOver = false
        isPaused = false
    }
    
    // Запуск игры
    LaunchedEffect(isGameRunning, isPaused) {
        if (isGameRunning && !gameOver && !isPaused) {
            while (gameTime > 0 && isGameRunning && !isPaused) {
                delay(1000)
                gameTime--
            }
            if (gameTime <= 0) {
                gameOver = true
                isGameRunning = false
            }
        }
    }
    
    // Создание насекомых
    LaunchedEffect(isGameRunning, isPaused) {
        if (isGameRunning && !gameOver && !isPaused) {
            while (isGameRunning && gameTime > 0 && !isPaused) {
                try {
                    // Сложность влияет на частоту появления насекомых (1-10 -> 3000-1000мс)
                    val spawnDelay = (3000 - (difficulty - 1) * 200) / settings.gameSpeed
                    delay(spawnDelay.toLong())
                    if (insects.size < settings.maxCockroaches) {
                        val newInsect = createRandomInsect(difficulty)
                        insects = insects + newInsect
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки создания насекомых
                    delay(1000)
                }
            }
        }
    }
    
    // Движение насекомых
    LaunchedEffect(insects, isGameRunning, isPaused) {
        if (isGameRunning && !gameOver && !isPaused) {
            while (isGameRunning && gameTime > 0 && !isPaused) {
                try {
                    delay((50 / settings.gameSpeed).toLong()) // Учитываем скорость игры
                    insects = insects.map { insect ->
                        // Сложность влияет на скорость движения (1-10 -> 0.5-3.0x)
                        val speedMultiplier = 0.5f + (difficulty - 1) * 0.25f
                        val newPosition = insect.position + insect.velocity * settings.gameSpeed * speedMultiplier
                        insect.copy(position = newPosition)
                    }.filter { insect ->
                        // Удаляем насекомых, которые вышли за экран
                        insect.position.x > -50 && insect.position.x < 1000 && 
                        insect.position.y > -50 && insect.position.y < 1000
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки движения насекомых
                    delay(100)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32))
    ) {
        // Верхняя панель с информацией
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Очки: $score",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Время: $gameTime",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = "Игрок: $playerName",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Игровое поле
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable { 
                    // Штраф за промах
                    if (isGameRunning && !gameOver && !isPaused) {
                        score = maxOf(0, score - 5)
                    }
                }
        ) {
            // Отрисовка насекомых
            insects.forEach { insect ->
                val iconRes = when (insect.type) {
                    InsectType.COCKROACH -> R.drawable.cockroach_simple // Используем простую иконку
                    InsectType.POISONOUS -> R.drawable.poisonous_cockroach
                }
                val iconSize = 80.dp // Увеличил размер до 80dp
                
                // Проверяем, что позиция насекомого валидна
                if (insect.position.x >= 0 && insect.position.y >= 0) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (insect.type == InsectType.POISONOUS) "Ядовитый таракан" else "Таракан",
                        modifier = Modifier
                            .offset(
                                x = with(density) { insect.position.x.toDp() - iconSize / 2 },
                                y = with(density) { insect.position.y.toDp() - iconSize / 2 }
                            )
                            .size(iconSize)
                            .clickable {
                                if (isGameRunning && !gameOver && !isPaused) {
                                    // Удаляем насекомое
                                    insects = insects.filter { it.id != insect.id }
                                    
                                    // Начисляем очки
                                    score += when (insect.type) {
                                        InsectType.COCKROACH -> 10
                                        InsectType.POISONOUS -> -20
                                    }
                                    score = maxOf(0, score) // Не даем уйти в минус
                                }
                            }
                    )
                }
            }
            
            if (gameOver) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Игра окончена!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Финальный счет: $score",
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { 
                            gameOver = false
                            isGameRunning = false
                            isPaused = false
                            insects = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Играть снова", color = Color.White)
                    }
                }
            }
            
            if (isPaused) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ПАУЗА",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { 
                            isPaused = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Продолжить", color = Color.White)
                    }
                }
            }
        }
        
        // Нижняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Назад", color = Color.White)
            }
            
            if (isGameRunning && !gameOver && !isPaused) {
                Button(
                    onClick = { 
                        isPaused = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Пауза", color = Color.White)
                }
            }
        }
    }
}

private fun createRandomInsect(difficulty: Int): Insect {
    // Сложность влияет на вероятность появления ядовитых тараканов
    val poisonousChance = when {
        difficulty <= 3 -> 10  // Легко: 10% ядовитых
        difficulty <= 6 -> 20  // Средне: 20% ядовитых
        else -> 30             // Сложно: 30% ядовитых
    }
    
    val type = when (Random.nextInt(100)) {
        in 0..(100 - poisonousChance - 1) -> InsectType.COCKROACH
        else -> InsectType.POISONOUS
    }
    
    val startX = Random.nextFloat() * 800f
    val startY = Random.nextFloat() * 600f
    val velocityX = (Random.nextFloat() - 0.5f) * 4f
    val velocityY = (Random.nextFloat() - 0.5f) * 4f
    
    return Insect(
        id = Random.nextInt(),
        position = Offset(startX, startY),
        velocity = Offset(velocityX, velocityY),
        type = type
    )
}

