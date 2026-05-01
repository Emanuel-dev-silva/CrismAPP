package com.example.crismapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crismapp.ui.UserSelectionScreen
import com.example.crismapp.ui.CatequistaLoginScreen
import com.example.crismapp.ui.CrismandoScreen
import com.example.crismapp.ui.CatequistaOptionsScreen
import com.example.crismapp.ui.CrismandoLoginScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = "userSelection") {
    NavHost(navController = navController, startDestination = startDestination) {

        // Tela de Seleção Inicial
        composable("userSelection") {
            UserSelectionScreen(
                onCrismandoSelected = {
                    // Agora navega para a tela de login do crismando primeiro
                    navController.navigate("crismandoLoginScreen")
                },
                onCatequistaSelected = {
                    navController.navigate("LoginCatequista")
                }
            )
        }

        // --- NOVA ROTA DE LOGIN DO CRISMANDO ---
        composable("crismandoLoginScreen") {
            CrismandoLoginScreen(navController = navController)
        }

        // Tela de Login do Catequista
        composable("LoginCatequista") {
            CatequistaLoginScreen(navController = navController)
        }

        // Tela de Opções do Catequista (Turmas e Sair)
        composable("catequistaOptions") {
            CatequistaOptionsScreen(navController = navController)
        }

        // Tela do Crismando (Frequência, Avisos, Voltar) - Acessada após login
        composable("crismandoScreen") {
            CrismandoScreen(navController = navController)
        }

        // --- ROTAS DE GESTÃO DE TURMAS ---

        composable("turmaJovemScreen") {
            TurmaJovemScreen(navController = navController)
        }

        composable("turmaAdultaScreen") {
            TurmaAdultaScreen(navController = navController)
        }
    }
}