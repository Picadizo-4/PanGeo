package com.example.pangeo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.ui.game.culture.CultureRepository
import com.example.pangeo.viewmodel.RecordsViewModel

/**
 * Pantalla de Historial y Récords Personales (Mapa del Mundo).
 * * Presenta un desglose detallado de los aciertos del usuario categorizados por
 * continente y modo de juego. Su función es motivar al usuario mediante la
 * visualización del progreso porcentual hacia el dominio total de la geografía mundial.
 *
 * @param viewModel Fuente de datos que recupera los registros históricos desde Firebase.
 * @param onNavigateBack Acción de retorno a la pantalla de Logros.
 */
@Composable
fun WorldMapAchievementsScreen(
    viewModel: RecordsViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    /**
     * Observación del Diccionario de Récords:
     * Los datos se recuperan como un mapa de pares clave-valor desde el ViewModel.
     */
    val records by viewModel.personalRecords

    // Disparo de la petición de datos al entrar en la composición
    LaunchedEffect(Unit) {
        viewModel.fetchUserRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp)
    ) {
        // --- CABECERA DE SECCIÓN ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Mapa del Mundo",
                fontSize = 32.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor con scroll para navegar por las múltiples categorías de logros
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // --- SECCIÓN: CULTURA GENERAL ---
            // Muestra la racha actual y el rango honorífico obtenido en Supervivencia.
            SectionTitle("Cultura General", caveatFamily)
            val cultureStreak = records["cultura_supervivencia_streak"]?.toInt() ?: 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Supervivencia Cultural",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (cultureStreak > 0) {
                        val title = CultureRepository.getTitleForStreak(cultureStreak)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥 Racha:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$cultureStreak aciertos", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏅 Rango:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Aún no has iniciado tu expedición cultural.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            // --- CATEGORÍAS DE JUEGO ---
            // Se itera por las modalidades de juego usando un grid adaptativo.

            SectionTitle("Banderas (Test)", caveatFamily)
            ScoreGrid("banderas_botones", records, listOf(44, 35, 49, 54, 14))
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Banderas (Escritura)", caveatFamily)
            ScoreGrid("banderas_texto", records, listOf(44, 35, 49, 54, 14))
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Capitales (Test)", caveatFamily)
            ScoreGrid("capitales_botones", records, listOf(45, 35, 49, 54, 14))
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Capitales (Escritura)", caveatFamily)
            ScoreGrid("capitales_texto", records, listOf(45, 35, 49, 54, 14))
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Mapas (Selección)", caveatFamily)
            ScoreGrid("mapas_botones", records, listOf(48, 35, 49, 54, 14))
            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Mapas (Escritura)", caveatFamily)
            ScoreGrid("mapas_texto", records, listOf(48, 35, 49, 54, 14))

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Genera una cuadrícula de dos columnas para visualizar el progreso por continente.
 * * @param category Identificador de la modalidad de juego.
 * @param records Mapa de récords recuperado del servidor.
 * @param maxScores Lista de puntuaciones máximas configuradas para cada continente.
 */
@Composable
fun ScoreGrid(category: String, records: Map<String, Long>, maxScores: List<Int>) {
    val continents = listOf("europa", "america", "asia", "africa", "oceania")
    val titles = listOf("Europa", "América", "Asia", "África", "Oceanía")

    Column {
        for (i in continents.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Cálculo de puntuación (asumiendo 10 XP por acierto)
                val score1 = (records["${continents[i]}_${category}_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = titles[i], currentScore = score1, maxScore = maxScores[i])

                if (i + 1 < continents.size) {
                    val score2 = (records["${continents[i+1]}_${category}_score"]?.toInt() ?: 0) / 10
                    ContinentScoreCard(modifier = Modifier.weight(1f), title = titles[i+1], currentScore = score2, maxScore = maxScores[i+1])
                } else {
                    Spacer(modifier = Modifier.weight(1f)) // Placeholder para mantener el alineamiento
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String, fontFamily: FontFamily) {
    Text(
        text = title,
        fontSize = 24.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4A60B2),
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}

/**
 * Tarjeta individual de progreso continental.
 * * Implementa una barra de progreso inteligente que cambia de color al alcanzar
 * el 100% de los aciertos, proporcionando una gratificación visual instantánea.
 */
@Composable
fun ContinentScoreCard(modifier: Modifier = Modifier, title: String, currentScore: Int, maxScore: Int) {
    val progress = if (maxScore > 0) (currentScore.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    // Verde para completado, Ámbar para progreso en curso
    val barColor = if (percentage == 100) Color(0xFF4CAF50) else Color(0xFFFFC107)

    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                    text = "$percentage%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (percentage == 100) Color(0xFF4CAF50) else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Contenedor de la barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(barColor))

                // Indicador numérico centralizado
                Text(
                    text = "$currentScore/$maxScore",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center),
                    color = if (progress > 0.5f) Color.White else Color.Black
                )
            }
        }
    }
}