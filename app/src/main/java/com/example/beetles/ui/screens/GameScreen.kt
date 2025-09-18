package com.example.beetles.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.beetles.R
import com.example.beetles.models.Insect
import com.example.beetles.models.InsectType
import com.example.beetles.models.Bonus
import com.example.beetles.models.BonusType
import com.example.beetles.utils.AccelerometerManager
import com.example.beetles.utils.SoundManager
import kotlinx.coroutines.delay
import kotlin.random.Random

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
    var isGameRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var insects by remember { mutableStateOf(listOf<Insect>()) }
    var bonuses by remember { mutableStateOf(listOf<Bonus>()) }
    var gameOver by remember { mutableStateOf(false) }
    var gravityEnabled by remember { mutableStateOf(false) }
    var bonusTimeLeft by remember { mutableStateOf(0) }
    var lastBonusActivation by remember { mutableStateOf(0L) }
    
    val context = LocalContext.current
    val density = LocalDensity.current
    
    val accelerometerManager = remember { AccelerometerManager(context) }
    val soundManager = remember { SoundManager(context) }
    val gravity by accelerometerManager.gravity
    
    LaunchedEffect(gravityEnabled) {
        if (gravityEnabled) accelerometerManager.startListening() else accelerometerManager.stopListening()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            accelerometerManager.stopListening()
            soundManager.release()
        }
    }
    
    LaunchedEffect(Unit) {
        delay(500)
        isGameRunning = true
        gameTime = settings.roundDuration
        score = 0
        insects = emptyList()
        bonuses = emptyList()
        gameOver = false
        isPaused = false
        gravityEnabled = false
        bonusTimeLeft = 0
        lastBonusActivation = 0L
    }
    
    LaunchedEffect(isGameRunning, isPaused, settings.gameSpeed, difficulty) {
        if (!isGameRunning || gameOver || isPaused) return@LaunchedEffect
        
        var lastSpawnTime = 0L
        var lastGameTimeUpdate = 0L
        val gameStartTime = System.currentTimeMillis()
        
        while (isGameRunning && gameTime > 0 && !isPaused) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastGameTimeUpdate >= 1000) {
                gameTime--
                lastGameTimeUpdate = currentTime
                
                if (bonusTimeLeft > 0) {
                    bonusTimeLeft--
                    if (bonusTimeLeft <= 0) {
                        gravityEnabled = false
                        insects = insects.map { it.copy(isAffectedByGravity = false, hasScreamed = false) }
                    }
                }
                
                if (gameTime <= 0) {
                    gameOver = true
                    isGameRunning = false
                    break
                }
            }
            
            val shouldSpawnBonus = if (lastBonusActivation == 0L) {
                currentTime - gameStartTime >= settings.bonusInterval * 1000L
            } else {
                currentTime - lastBonusActivation >= settings.bonusInterval * 1000L
            }
            
            if (shouldSpawnBonus && bonuses.isEmpty()) {
                bonuses = listOf(createRandomBonus())
                soundManager.playBonusSound()
            }
            
            val spawnDelay = (SPAWN_BASE_DELAY_MS - (difficulty - 1) * 200) / settings.gameSpeed
            if (currentTime - lastSpawnTime >= spawnDelay && insects.size < settings.maxCockroaches) {
                insects = insects + createRandomInsect(difficulty, gravityEnabled)
                lastSpawnTime = currentTime
            }
            
            val speedMultiplier = 0.5f + (difficulty - 1) * 0.25f
            insects = insects.mapNotNull { insect ->
                var newVelocity = insect.velocity
                var updatedInsect = insect
                
                if (insect.isAffectedByGravity && gravityEnabled) {
                    val oldVelocityY = newVelocity.y
                    newVelocity += gravity * 2f
                    
                    if (!insect.hasScreamed && newVelocity.y > oldVelocityY + 1f) {
                        soundManager.playBeetleScream()
                        updatedInsect = insect.copy(hasScreamed = true)
                    }
                }
                
                val newPosition = updatedInsect.position + newVelocity * settings.gameSpeed * speedMultiplier
                
                if (newPosition.x > -50 && newPosition.x < SCREEN_BOUNDS && 
                    newPosition.y > -50 && newPosition.y < SCREEN_BOUNDS) {
                    updatedInsect.copy(position = newPosition, velocity = newVelocity)
                } else null
            }
            
            delay(GAME_TICK_MS / settings.gameSpeed.toLong())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Очки: $score", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Время: $gameTime", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Игрок: $playerName", color = Color.White, fontSize = 16.sp)
            
            if (gravityEnabled && bonusTimeLeft > 0) {
                Text("🌟 Гравитация: ${bonusTimeLeft}с", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clickable { 
                    if (isGameRunning && !gameOver && !isPaused) {
                        score = maxOf(0, score - MISS_PENALTY)
                    }
                }
        ) {
            bonuses.forEach { bonus ->
                if (bonus.isActive) {
                    val bonusSize = 60.dp
                    val scale by animateFloatAsState(
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bonus_pulse"
                    )
                    
                    Image(
                        painter = painterResource(id = R.drawable.bonus_gravity),
                        contentDescription = "Бонус гравитации",
                        modifier = Modifier
                            .offset(
                                x = with(density) { bonus.position.x.toDp() - bonusSize / 2 },
                                y = with(density) { bonus.position.y.toDp() - bonusSize / 2 }
                            )
                            .size(bonusSize)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clickable {
                                if (isGameRunning && !gameOver && !isPaused) {
                                    bonuses = bonuses.filter { it.id != bonus.id }
                                    gravityEnabled = true
                                    bonusTimeLeft = 10
                                    lastBonusActivation = System.currentTimeMillis()
                                    insects = insects.map { it.copy(isAffectedByGravity = true, hasScreamed = false) }
                                }
                            }
                    )
                }
            }
            
            insects.forEach { insect ->
                val iconRes = when (insect.type) {
                    InsectType.COCKROACH -> R.drawable.cockroach_simple
                    InsectType.POISONOUS -> R.drawable.poisonous_cockroach
                }
                val iconSize = INSECT_SIZE_DP.dp
                
                if (insect.position.x >= 0 && insect.position.y >= 0) {
                    val scale by animateFloatAsState(1f, tween(300, easing = EaseOutBack), label = "insect_scale")
                    val rotation by animateFloatAsState(
                        if (insect.isAffectedByGravity) 15f else 0f, 
                        tween(500), 
                        label = "insect_rotation"
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
                            .graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = rotation }
                            .clickable {
                                if (isGameRunning && !gameOver && !isPaused) {
                                    insects = insects.filter { it.id != insect.id }
                                    when (insect.type) {
                                        InsectType.COCKROACH -> score += COCKROACH_SCORE
                                        InsectType.POISONOUS -> score -= POISONOUS_PENALTY
                                    }
                                    score = maxOf(0, score)
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
                    Text("Игра окончена!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Финальный счет: $score", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(vertical = 8.dp))
                    Button(
                        onClick = { 
                            gameOver = false
                            isGameRunning = false
                            isPaused = false
                            insects = emptyList()
                            bonuses = emptyList()
                            gravityEnabled = false
                            bonusTimeLeft = 0
                            lastBonusActivation = 0L
                            score = 0
                            gameTime = settings.roundDuration
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
                    Text("ПАУЗА", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Продолжить", color = Color.White)
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    onClick = { isPaused = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Пауза", color = Color.White)
                }
            }
        }
    }
}

private fun createRandomInsect(difficulty: Int, gravityEnabled: Boolean = false): Insect {
    val poisonousChance = when {
        difficulty <= 3 -> 10
        difficulty <= 6 -> 20
        else -> 30
    }
    
    val type = if (Random.nextInt(100) < poisonousChance) InsectType.POISONOUS else InsectType.COCKROACH
    
    return Insect(
        id = Random.nextInt(),
        position = Offset(Random.nextFloat() * 800f, Random.nextFloat() * 600f),
        velocity = Offset((Random.nextFloat() - 0.5f) * 4f, (Random.nextFloat() - 0.5f) * 4f),
        type = type,
        isAffectedByGravity = gravityEnabled,
        hasScreamed = false
    )
}

private fun createRandomBonus(): Bonus = Bonus(
    id = Random.nextInt(),
    position = Offset(Random.nextFloat() * 700f + 50f, Random.nextFloat() * 500f + 50f),
    type = BonusType.GRAVITY
)

