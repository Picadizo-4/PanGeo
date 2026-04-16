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
import com.airbnb.lottie.compose.*
import com.example.pangeo.ui.game.capitals.CapitalEuropeGameScreen
import com.example.pangeo.ui.game.capitals.CapitalsMenuScreen
import com.example.pangeo.ui.game.culture.CultureGameScreen
import com.example.pangeo.ui.game.maps.EuropeMapScreen
import com.example.pangeo.ui.game.maps.MapsMenuScreen
import com.example.pangeo.viewmodel.CultureViewModel
import com.example.pangeo.ui.home.AchievementsScreen
import com.example.pangeo.ui.home.GlobalRankingScreen
import com.example.pangeo.ui.home.WorldMapAchievementsScreen
import com.example.pangeo.ui.study.StudyCapitalsScreen
import com.example.pangeo.ui.study.StudyFlagsScreen
import com.example.pangeo.ui.study.StudyMapScreen
import com.example.pangeo.ui.study.StudyMenuScreen
import com.example.pangeo.viewmodel.RankingViewModel
import com.example.pangeo.viewmodel.RecordsViewModel
import com.example.pangeo.viewmodel.CapitalsViewModel
import com.example.pangeo.viewmodel.MapsViewModel
import com.example.pangeo.viewmodel.StudyCapitalsViewModel
import com.example.pangeo.viewmodel.StudyFlagsViewModel
import com.example.pangeo.viewmodel.StudyMapViewModel

/**
 * Punto de entrada principal de la aplicación PanGeo.
 * * Responsabilidades:
 * 1. Inicialización del motor de Compose y temas globales.
 * 2. Gestión del estado de autenticación raíz.
 * 3. Implementación del Splash Screen animado.
 * 4. Definición del Grafo de Navegación ([NavHost]).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Carga de tipografía decorativa para branding global
            val caveatFamily = remember {
                try {
                    FontFamily(Font(R.font.caveat))
                } catch (e: Exception) {
                    FontFamily.Serif
                }
            }

            // Aplicación del sistema de diseño (Material 3)
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
                    // Inicialización del controlador de navegación y proveedores de estado (ViewModels)
                    val navController = rememberNavController()

                    /**
                     * Instanciación de ViewModels:
                     * En una arquitectura profesional, esto se delegaría a un inyector de dependencias (Dagger/Hilt),
                     * pero aquí se gestiona mediante el scope de la Activity para mantener la simplicidad.
                     */
                    val authViewModel: AuthViewModel = viewModel()
                    val flagsViewModel: FlagsViewModel = viewModel()
                    val capitalsViewModel: CapitalsViewModel = viewModel()
                    val cultureViewModel: CultureViewModel = viewModel()
                    val recordsViewModel: RecordsViewModel = viewModel()
                    val rankingViewModel: RankingViewModel = viewModel()

                    val authState by authViewModel.authState.collectAsState()
                    val userData by authViewModel.userData

                    /**
                     * Lógica de Splash Screen:
                     * Determina si la aplicación debe mostrar la animación de carga basándose
                     * en la resolución de la sesión de Firebase y los datos de perfil.
                     */
                    val showSplash = authState is AuthState.Loading || (authState is AuthState.Success && userData == null)

                    if (showSplash) {
                        // --- VISTA DE CARGA (SPLASH) ---
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.globe_3d))
                                val progress by animateLottieCompositionAsState(
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever
                                )

                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier.size(220.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "PANGEO",
                                    fontSize = 54.sp,
                                    fontFamily = caveatFamily,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4A60B2)
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = "Preparando expedición...",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color.Gray
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                CircularProgressIndicator(
                                    color = Color(0xFF4A60B2),
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    } else {
                        /**
                         * GRAFO DE NAVEGACIÓN PRINCIPAL:
                         * Define todas las pantallas y sus transiciones.
                         * La ruta inicial se decide dinámicamente según el [authState].
                         */
                        val startRoute = if (authState is AuthState.Success) "home" else "login"

                        NavHost(navController = navController, startDestination = startRoute) {

                            // --- FLUJO DE AUTENTICACIÓN ---
                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onNavigateToRegister = { navController.navigate("register") },
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("register") {
                                RegisterScreen(
                                    viewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() })
                            }

                            // --- FLUJO HOME Y PERFIL ---
                            composable("home") {
                                HomeScreen(
                                    viewModel = authViewModel,
                                    onNavigate = { ruta -> navController.navigate(ruta) })
                            }
                            composable("perfil") {
                                ProfileScreen(
                                    viewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onLogout = {
                                        authViewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // --- MÓDULOS DE JUEGO: BANDERAS ---
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

                            // --- MÓDULOS DE JUEGO: CAPITALES ---
                            composable("capitals_menu") {
                                CapitalsMenuScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEurope = { navController.navigate("juego_capitales_europa") }
                                )
                            }
                            composable("juego_capitales_europa") {
                                CapitalEuropeGameScreen(
                                    viewModel = capitalsViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- MÓDULOS DE JUEGO: CULTURA ---
                            composable("cultura") {
                                CultureGameScreen(
                                    viewModel = cultureViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- MÓDULOS DE JUEGO: MAPAS ---
                            composable("maps_menu") {
                                MapsMenuScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEurope = { navController.navigate("juego_mapas_europa") }
                                )
                            }
                            composable("juego_mapas_europa") {
                                val mapsViewModel: MapsViewModel = viewModel()
                                EuropeMapScreen(
                                    viewModel = mapsViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            // --- SOCIAL Y RÉCORDS ---
                            composable("achievements") {
                                AchievementsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToPersonalRecords = { navController.navigate("achievements_world_map") },
                                    onNavigateToRanking = { navController.navigate("global_ranking") }
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

                            // --- BIBLIOTECA DE ESTUDIO (MÓDULO EDUCATIVO) ---
                            composable("estudio") {
                                StudyMenuScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToStudyMap = { navController.navigate("estudio_mapas") },
                                    onNavigateToStudyFlags = { navController.navigate("estudio_banderas") },
                                    onNavigateToStudyCapitals = { navController.navigate("estudio_capitales") }
                                )
                            }
                            composable("estudio_mapas") {
                                val studyMapViewModel: StudyMapViewModel = viewModel()
                                StudyMapScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = studyMapViewModel
                                )
                            }
                            composable("estudio_banderas") {
                                val studyFlagsViewModel: StudyFlagsViewModel = viewModel()
                                StudyFlagsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = studyFlagsViewModel
                                )
                            }
                            composable("estudio_capitales") {
                                val studyCapitalsViewModel: StudyCapitalsViewModel = viewModel()
                                StudyCapitalsScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = studyCapitalsViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}