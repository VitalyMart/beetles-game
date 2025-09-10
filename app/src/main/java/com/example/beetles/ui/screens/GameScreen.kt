package com.example.beetles.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.ui.graphics.graphicsLayer
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

// Константы для оптимизации
private const val GAME_TICK_MS = 50L
private const val SPAWN_BASE_DELAY_MS = 3000L
private const val MISS_PENALTY = 5
private const val COCKROACH_SCORE = 10
private const val POISONOUS_PENALTY = 20
private const val SCREEN_BOUNDS = 1000f
private const val INSECT_SIZE_DP = 80f

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
    
    // Оптимизированный игровой цикл - объединяем все в один LaunchedEffect
    LaunchedEffect(isGameRunning, isPaused, settings.gameSpeed, difficulty) {
        if (!isGameRunning || gameOver || isPaused) return@LaunchedEffect
        
        var lastSpawnTime = 0L
        var lastGameTimeUpdate = 0L
        val gameStartTime = System.currentTimeMillis()
        
        while (isGameRunning && gameTime > 0 && !isPaused) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - gameStartTime
            
            // Обновление игрового времени (каждую секунду)
            if (currentTime - lastGameTimeUpdate >= 1000) {
                gameTime--
                lastGameTimeUpdate = currentTime
                if (gameTime <= 0) {
                    gameOver = true
                    isGameRunning = false
                    break
                }
            }
            
            // Создание насекомых
            val spawnDelay = (SPAWN_BASE_DELAY_MS - (difficulty - 1) * 200) / settings.gameSpeed
            if (currentTime - lastSpawnTime >= spawnDelay && insects.size < settings.maxCockroaches) {
                val newInsect = createRandomInsect(difficulty)
                insects = insects + newInsect
                lastSpawnTime = currentTime
            }
            
            // Движение насекомых (оптимизированное)
            val speedMultiplier = 0.5f + (difficulty - 1) * 0.25f
            insects = insects.mapNotNull { insect ->
                val newPosition = insect.position + insect.velocity * settings.gameSpeed * speedMultiplier
                // Проверяем границы экрана
                if (newPosition.x > -50 && newPosition.x < SCREEN_BOUNDS && 
                    newPosition.y > -50 && newPosition.y < SCREEN_BOUNDS) {
                    insect.copy(position = newPosition)
                } else null // Удаляем насекомых за границами
            }
            
            delay(GAME_TICK_MS / settings.gameSpeed.toLong())
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
                        score = maxOf(0, score - MISS_PENALTY)
                    }
                }
        ) {
            // Отрисовка насекомых с анимациями
            insects.forEach { insect ->
                val iconRes = when (insect.type) {
                    InsectType.COCKROACH -> R.drawable.cockroach_simple
                    InsectType.POISONOUS -> R.drawable.poisonous_cockroach
                }
                val iconSize = INSECT_SIZE_DP.dp
                
                // Проверяем, что позиция насекомого валидна
                if (insect.position.x >= 0 && insect.position.y >= 0) {
                    // Анимация масштаба для появления насекомого
                    val scale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(300, easing = EaseOutBack),
                        label = "insect_scale"
                    )
                    
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (insect.type == InsectType.POISONOUS) "Ядовитый таракан" else "Таракан",
                        modifier = Modifier
                            .offset(
                                x = with(density) { insect.position.x.toDp() - iconSize / 2 },
                                y = with(density) { insect.position.y.toDp() - iconSize / 2 }
                            )
                            .size(iconSize)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable {
                                if (isGameRunning && !gameOver && !isPaused) {
                                    // Удаляем насекомое
                                    insects = insects.filter { it.id != insect.id }
                                    
                                    // Начисляем очки
                                    when (insect.type) {
                                        InsectType.COCKROACH -> score += COCKROACH_SCORE
                                        InsectType.POISONOUS -> score -= POISONOUS_PENALTY
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

