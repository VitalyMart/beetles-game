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
    val tabs = listOf("Регистрация", "Правила", "Авторы", "Настройки")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игра Тараканы") }
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
                    0 -> RegistrationScreen()
                    1 -> RulesScreen()
                    2 -> AuthorsScreen()
                    3 -> SettingsScreen()
                }
            }
        }
    )
}