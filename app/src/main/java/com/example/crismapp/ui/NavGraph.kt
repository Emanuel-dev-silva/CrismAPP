package com.example.crismapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crismapp.ui.UserSelectionScreen
import com.example.crismapp.ui.CatequistaLoginScreen
import com.example.crismapp.ui.CrismandoScreen
import com.example.crismapp.ui.CatequistaOptionsScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = "userSelection") {
    NavHost(navController = navController, startDestination = startDestination) {

        // Tela de Seleção Inicial
        composable("userSelection") {
            UserSelectionScreen(
                onCrismandoSelected = {
                    navController.navigate("crismandoScreen")
                },
                onCatequistaSelected = {
                    navController.navigate("LoginCatequista")
                }
            )
        }

        // Tela de Login do Catequista
        composable("LoginCatequista") {
            CatequistaLoginScreen(navController = navController)
        }

        // Tela de Opções do Catequista (Turmas e Sair)
        composable("catequistaOptions") {
            CatequistaOptionsScreen(navController = navController)
        }

        // Tela do Crismando (Frequência, Avisos, Voltar)
        composable("crismandoScreen") {
            CrismandoScreen(navController = navController)
        }

        // --- NOVAS ROTAS DE GESTÃO DE TURMAS ---

        composable("turmaJovemScreen") {
            // Quando você criar o arquivo TurmaJovemScreen.kt, ele será chamado aqui
            // Por enquanto, você pode criar um Composable simples para teste
            TurmaJovemScreen(navController = navController)
        }

        composable("turmaAdultaScreen") {
            // Quando você criar o arquivo TurmaAdultaScreen.kt, ele será chamado aqui
            TurmaAdultaScreen(navController = navController)
        }
    }
}