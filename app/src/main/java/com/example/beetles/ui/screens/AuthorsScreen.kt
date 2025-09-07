package com.example.beetles.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.beetles.R

data class Author(val name: String, val photoResId: Int)

@Composable
fun AuthorsScreen() {
    val authors = listOf(
        Author("Мартынов Виталий", R.drawable.author1),
        Author("Вараксин Максим", R.drawable.author2),
        Author("Парков Даниил", R.drawable.author3)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Авторы проекта",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(authors) { author ->
                AuthorItem(author = author)
            }
        }
    }
}

@Composable
fun AuthorItem(author: Author) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = author.photoResId),
                contentDescription = "Фото ${author.name}",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp)
            )

            Text(
                text = author.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}