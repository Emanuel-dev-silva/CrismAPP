package com.example.crismapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.crismapp.ui.NavGraph // Certifique-se de que o NavGraph está nesse pacote

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Use o seu tema personalizado se tiver um, ou MaterialTheme
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Criamos o controlador de navegação
                    val navController = rememberNavController()

                    // 2. Chamamos o NavGraph, que gerencia todas as telas
                    NavGraph(navController = navController)
                }
            }
        }
    }
}