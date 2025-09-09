package com.example.beetles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.beetles.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var showGame by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }
    var playerDifficulty by remember { mutableStateOf(5) }
    var gameSettings by remember { mutableStateOf(GameSettings()) }
    val tabs = listOf("Регистрация", "Правила", "Авторы", "Настройки")

    if (showGame) {
        GameScreen(
            onBack = { showGame = false },
            playerName = playerName,
            settings = gameSettings,
            difficulty = playerDifficulty
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Игра Жуки") }
                )
            },
            content = { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title) },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> RegistrationScreen(
                            onStartGame = { name, difficulty ->
                                playerName = name
                                playerDifficulty = difficulty
                                showGame = true
                            }
                        )
                        1 -> RulesScreen()
                        2 -> AuthorsScreen()
                        3 -> SettingsScreen(
                            onSettingsChanged = { settings ->
                                gameSettings = settings
                            }
                        )
                    }
                }
            }
        )
    }
}