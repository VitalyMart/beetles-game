package com.example.beetles.ui.screens

import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beetles.BeetleApplication
import com.example.beetles.database.entities.User
import com.example.beetles.models.Player
import com.example.beetles.viewmodel.UserViewModel
import com.example.beetles.viewmodel.UserViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onStartGame: (User, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val application = context.applicationContext as BeetleApplication
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(application.repository)
    )
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("1 курс") }
    var difficulty by remember { mutableStateOf(5) }
    var birthDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showResult by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс", "5 курс", "6 курс")
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация игрока",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Пол:", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RadioButton(
                selected = gender == "Мужской",
                onClick = { gender = "Мужской" }
            )
            Text("Мужской", modifier = Modifier.align(Alignment.CenterVertically))

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(
                selected = gender == "Женский",
                onClick = { gender = "Женский" }
            )
            Text("Женский", modifier = Modifier.align(Alignment.CenterVertically))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Курс:", style = MaterialTheme.typography.bodyLarge)
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCourse,
                onValueChange = {},
                label = { Text("Выберите курс") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                courses.forEach { course ->
                    DropdownMenuItem(
                        text = { Text(course) },
                        onClick = {
                            selectedCourse = course
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Уровень сложности: $difficulty/10", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = difficulty.toFloat(),
            onValueChange = { difficulty = it.toInt() },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Дата рождения:", style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = { showDatePickerDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Выбрать дату: ${formatDate(birthDate)}")
        }

        if (showDatePickerDialog) {
            CustomDatePickerDialog(
                onDismissRequest = { showDatePickerDialog = false },
                onDateSelected = { calendar ->
                    birthDate = calendar
                    showDatePickerDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Проверяем, существует ли пользователь с таким ФИО
                userViewModel.getUserByName(fullName) { existingUser ->
                    if (existingUser != null) {
                        // Используем существующего пользователя
                        currentUser = existingUser
                        showResult = true
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    } else {
                        // Создаем нового пользователя
                        val zodiacSign = calculateZodiacSign(birthDate)
                        val newUser = User(
                            fullName = fullName,
                            gender = gender,
                            course = selectedCourse,
                            birthDate = formatDate(birthDate),
                            zodiacSign = zodiacSign
                        )
                        userViewModel.insertUser(newUser) { userId ->
                            currentUser = newUser.copy(id = userId)
                            showResult = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = fullName.isNotBlank() && gender.isNotBlank()
        ) {
            Text("Зарегистрироваться")
        }

        if (showResult && currentUser != null) {
            Spacer(modifier = Modifier.height(24.dp))
            UserInfoCard(user = currentUser!!)

            // Кнопка для начала игры
            Button(
                onClick = {
                    onStartGame(currentUser!!, difficulty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Начать игру", color = Color.White)
            }

            // Кнопка для возврата к форме
            Button(
                onClick = {
                    showResult = false
                    currentUser = null
                    // Прокрутка обратно к верху
                    coroutineScope.launch {
                        scrollState.animateScrollTo(0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Вернуться к форме")
            }
        }

        // Добавляем дополнительный отступ внизу для удобства прокрутки
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CustomDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Calendar) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(calendar)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Отмена")
            }
        },
        title = { Text("Выберите дату рождения") },
        text = {
            AndroidView(
                factory = { ctx ->
                    DatePicker(ctx).apply {
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        init(year, month, day) { _, selectedYear, selectedMonth, selectedDay ->
                            calendar.set(selectedYear, selectedMonth, selectedDay)
                        }
                    }
                },
                modifier = Modifier.wrapContentSize()
            )
        }
    )
}

@Composable
fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Данные игрока:",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PlayerInfoText("ФИО: ${user.fullName}")
            PlayerInfoText("Пол: ${user.gender}")
            PlayerInfoText("Курс: ${user.course}")
            PlayerInfoText("Дата рождения: ${user.birthDate}")
            PlayerInfoText("Знак зодиака: ${user.zodiacSign}")
        }
    }
}

@Composable
fun PlayerInfoText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

fun formatDate(calendar: Calendar): String {
    return "${calendar.get(Calendar.DAY_OF_MONTH)}." +
            "${calendar.get(Calendar.MONTH) + 1}.${calendar.get(Calendar.YEAR)}"
}

fun calculateZodiacSign(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1

    return when (month) {
        1 -> if (day < 20) "Козерог" else "Водолей"
        2 -> if (day < 19) "Водолей" else "Рыбы"
        3 -> if (day < 21) "Рыбы" else "Овен"
        4 -> if (day < 20) "Овен" else "Телец"
        5 -> if (day < 21) "Телец" else "Близнецы"
        6 -> if (day < 21) "Близнецы" else "Рак"
        7 -> if (day < 23) "Рак" else "Лев"
        8 -> if (day < 23) "Лев" else "Дева"
        9 -> if (day < 23) "Дева" else "Весы"
        10 -> if (day < 23) "Весы" else "Скорпион"
        11 -> if (day < 22) "Скорпион" else "Стрелец"
        12 -> if (day < 22) "Стрелец" else "Козерог"
        else -> "Неизвестно"
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    MaterialTheme {
        RegistrationScreen()
    }
}