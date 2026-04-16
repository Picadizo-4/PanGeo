package com.example.pangeo.ui.game.capitals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel
import com.example.pangeo.viewmodel.CapitalsViewModel

/**
 * Pantalla principal del juego de capitales de Europa.
 * * Orquesta tres estados principales:
 * 1. Introducción: Selección de modo (Botones vs Escritura).
 * 2. Gameplay: Lógica de preguntas, cronómetro y feedback visual.
 * 3. Resultados: Cálculo de XP, bonificaciones por "Pleno" y guardado en Firebase.
 *
 * @param viewModel Lógica de juego y estados de las preguntas.
 * @param authViewModel Gestión del perfil del jugador (XP y Récords).
 * @param onNavigateBack Navegación hacia el menú principal.
 */
@Composable
fun CapitalEuropeGameScreen(
    viewModel: CapitalsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    // Tipografía manuscrita para elementos decorativos y títulos
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // Estados locales para la navegación interna del juego
    var showIntro by remember { mutableStateOf(true) }
    var gameMode by remember { mutableStateOf("botones") }

    // Desestructuración de estados del ViewModel para facilitar la lectura
    val currentQuestion = viewModel.currentQuestion
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val selectedAnswer by viewModel.selectedAnswer
    val textFeedback by viewModel.textFeedback
    val totalQuestions by viewModel.totalQuestions
    val currentIndex by viewModel.currentQuestionIndex
    val elapsedTime by viewModel.elapsedTime

    // Formateo de tiempo de expedición (MM:SS)
    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val maxScore = totalQuestions * 10
    var isNewBestTime by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    /**
     * Gestión del Final de Partida:
     * Cuando el juego termina, se sincronizan los puntos de experiencia (XP)
     * y se persiste el récord en la base de datos de Firebase.
     */
    LaunchedEffect(isGameOver) {
        if (isGameOver && !showIntro) {
            var totalXP = score
            // Bonificación por perfección: +500 XP si no hubo fallos
            if (score == maxScore && maxScore > 0) totalXP += 500
            authViewModel.addXP(totalXP)

            val category = if (gameMode == "botones") "europa_capitales_botones" else "europa_capitales_texto"
            authViewModel.saveGameRecord(category, score, elapsedTime) { newTime ->
                isNewBestTime = newTime
            }
        }
    }

    /**
     * Limpieza de recursos (Cleanup):
     * Asegura que el juego y el cronómetro se detengan si el usuario sale de la pantalla
     * de forma inesperada, evitando fugas de memoria (memory leaks).
     */
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
            viewModel.stopTimer()
        }
    }

    // Reset del campo de entrada al avanzar de pregunta en modo escritura
    LaunchedEffect(currentIndex) { inputText = "" }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        if (showIntro) {
            // --- ESTADO 1: PANTALLA DE INICIO (Modos de Juego) ---
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
                Spacer(modifier = Modifier.weight(0.2f))
                Image(
                    painter = painterResource(id = R.drawable.mapaeuropa),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp).padding(bottom = 20.dp),
                    contentScale = ContentScale.Fit
                )
                Text("Capitales: Europa", fontSize = 42.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Elige tu nivel de expedición:", fontSize = 18.sp, fontFamily = FontFamily.SansSerif, color = Color.Gray)

                Spacer(modifier = Modifier.weight(0.3f))

                // Selector de modo Selección (Botones)
                Button(
                    onClick = { gameMode = "botones"; showIntro = false; viewModel.resetGame() },
                    modifier = Modifier.fillMaxWidth().height(65.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF885D00)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MODO SELECCIÓN", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Selector de modo Escritura (Hardcore)
                Button(
                    onClick = { gameMode = "texto"; showIntro = false; viewModel.resetGame() },
                    modifier = Modifier.fillMaxWidth().height(65.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCB900)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MODO ESCRITURA", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        } else if (isGameOver) {
            // --- ESTADO 2: PANTALLA DE RESULTADOS ---
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("¡Expedición Terminada!", fontSize = 38.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
                Text("Has acertado ${score/10} de $totalQuestions", fontSize = 20.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFF4A60B2))

                Spacer(modifier = Modifier.height(30.dp))

                // Visualización de recompensa según desempeño
                if (score == maxScore && maxScore > 0) {
                    Text("¡PLENO TOTAL!", fontSize = 32.sp, fontFamily = caveatFamily, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Text("Total: ${score + 500} XP", fontSize = 42.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                } else {
                    Text("$score XP", fontSize = 48.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Surface(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tiempo: $formattedTime", fontSize = 22.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        if (isNewBestTime) Text("¡Nuevo récord!", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { isNewBestTime = false; viewModel.resetGame() },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VOLVER A JUGAR", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { isNewBestTime = false; viewModel.stopTimer(); onNavigateBack() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Salir al menú", color = Color.Gray, fontFamily = FontFamily.SansSerif)
                }
            }
        } else {
            // --- ESTADO 3: PANTALLA DE JUEGO ACTIVA ---
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // HUD (Heads-Up Display): Cronómetro y Puntuación
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    Text(formattedTime, fontSize = 24.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("$score XP", fontSize = 22.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                }

                Text("País ${currentIndex + 1} / $totalQuestions", fontSize = 16.sp, fontFamily = FontFamily.SansSerif, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.weight(0.1f))

                currentQuestion?.let { question ->
                    // Tarjeta de Desafío: Reconocimiento Visual de Bandera y Nombre
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = question.flagRes),
                                contentDescription = "Bandera Desafío",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(0.8f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = question.countryName,
                                fontSize = 34.sp,
                                fontFamily = caveatFamily,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.2f))

                    // Lógica de interacción diferenciada por Modo de Juego
                    if (gameMode == "botones") {
                        // Modo Selección: Grid vertical de opciones
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            question.options.forEach { option ->
                                val isCorrect = option == question.correctAnswer
                                val isChosen = option == selectedAnswer

                                // Feedback visual cromático de la respuesta
                                val bgColor = when {
                                    selectedAnswer == null -> Color.White
                                    isCorrect -> Color(0xFFC8E6C9)
                                    isChosen && !isCorrect -> Color(0xFFFFCDD2)
                                    else -> Color.White
                                }
                                Button(
                                    onClick = { viewModel.checkAnswerSelectionMode(option) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text(option, color = Color.Black, fontSize = 18.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    } else {
                        // Modo Escritura: Campo de texto con validación automática
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                if (textFeedback == null) {
                                    inputText = it
                                    viewModel.checkAutoAnswerTextMode(it)
                                }
                            },
                            label = { Text("Escribe la capital...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
                        )

                        // Feedback de texto post-respuesta
                        if (textFeedback != null) {
                            Text(
                                text = if (textFeedback == true) "¡Correcto!" else "La respuesta era: ${question.correctAnswer}",
                                color = if (textFeedback == true) Color(0xFF4CAF50) else Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { if (textFeedback == null) viewModel.checkAnswerTextMode("---") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                        ) {
                            Text("SALTAR PREGUNTA", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}