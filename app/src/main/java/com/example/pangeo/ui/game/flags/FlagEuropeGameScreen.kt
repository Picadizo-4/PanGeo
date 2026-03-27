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
import androidx.compose.runtime.DisposableEffect

@Composable
fun FlagEuropeGameScreen(
    viewModel: FlagsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    var showIntro by remember { mutableStateOf(true) }

    val currentQuestion = viewModel.currentQuestion
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val selectedAnswer by viewModel.selectedAnswer

    // Contadores
    val totalQuestions by viewModel.totalQuestions
    val currentIndex by viewModel.currentQuestionIndex

    val elapsedTime by viewModel.elapsedTime
    val minutes = elapsedTime / 60
    val seconds = elapsedTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // El cálculo del pleno ahora es dinámico (ej: 44 banderas = 440 puntos máximo)
    val maxScore = totalQuestions * 10
    var isNewBestTime by remember { mutableStateOf(false) }

    LaunchedEffect(isGameOver) {
        if (isGameOver && !showIntro) {
            var totalXP = score
            if (score == maxScore && maxScore > 0) {
                totalXP += 500 // Bonus de Pleno
            }
            authViewModel.addXP(totalXP)

            authViewModel.saveGameRecord("europa_banderas", score, elapsedTime) { newTime ->
                isNewBestTime = newTime
            }
        }
    }

    // 2. NUEVO: LIMPIEZA AUTOMÁTICA
    // Cuando el usuario sale de esta pantalla (al menú o cerrando sesión),
    // reseteamos el juego para que no se queden los datos "atascados" para el siguiente.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetGame()
            viewModel.stopTimer()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        if (showIntro) {
            // ==========================================
            // 1. PANTALLA DE SALA DE ESPERA (INTRO)
            // ==========================================
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    IconButton(onClick = {
                        viewModel.stopTimer()
                        onNavigateBack()
                    }) {
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

                Text("Expedición: Europa", fontSize = 42.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.Black, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Adivina las 44 banderas lo más rápido posible. \n\nEl cronómetro empezará en cuanto pulses el botón.",
                    fontSize = 24.sp,
                    fontFamily = caveatFamily,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.weight(0.3f))

                Button(
                    onClick = {
                        showIntro = false
                        viewModel.resetGame()
                    },
                    modifier = Modifier.fillMaxWidth().height(65.dp).shadow(6.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("COMENZAR", color = Color.White, fontSize = 28.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

        } else if (isGameOver) {
            // ==========================================
            // 2. PANTALLA DE RESULTADOS FINALES
            // ==========================================
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¡Expedición Terminada!", fontSize = 40.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                // NUEVO: Contador de aciertos reales (score / 10 porque cada acierto vale 10)
                val correctAnswers = score / 10
                Text(
                    text = "Has acertado $correctAnswers de $totalQuestions países",
                    fontSize = 24.sp,
                    fontFamily = caveatFamily,
                    color = Color(0xFF4A60B2), // Un azul suave para que destaque
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (score == maxScore && maxScore > 0) {
                    Text("¡Enhorabuena, has hecho pleno!", fontSize = 28.sp, fontFamily = caveatFamily, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(15.dp))
                    Text("Puntos base: $score XP", fontSize = 28.sp, fontFamily = caveatFamily, color = Color.DarkGray)
                    Text("+ 500 XP (Bonus de Pleno)", fontSize = 26.sp, fontFamily = caveatFamily, color = Color(0xFFF39C12), fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.width(150.dp).padding(vertical = 10.dp), color = Color.LightGray)
                    Text("Total: ${score + 500} XP", fontSize = 38.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                } else {
                    Text("Puntuación obtenida:", fontSize = 22.sp, fontFamily = caveatFamily, color = Color.Gray)
                    Text("$score XP", fontSize = 45.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(25.dp))

                Surface(
                    color = if (isNewBestTime) Color(0xFFFFF9C4) else Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 25.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(text = "⏱️ Tiempo: $formattedTime", fontSize = 26.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold, color = if (isNewBestTime) Color(0xFFF57F17) else Color(0xFF1976D2))
                        if (isNewBestTime) {
                            Text("¡Nuevo mejor tiempo!", fontSize = 18.sp, fontFamily = caveatFamily, color = Color(0xFFE65100), fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Button(
                    onClick = {
                        isNewBestTime = false
                        viewModel.resetGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(55.dp)
                ) {
                    Text("Volver a jugar", color = Color.White, fontFamily = caveatFamily, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = {
                        isNewBestTime = false
                        viewModel.stopTimer()
                        onNavigateBack()
                    }
                ) {
                    Text("Salir al menú", color = Color.Gray, fontFamily = caveatFamily, fontSize = 22.sp)
                }
            }
        } else {
            // ==========================================
            // 3. PANTALLA DE JUEGO ACTIVA
            // ==========================================
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        viewModel.stopTimer()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }

                    Text(text = formattedTime, fontSize = 28.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                    Text("Puntos: $score", fontSize = 26.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
                }

                // NUEVO: Contador de banderas (ej: 5 / 44)
                Text(
                    text = "Bandera ${currentIndex + 1} de $totalQuestions",
                    fontSize = 20.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.weight(0.3f))

                currentQuestion?.let { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = question.flagImageRes),
                            contentDescription = "Bandera a adivinar",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.5f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        question.options.forEach { option ->
                            val isCorrect = option == question.correctAnswer
                            val isChosen = option == selectedAnswer

                            val bgColor = when {
                                selectedAnswer == null -> Color.White
                                isCorrect -> Color(0xFFC8E6C9)
                                isChosen && !isCorrect -> Color(0xFFFFCDD2)
                                else -> Color.White
                            }

                            val bColor = when {
                                selectedAnswer == null -> Color.Black
                                isCorrect -> Color(0xFF4CAF50)
                                isChosen && !isCorrect -> Color(0xFFF44336)
                                else -> Color.LightGray
                            }

                            AnswerButton(
                                text = option,
                                fontFamily = caveatFamily,
                                backgroundColor = bgColor,
                                borderColor = bColor,
                                onClick = { viewModel.checkAnswer(option) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.3f))
            }
        }
    }
}


@Composable
fun AnswerButton(
    text: String,
    fontFamily: FontFamily,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 26.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
    }
}