package com.example.pangeo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pangeo.ui.auth.LoginScreen
import com.example.pangeo.ui.auth.ProfileScreen
import com.example.pangeo.ui.auth.RegisterScreen
import com.example.pangeo.ui.home.HomeScreen
import com.example.pangeo.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Definimos un esquema de colores básico y limpio para PanGeo
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color.Black,
                    background = Color(0xFFF2F2F2),
                    surface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // Compartimos el AuthViewModel entre todas las pantallas de login/registro
                    val authViewModel: AuthViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "login") {

                        // PANTALLA LOGIN
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginSuccess = { navController.navigate("home") }
                            )
                        }

                        // PANTALLA REGISTRO
                        composable("register") {
                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // PANTALLA HOME
                        composable("home") {
                            HomeScreen(
                                viewModel = authViewModel,
                                onNavigate = { ruta -> navController.navigate(ruta) }
                            )
                        }

                        // PANTALLA PERFIL (La que te daba el fallo del nombre)
                        composable("perfil") {
                            ProfileScreen(
                                viewModel = authViewModel, // <--- ESTO ES LO QUE CARGA A CAPITANPANGEO
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}