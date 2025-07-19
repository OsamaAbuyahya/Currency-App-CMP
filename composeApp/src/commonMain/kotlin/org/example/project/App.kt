package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.example.project.presentation.screen.HomeScreen

@Composable
@Preview
fun App() {

    MaterialTheme {
        Navigator(HomeScreen())
    }
}