package com.sergeyfierce.dailyplanner.feature.notes

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

@Composable
fun NotesScreen() {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Заметки") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text(text = "В разработке", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
