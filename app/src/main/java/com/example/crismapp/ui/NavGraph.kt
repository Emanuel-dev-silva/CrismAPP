package com.example.crismapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crismapp.ui.UserSelectionScreen
import com.example.crismapp.ui.CatequistaLoginScreen
import com.example.crismapp.ui.CrismandoScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = "userSelection") {
    NavHost(navController = navController, startDestination = startDestination) {

        // Tela de Seleção Inicial
        composable("userSelection") {
            UserSelectionScreen(
                onCrismandoSelected = {
                    // Agora navegando para a nova tela de Crismando
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

        // Nova Tela do Crismando (Frequência, Avisos, Voltar)
        composable("crismandoScreen") {
            CrismandoScreen(navController = navController)
        }
    }
}