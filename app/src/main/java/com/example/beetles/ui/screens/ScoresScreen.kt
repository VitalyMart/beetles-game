package com.example.beetles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beetles.BeetleApplication
import com.example.beetles.database.dao.ScoreWithUserName
import com.example.beetles.viewmodel.UserViewModel
import com.example.beetles.viewmodel.UserViewModelFactory

@Composable
fun ScoresScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as BeetleApplication
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(application.repository)
    )
    
    var scores by remember { mutableStateOf<List<ScoreWithUserName>>(emptyList()) }
    
    // Загружаем рекорды
    LaunchedEffect(Unit) {
        userViewModel.getTopScores { topScores ->
            scores = topScores
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Таблица лидеров",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (scores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пока нет рекордов",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Заголовок таблицы
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Место",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Имя",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f)
                    )
                    Text(
                        text = "Очки",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Список рекордов
            LazyColumn {
                itemsIndexed(scores) { index, score ->
                    ScoreRow(
                        position = index + 1,
                        score = score
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreRow(position: Int, score: ScoreWithUserName) {
    val backgroundColor = when (position) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Золотой
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Серебряный
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Бронзовый
        else -> MaterialTheme.colorScheme.surface
    }
    
    val textColor = when (position) {
        1 -> Color(0xFFB8860B) // Темно-золотой
        2 -> Color(0xFF708090) // Темно-серебряный
        3 -> Color(0xFF8B4513) // Темно-бронзовый
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (position <= 3) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Место
            Text(
                text = "$position",
                fontSize = 18.sp,
                fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            
            // Имя игрока
            Text(
                text = score.fullName,
                fontSize = 16.sp,
                fontWeight = if (position <= 3) FontWeight.Medium else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(3f)
            )
            
            // Очки
            Text(
                text = "${score.score}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}