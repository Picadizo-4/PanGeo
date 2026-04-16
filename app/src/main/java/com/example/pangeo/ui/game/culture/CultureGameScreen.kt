package com.example.pangeo.ui.game.culture

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel
import com.example.pangeo.viewmodel.CultureViewModel

/**
 * Pantalla del modo de juego "Supervivencia Cultural".
 * * Implementa una mecánica de 'muerte súbita' donde el usuario debe mantener una racha
 * de aciertos. Incluye elementos de gamificación avanzada como:
 * 1. Fondos dinámicos que reaccionan a la racha del usuario.
 * 2. Sistema de comodines (50/50) con lógica de recarga por hitos.
 * 3. Persistencia de récords personales en Firebase.
 * * @param viewModel Lógica de preguntas y gestión de rachas.
 * @param authViewModel Gestión de experiencia (XP) y récords globales del perfil.
 * @param onNavigateBack Control de navegación hacia el menú principal.
 */
@Composable
fun CultureGameScreen(
    viewModel: CultureViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val context = LocalContext.current

    var showIntro by remember { mutableStateOf(true) }

    // Suscripción a estados reactivos del ViewModel
    val streak by viewModel.streak
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val isVictory by viewModel.isVictory
    val question = viewModel.currentQuestion.value
    val options = viewModel.currentOptions.value
    val selectedAnswer by viewModel.selectedAnswer
    val hasUsed5050 by viewModel.hasUsedFiftyFifty

    val showRewardNotify by viewModel.showWildcardReward
    val bestStreak by viewModel.bestStreak
    val isNewRecord by viewModel.isNewRecord

    /**
     * Lógica de Feedback Visual:
     * El color de fondo evoluciona proporcionalmente a la racha,
     * proporcionando una narrativa visual de progresión y dificultad.
     */
    val targetColor = CultureRepository.getBackgroundColorForStreak(streak)
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (showIntro || isGameOver) Color(0xFFFDFDFD) else targetColor,
        animationSpec = tween(durationMillis = 800)
    )

    // Ajuste de contraste: El texto pasa a blanco cuando el fondo es muy oscuro (rachas altas)
    val textColor = if (streak >= 14 && !isGameOver && !showIntro) Color.White else Color.Black

    /**
     * Side Effect de Finalización:
     * Al detectar el fin del juego, se sincronizan los resultados con Firebase.
     * Solo se ejecuta una vez gracias a la restricción del bloque 'LaunchedEffect'.
     */
    LaunchedEffect(isGameOver) {
        if (isGameOver && !showIntro) {
            authViewModel.addXP(score)
            if (isNewRecord) {
                authViewModel.saveCultureStreakRecord(bestStreak)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(animatedBackgroundColor)) {
        if (showIntro) {
            IntroLayout(onNavigateBack, caveatFamily, bestStreak, context, viewModel) { showIntro = false }
        } else if (isGameOver) {
            GameOverLayout(caveatFamily, isNewRecord, bestStreak, streak, score, context, viewModel, isVictory, onNavigateBack)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                GameHeader(viewModel, streak, bestStreak, textColor)

                Spacer(modifier = Modifier.weight(0.1f))

                question?.let { q ->
                    QuestionCard(q)

                    Spacer(modifier = Modifier.weight(0.2f))

                    OptionsLayout(options, q, selectedAnswer, viewModel)

                    Spacer(modifier = Modifier.height(24.dp))

                    WildcardButton(hasUsed5050, selectedAnswer, viewModel)
                }
                Spacer(modifier = Modifier.weight(0.1f))
            }

            /**
             * Notificación de Recompensa:
             * Se activa mediante [AnimatedVisibility] cuando el usuario alcanza un hito
             * de racha (ej. cada 100 aciertos) para restaurar el comodín.
             */
            AnimatedVisibility(
                visible = showRewardNotify,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
            ) {
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¡Hito alcanzado! Comodín restaurado",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layout de introducción: Explica las reglas de supervivencia y muestra el récord personal.
 */
@Composable
fun IntroLayout(onBack: () -> Unit, font: FontFamily, best: Int, ctx: android.content.Context, vm: CultureViewModel, onStart: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
        }
        Spacer(modifier = Modifier.weight(0.2f))
        Text("Supervivencia Cultural", fontSize = 42.sp, fontFamily = font, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)

        if (best > 0) {
            Surface(color = Color(0xFFFFF9C4), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
                Text("🏆 Mejor Racha: $best", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "REGLAS DE SUPERVIVENCIA:", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Muerte súbita: Un solo fallo y estás fuera.\n" +
                            "• Comodín 50/50: Elimina dos opciones incorrectas.\n" +
                            "• Recarga: Recuperarás el uso del comodín cada 100 aciertos.\n"+
                            "• Evolución: A partir de la racha 500, el comodín eliminará TODAS las opciones incorrectas.",
                    fontSize = 15.sp, lineHeight = 20.sp, color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        Button(
            onClick = { onStart(); vm.startGame(ctx) },
            modifier = Modifier.fillMaxWidth().height(65.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("COMENZAR", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

/**
 * Layout de finalización: Gestiona tanto la derrota como la victoria absoluta.
 */
@Composable
fun GameOverLayout(
    font: FontFamily,
    isNew: Boolean,
    best: Int,
    streak: Int,
    score: Int,
    ctx: android.content.Context,
    vm: CultureViewModel,
    isVictory: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isVictory) "¡Leyenda Mundial!" else "Expedición Fallida",
            fontSize = 42.sp,
            fontFamily = font,
            color = if (isVictory) Color(0xFFFFA000) else Color.Red,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (isVictory) {
            Text("Has completado todos los desafíos.", fontSize = 18.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
        } else if (isNew) {
            Text("¡NUEVO RÉCORD!", fontSize = 22.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        } else {
            Text("Récord a batir: $best", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(30.dp))
        Text("Racha Final: $streak", fontSize = 22.sp)
        Text("$score XP", fontSize = 54.sp, fontWeight = FontWeight.Black)

        Spacer(modifier = Modifier.height(40.dp))

        if (!isVictory) {
            Button(onClick = { vm.startGame(ctx) }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                Text("INTENTAR DE NUEVO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = onBack) {
            Text(if (isVictory) "Volver con honor" else "Volver al menú", color = Color.Gray)
        }
    }
}

/**
 * Cabecera de juego: Muestra la racha actual y el récord, con feedback visual de "fuego" en rachas altas.
 */
@Composable
fun GameHeader(vm: CultureViewModel, streak: Int, best: Int, textColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { vm.isGameOver.value = true }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = textColor) }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (streak >= 5) Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF5722))
                Text(" Racha: $streak", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            if (best > 0) Text("🏆 Récord: $best", fontSize = 12.sp, color = textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun QuestionCard(q: CultureQuestion) {
    Card(modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(20.dp)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Text(text = q.questionText, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp).fillMaxWidth())
    }
}

/**
 * Rejilla de opciones: Maneja el feedback de respuesta correcta/incorrecta mediante colores.
 */
@Composable
fun OptionsLayout(options: List<String>, q: CultureQuestion, selected: String?, vm: CultureViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        options.forEach { option ->
            if (option.isNotBlank()) {
                val isCorrect = option == q.correctAnswer
                val isChosen = option == selected
                val bgColor = when {
                    selected == null -> Color.White
                    isCorrect -> Color(0xFFC8E6C9)
                    isChosen && !isCorrect -> Color(0xFFFFCDD2)
                    else -> Color.White
                }
                Button(
                    onClick = { vm.checkAnswer(option) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text(option, color = Color.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            } else {
                // Espaciador para mantener la consistencia visual cuando el comodín elimina opciones
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

/**
 * Botón de Comodín: Evoluciona dinámicamente según la racha alcanzada.
 */
@Composable
fun WildcardButton(hasUsed: Boolean, selected: String?, vm: CultureViewModel) {
    val streak by vm.streak

    val buttonText = when {
        hasUsed -> "Comodín Gastado"
        streak >= 500 -> "Eliminación Total"
        else -> "Comodín 50/50"
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        OutlinedButton(
            onClick = { vm.useFiftyFifty() },
            enabled = !hasUsed && selected == null,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (hasUsed) Color.Transparent else Color(0xFFFFE082)
            ),
            modifier = Modifier.height(50.dp)
        ) {
            Icon(Icons.Default.Support, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(buttonText, fontWeight = FontWeight.Bold)
        }
    }
}