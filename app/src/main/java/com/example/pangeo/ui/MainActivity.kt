package com.example.pangeo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pangeo.R
import com.example.pangeo.ui.auth.LoginScreen
import com.example.pangeo.ui.auth.RegisterScreen
import com.example.pangeo.ui.games.flags.FlagEuropeGameScreen
import com.example.pangeo.ui.games.flags.FlagsMenuScreen
import com.example.pangeo.ui.home.HomeScreen
import com.example.pangeo.ui.home.ProfileScreen
import com.example.pangeo.viewmodel.AuthState
import com.example.pangeo.viewmodel.AuthViewModel
import com.example.pangeo.viewmodel.FlagsViewModel
// --- IMPORTS DE LOTTIE ---
import com.airbnb.lottie.compose.*
import com.example.pangeo.ui.home.AchievementsScreen
import com.example.pangeo.ui.home.GlobalRankingScreen
import com.example.pangeo.ui.home.WorldMapAchievementsScreen
import com.example.pangeo.viewmodel.RankingViewModel
import com.example.pangeo.viewmodel.RecordsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val caveatFamily = remember {
                try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
            }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color.Black,
                    background = Color(0xFFFDFDFD),
                    surface = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val flagsViewModel: FlagsViewModel = viewModel()

                    val authState by authViewModel.authState.collectAsState()
                    val userData by authViewModel.userData
                    val recordsViewModel: RecordsViewModel = viewModel()
                    val rankingViewModel: RankingViewModel = viewModel()

                    val showSplash = authState is AuthState.Loading || (authState is AuthState.Success && userData == null)

                    if (showSplash) {
                        // ==========================================
                        // DISEÑO DE LA SPLASH SCREEN CON LOTTIE 3D
                        // ==========================================
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                // --- NUEVO: REPRODUCTOR LOTTIE ---
                                // 1. Cargamos la composición del archivo JSON en raw
                                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.globe_3d))

                                // 2. Configuramos el estado (para que se repita infinitamente)
                                val progress by animateLottieCompositionAsState(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever
                                )

                                // 3. Pintamos la animación en pantalla
                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier.size(250.dp) // Un poco más grande para que luzca
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "PANGEO",
                                    fontSize = 50.sp,
                                    fontFamily = caveatFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A60B2)
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                // Mantenemos el CircularProgressIndicator para que sepa que está cargando
                                CircularProgressIndicator(
                                    color = Color(0xFF4A60B2),
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    } else {
                        // ==========================================
                        // NAVEGACIÓN (SIN FLASH DE LOGIN)
                        // ==========================================
                        val startRoute = if (authState is AuthState.Success) "home" else "login"

                        NavHost(navController = navController, startDestination = startRoute) {
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onNavigateToRegister = { navController.navigate("register") },
                                    onLoginSuccess = {
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                    }
                                )
                            }
                            composable("register") {
                                RegisterScreen(viewModel = authViewModel, onNavigateBack = { navController.popBackStack() })
                            }
                            composable("home") {
                                HomeScreen(viewModel = authViewModel, onNavigate = { ruta -> navController.navigate(ruta) })
                            }
                            composable("perfil") {
                                ProfileScreen(
                                    viewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                                    }
                                )
                            }
                            composable("banderas_menu") {
                                FlagsMenuScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEurope = { navController.navigate("juego_europa") }
                                )
                            }
                            composable("juego_europa") {
                                FlagEuropeGameScreen(
                                    viewModel = flagsViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            // Búscalo en tu NavHost y déjalo así:
                            composable("achievements") {
                                AchievementsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToWorldMap = { navController.navigate("achievements_world_map") },
                                    onNavigateToRanking = { navController.navigate("global_ranking") } // <-- EL CABLE CONECTADO
                                )
                            }

                            composable("achievements_world_map") {
                                WorldMapAchievementsScreen(
                                    viewModel = recordsViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("global_ranking") {
                                GlobalRankingScreen(
                                    viewModel = rankingViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}