package com.example.crismapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.crismapp.ui.CrismAppPadraoVisual
import com.example.crismapp.ui.NavGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var showMainContent by mutableStateOf(false)
        var keepSplashScreen = true

        lifecycleScope.launch {
            delay(2_000L)

            keepSplashScreen = false
            showMainContent = true
        }

        splashScreen.setKeepOnScreenCondition {
            keepSplashScreen
        }

        setContent {
            /*
             * CrismAppPadraoVisual envolve todo o aplicativo.
             *
             * Ele fixa:
             * - família da fonte;
             * - tamanho dos estilos do Material 3;
             * - altura das linhas;
             * - espaçamento entre letras;
             * - escala de fonte em 1f.
             */
            CrismAppPadraoVisual {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!showMainContent) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFF0000))
                        ) {
                            Text(
                                text = "Paróquia Santo Antônio - Tiúma",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        bottom = 48.dp,
                                        end = 24.dp
                                    )
                            )
                        }
                    } else {
                        val navController =
                            rememberNavController()

                        NavGraph(
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}