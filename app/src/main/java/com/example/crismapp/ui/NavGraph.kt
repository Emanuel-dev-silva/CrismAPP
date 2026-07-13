package com.example.crismapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "userSelection"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // =====================================================
        // TELA INICIAL
        // =====================================================

        composable(route = "userSelection") {
            UserSelectionScreen(
                onCrismandoSelected = {
                    navController.navigate("crismandoLoginScreen")
                },
                onCatequistaSelected = {
                    navController.navigate("LoginCatequista")
                }
            )
        }

        // =====================================================
        // LOGIN DO CRISMANDO
        // =====================================================

        composable(route = "crismandoLoginScreen") {
            CrismandoLoginScreen(
                navController = navController
            )
        }

        // =====================================================
        // LOGIN E OPÇÕES DO CATEQUISTA
        // =====================================================

        composable(route = "LoginCatequista") {
            CatequistaLoginScreen(
                navController = navController
            )
        }

        composable(route = "catequistaOptions") {
            CatequistaOptionsScreen(
                navController = navController
            )
        }

        // =====================================================
        // ÁREA DO CRISMANDO
        //
        // Agora esta rota aceita uma matrícula:
        //
        // crismandoScreen?matricula=CX-1234
        //
        // A matrícula é opcional temporariamente para não
        // quebrar o código antigo enquanto fazemos a migração.
        // =====================================================

        composable(
            route = "crismandoScreen?matricula={matricula}",
            arguments = listOf(
                navArgument(name = "matricula") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = false
                }
            )
        ) {
            CrismandoScreen(
                navController = navController
            )
        }

        // =====================================================
        // GESTÃO DAS TURMAS
        // =====================================================

        composable(route = "turmaJovemScreen") {
            TurmaJovemScreen(
                navController = navController
            )
        }

        composable(route = "turmaAdultaScreen") {
            TurmaAdultaScreen(
                navController = navController
            )
        }
    }
}