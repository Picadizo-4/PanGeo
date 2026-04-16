package com.example.pangeo.ui.game.maps

import android.graphics.RectF
import android.graphics.Region
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel
import com.example.pangeo.viewmodel.MapsViewModel

/**
 * Pantalla interactiva del Mapa de Europa.
 * * Implementa un motor cartográfico personalizado sobre [Canvas] que permite:
 * 1. Transformaciones espaciales: Zoom (escalado) y Pan (desplazamiento) con límites calculados.
 * 2. Hit Testing: Detección de colisiones mediante polígonos [Region] para identificar países.
 * 3. Gamificación: Modos de selección táctil y reconocimiento por escritura.
 * * @param viewModel Lógica de estados del mapa, puntuación y cronómetro.
 * @param authViewModel Persistencia de XP y récords en la nube (Firebase).
 */
@Composable
fun EuropeMapScreen(
    viewModel: MapsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    var showIntro by remember { mutableStateOf(true) }
    var isNewBestTime by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    /** * Estados de la Cámara:
     * Controlan la vista actual del usuario sobre el mapa vectorial.
     */
    var zoom by remember { mutableFloatStateOf(1.8f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    // Observación de flujos de datos reactivos
    val targetCountry by viewModel.targetCountry.collectAsState()
    val countryStates by viewModel.countryStates.collectAsState()
    val score by viewModel.score.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val blinkingCountry by viewModel.blinkingCountry.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    val gameMode = viewModel.gameMode
    val maxPossibleScore = viewModel.europeCountries.size * 10
    val formattedTime = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60)

    // Esquema de colores para el feedback del mapa
    val colorDefault = Color(0xFFFFEB3B)
    val colorCorrect = Color(0xFF4CAF50)
    val colorPartial = Color(0xFFFF9800)
    val colorWrong = Color(0xFFF44336)
    val colorHighlighted = Color(0xFF2196F3)

    /**
     * Animación de Parpadeo (Blink):
     * Proporciona feedback visual cuando el usuario falla o se resalta un país.
     */
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(150, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

    DisposableEffect(Unit) { onDispose { viewModel.stopTimer() } }
    LaunchedEffect(currentIndex) { inputText = "" }

    /**
     * Sincronización Final:
     * Calcula bonificaciones por perfección y registra el rendimiento en Firebase.
     */
    LaunchedEffect(isGameOver) {
        if (isGameOver && !showIntro) {
            var finalXP = score
            if (score >= maxPossibleScore && maxPossibleScore > 0) finalXP += 500
            authViewModel.addXP(finalXP)

            val modalidad = if (gameMode == "botones") "mapas_botones" else "mapas_texto"
            authViewModel.saveGameRecord("europa_$modalidad", score, elapsedTime) { newTime ->
                isNewBestTime = newTime
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        if (showIntro) {
            // --- ESTADO 1: SELECTOR DE MODALIDAD ---
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
                Spacer(modifier = Modifier.weight(0.2f))
                Image(painter = painterResource(id = R.drawable.mapaeuropa), contentDescription = null, modifier = Modifier.size(160.dp))
                Text("Expedición: Europa", fontSize = 42.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.weight(0.3f))
                Button(
                    onClick = { showIntro = false; viewModel.resetGame("botones") },
                    modifier = Modifier.fillMaxWidth().height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005937)),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("MODO SELECCIÓN", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showIntro = false; viewModel.resetGame("texto") },
                    modifier = Modifier.fillMaxWidth().height(65.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CB81)),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("MODO ESCRITURA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                Spacer(modifier = Modifier.height(40.dp))
            }
        } else if (isGameOver) {
            // --- ESTADO 2: RESUMEN DE PUNTUACIÓN Y RÉCORDS ---
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (score >= maxPossibleScore) {
                    Text("¡PLENO TOTAL!", fontSize = 38.sp, fontFamily = caveatFamily, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Text("+500 XP Bonus", fontSize = 20.sp, color = Color(0xFFF39C12), fontWeight = FontWeight.Black)
                } else {
                    Text("¡Expedición Terminada!", fontSize = 34.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("$score XP", fontSize = 48.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(25.dp))
                Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                    Text("Tiempo: $formattedTime", modifier = Modifier.padding(16.dp), fontSize = 22.sp, fontFamily = FontFamily.Monospace)
                }
                if (isNewBestTime) {
                    Text("⭐ ¡Nuevo Récord Personal! ⭐", color = Color(0xFFF39C12), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(modifier = Modifier.height(25.dp))
                Button(onClick = { zoom = 1.8f; panX = 0f; panY = 0f; viewModel.resetGame(gameMode) }, modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("VOLVER A JUGAR") }
                TextButton(onClick = onNavigateBack) { Text("Salir al menú") }
            }
        } else {
            // --- ESTADO 3: GAMEPLAY INTERACTIVO ---
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.stopTimer(); onNavigateBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                    Text(text = formattedTime, fontSize = 26.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("XP: $score", fontSize = 22.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Black)
                }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (gameMode == "botones") {
                            Text("Toca el país:", color = Color.Gray)
                            Text(targetCountry?.name ?: "", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A60B2))
                        } else {
                            Text("Adivina el país azul:", fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }

                // --- MOTOR GRÁFICO (CANVAS) ---
                Box(modifier = Modifier.weight(1f).fillMaxWidth().clipToBounds(), contentAlignment = Alignment.Center) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                /** * Detección de Gestos de Cámara:
                                 * Gestiona el escalado pinch-to-zoom y el arrastre (pan).
                                 * Implementa un sistema de 'clamping' para evitar que el mapa se salga de la vista.
                                 */
                                detectTransformGestures { centroid, pan, zoomChange, _ ->
                                    val oldZoom = zoom
                                    zoom = (zoom * zoomChange).coerceIn(1f, 6f)
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val center = Offset(w / 2f, h / 2f)
                                    val newPanX = (panX + pan.x) - (centroid.x - center.x) * (zoom / oldZoom - 1f)
                                    val newPanY = (panY + pan.y) - (centroid.y - center.y) * (zoom / oldZoom - 1f)
                                    val maxX = (w * (zoom - 1f) / 2f) + (w * 0.2f)
                                    val maxY = (h * (zoom - 1f) / 2f) + (h * 0.2f)
                                    panX = newPanX.coerceIn(-maxX, maxX)
                                    panY = newPanY.coerceIn(-maxY, maxY)
                                }
                            }
                            .pointerInput(Unit) {
                                /**
                                 * Detección de Toque sobre País:
                                 * Traduce la coordenada de la pantalla (Viewport) a la coordenada del SVG original.
                                 * Utiliza inversión de matrices de transformación para el cálculo preciso.
                                 */
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val svgW = 1000f
                                    val svgH = 684f
                                    val baseScale = minOf(w / svgW, h / svgH) * 0.95f
                                    val baseX = (w - (svgW * baseScale)) / 2f
                                    val baseY = (h - (svgH * baseScale)) / 2f
                                    val center = Offset(w / 2f, h / 2f)

                                    // Inversión de zoom y pan para hallar el punto real en el SVG
                                    val viewX = (offset.x - (panX + center.x * (1f - zoom))) / zoom
                                    val viewY = (offset.y - (panY + center.y * (1f - zoom))) / zoom
                                    val realX = (viewX - baseX) / baseScale
                                    val realY = (viewY - baseY) / baseScale

                                    var clickedId: String? = null
                                    // Iteración inversa para respetar el Z-index visual
                                    for (country in viewModel.europeCountries.reversed()) {
                                        val path = country.path.asAndroidPath()
                                        val rectF = RectF()
                                        path.computeBounds(rectF, true)
                                        val region = Region().apply { setPath(path, Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())) }
                                        if (region.contains(realX.toInt(), realY.toInt())) {
                                            clickedId = country.id
                                            break
                                        }
                                    }
                                    clickedId?.let { viewModel.onCountrySelected(it) }
                                }
                            }
                    ) {
                        /**
                         * Renderizado del Mapa:
                         * Aplica las transformaciones en cascada: Desplazamiento -> Zoom de usuario -> Escalado SVG.
                         */
                        val w = size.width
                        val h = size.height
                        val center = Offset(w / 2f, h / 2f)
                        translate(left = panX + center.x * (1f - zoom), top = panY + center.y * (1f - zoom)) {
                            scale(scale = zoom, pivot = Offset.Zero) {
                                val svgW = 1000f
                                val svgH = 684f
                                val baseScale = minOf(w / svgW, h / svgH) * 0.95f
                                val baseX = (w - (svgW * baseScale)) / 2f
                                val baseY = (h - (svgH * baseScale)) / 2f
                                translate(left = baseX, top = baseY) {
                                    scale(scale = baseScale, pivot = Offset.Zero) {
                                        viewModel.europeCountries.forEach { country ->
                                            val color = when (countryStates[country.id]) {
                                                "CORRECT" -> colorCorrect
                                                "CORRECT_PARTIAL" -> colorPartial
                                                "WRONG" -> colorWrong
                                                "HIGHLIGHTED" -> colorHighlighted
                                                else -> if (country.id == blinkingCountry) colorWrong.copy(alpha = blinkAlpha) else colorDefault
                                            }
                                            drawPath(path = country.path, color = color)
                                            drawPath(path = country.path, color = Color.Black.copy(0.15f), style = Stroke(1.2f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Control de entrada para el Modo Escritura
                if (gameMode == "texto") {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it; viewModel.checkTextAnswer(it) },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        label = { Text("Escribe el nombre...") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.skipCountry() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)), modifier = Modifier.fillMaxWidth()) { Text("SALTAR PAÍS") }
                } else if (attempts == 1) {
                    Text("¡Incorrecto! Te queda 1 intento", color = colorWrong, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}