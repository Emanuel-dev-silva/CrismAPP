package com.example.crismapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.crismapp.ui.NavGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Instala a Splash Screen oficial
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Estado para controlar quando mostrar o NavGraph
        var showMainContent by mutableStateOf(false)

        // 2. Mantém a Splash por 2 segundos
        var keepSplashScreen = true
        lifecycleScope.launch {
            delay(2000)
            keepSplashScreen = false
            showMainContent = true
        }

        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!showMainContent) {
                        // Enquanto a Splash está carregando, desenhamos o fundo vermelho
                        // com o texto no canto inferior para garantir a transição
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFF0000)) // Vermelho Matriz
                        ) {
                            Text(
                                text = "Paróquia Santo Antônio - Tiúma",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 48.dp, end = 24.dp)
                            )
                        }
                    } else {
                        // Quando der os 2 segundos, carrega o App normalmente
                        val navController = rememberNavController()
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}