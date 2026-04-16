package com.example.pangeo.ui.games.flags

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.pangeo.viewmodel.FlagsViewModel

/**
 * Pantalla principal del juego de Banderas de Europa.
 * * Gestiona la lógica de expedición visual permitiendo dos niveles de dificultad:
 * 1. Selección: Reconocimiento entre 4 opciones dadas.
 * 2. Escritura: Recuerdo activo (Active Recall) escribiendo el nombre del país.
 * * Implementa persistencia de récords de tiempo y puntos de experiencia (XP) mediante Firebase.
 *
 * @param viewModel Lógica de negocio específica para el flujo de banderas y cronómetro.
 * @param authViewModel Gestión de la cuenta del usuario para guardar XP y récords.
 * @param onNavigateBack Callback para el retorno seguro al menú de selección de juegos.
 */
@Composable
fun FlagEuropeGameScreen(
    viewModel: FlagsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // Estados locales para la orquestación del flujo de partida
    var showIntro by remember { mutableStateOf(true) }
    var gameMode by remember { mutableStateOf("botones") }

    // Observación de estados reactivos desde el ViewModel
    val currentQuestion = viewModel.currentQuestion
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val selectedAnswer by viewModel.selectedAnswer
    val textFeedback by viewModel.textFeedback
    val totalQuestions by viewModel.totalQuestions
    val currentIndex by viewModel.currentQuestionIndex

    // Gestión y formateo del cronómetro de expedición
    val elapsedTime by viewModel.elapsedTime
    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val maxScore = totalQuestions * 10
    var isNewBestTime by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    /**
     * Sincronización de resultados al finalizar el juego.
     * Se calcula el bono de "Pleno" y se delega al repositorio de Auth el guardado de récords.
     */
    LaunchedEffect(isGameOver) {
        if (isGameOver && !showIntro) {
            var totalXP = score
            if (score == maxScore && maxScore > 0) {
                totalXP += 500 // Bonificación por precisión perfecta
            }
            authViewModel.addXP(totalXP)

            val category = if (gameMode == "botones") "europa_banderas_botones" else "europa_banderas_texto"
            authViewModel.saveGameRecord(category, score, elapsedTime) { newTime ->
                isNewBestTime = newTime
            }
        }
    }

    /**
     * Cleanup de la sesión de juego.
     * Asegura que el cronómetro se detenga y el estado se reinicie al abandonar la pantalla.
     */
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
            viewModel.stopTimer()
        }
    }

    LaunchedEffect(currentIndex) { inputText = "" }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        if (showIntro) {
            // --- ESTADO 1: INTRODUCCIÓN Y SELECCIÓN DE DIFICULTAD ---
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    IconButton(onClick = { viewModel.stopTimer(); onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }

                Spacer(modifier = Modifier.weight(0.2f))

                Image(
                    painter = painterResource(id = R.drawable.mapaeuropa),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp).padding(bottom = 20.dp),
                    contentScale = ContentScale.Fit
                )

                Text("Expedición: Europa", fontSize = 42.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Elige tu nivel de expedición:", fontSize = 18.sp, fontFamily = FontFamily.SansSerif, color = Color.Gray)

                Spacer(modifier = Modifier.weight(0.3f))

                // Botón Modo Selección: Color Granate PanGeo
                Button(
                    onClick = { gameMode = "botones"; showIntro = false; viewModel.resetGame() },
                    modifier = Modifier.fillMaxWidth().height(65.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A000D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MODO SELECCIÓN", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Modo Escritura: Color Rojo Alerta
                Button(
                    onClick = { gameMode = "texto"; showIntro = false; viewModel.resetGame() },
                    modifier = Modifier.fillMaxWidth().height(65.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE10017)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("MODO ESCRITURA", color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

        } else if (isGameOver) {
            // --- ESTADO 2: RESUMEN DE EXPEDICIÓN ---
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Expedición Terminada!", fontSize = 40.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                Text(
                    text = "Has acertado ${score / 10} de $totalQuestions países",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF4A60B2),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (score == maxScore && maxScore > 0) {
                    Text("¡PLENO TOTAL!", fontSize = 32.sp, fontFamily = caveatFamily, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text("Puntos base: $score XP", fontSize = 24.sp, fontFamily = FontFamily.SansSerif, color = Color.DarkGray)
                    Text("+ 500 XP (Bonus)", fontSize = 24.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFFF39C12), fontWeight = FontWeight.Black)
                    HorizontalDivider(modifier = Modifier.width(150.dp).padding(vertical = 10.dp), color = Color.LightGray)
                    Text("Total: ${score + 500} XP", fontSize = 38.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                } else {
                    Text("Puntuación:", fontSize = 18.sp, fontFamily = FontFamily.SansSerif, color = Color.Gray)
                    Text("$score XP", fontSize = 48.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(25.dp))

                // Indicador de Récord de Tiempo
                Surface(
                    color = if (isNewBestTime) Color(0xFFFFF9C4) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 25.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text(text = "⏱️ Tiempo: $formattedTime", fontSize = 22.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        if (isNewBestTime) {
                            Text("¡Nuevo récord!", fontSize = 16.sp, fontFamily = FontFamily.SansSerif, color = Color(0xFFE65100), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Button(
                    onClick = { isNewBestTime = false; viewModel.resetGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(55.dp)
                ) {
                    Text("VOLVER A JUGAR", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { isNewBestTime = false; viewModel.stopTimer(); onNavigateBack() }) {
                    Text("Salir al menú", color = Color.Gray, fontFamily = FontFamily.SansSerif)
                }
            }
        } else {
            // --- ESTADO 3: GAMEPLAY ACTIVO ---
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de Estado Superior (HUD)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.stopTimer(); onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                    Text(text = formattedTime, fontSize = 26.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("XP: $score", fontSize = 22.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                }

                Text(
                    text = "Bandera ${currentIndex + 1} de $totalQuestions",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(0.1f))

                currentQuestion?.let { question ->
                    // Tarjeta de la Bandera: Se utiliza ContentScale.Crop para una estética más profesional
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.6f).shadow(8.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = question.flagImageRes),
                            contentDescription = "Bandera a identificar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.2f))

                    // Lógica de UI condicional según el Modo de Juego
                    if (gameMode == "botones") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            question.options.forEach { option ->
                                val isCorrect = option == question.correctAnswer
                                val isChosen = option == selectedAnswer

                                // Definición dinámica de colores para feedback inmediato
                                val bgColor = when {
                                    selectedAnswer == null -> Color.White
                                    isCorrect -> Color(0xFFC8E6C9)
                                    isChosen && !isCorrect -> Color(0xFFFFCDD2)
                                    else -> Color.White
                                }
                                val bColor = when {
                                    selectedAnswer == null -> Color.LightGray
                                    isCorrect -> Color(0xFF4CAF50)
                                    isChosen && !isCorrect -> Color(0xFFF44336)
                                    else -> Color.LightGray
                                }

                                AnswerButton(
                                    text = option,
                                    backgroundColor = bgColor,
                                    borderColor = bColor,
                                    onClick = { viewModel.checkAnswerSelectionMode(option) }
                                )
                            }
                        }
                    } else {
                        // Modo Escritura con validación automática
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                if (textFeedback == null) {
                                    inputText = it
                                    viewModel.checkAutoAnswerTextMode(it)
                                }
                            },
                            label = { Text("Escribe el país...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
                        )

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
                            Text("SALTAR PREGUNTA", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}

/**
 * Componente reutilizable para los botones de respuesta.
 * * @param text Texto de la opción de respuesta.
 * @param backgroundColor Color de fondo dinámico según acierto/fallo.
 * @param borderColor Color de borde dinámico.
 * @param onClick Acción de validación.
 */
@Composable
fun AnswerButton(
    text: String,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp).shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(text = text, color = Color.Black, fontSize = 19.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
    }
}