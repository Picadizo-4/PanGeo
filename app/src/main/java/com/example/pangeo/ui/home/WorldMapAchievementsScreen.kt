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
import com.example.pangeo.viewmodel.RecordsViewModel

@Composable
fun WorldMapAchievementsScreen(
    viewModel: RecordsViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val records by viewModel.personalRecords

    // Cargamos los datos al entrar
    LaunchedEffect(Unit) {
        viewModel.fetchUserRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Mapa del Mundo", fontSize = 28.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // --- SECCIÓN: BANDERAS ---
            SectionTitle("Banderas", caveatFamily)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dividimos entre 10 para sacar los países. Los maxScore pierden un '0'
                val euScore = (records["europa_banderas_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Europa", currentScore = euScore, maxScore = 44, caveatFamily)

                val amScore = (records["america_banderas_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "América", currentScore = amScore, maxScore = 35, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val asScore = (records["asia_banderas_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Asia", currentScore = asScore, maxScore = 49, caveatFamily)

                val afScore = (records["africa_banderas_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "África", currentScore = afScore, maxScore = 54, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val asScore = (records["oceania_banderas_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Oceanía", currentScore = asScore, maxScore = 14, caveatFamily)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: CAPITALES ---
            SectionTitle("Capitales", caveatFamily)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euCapScore = (records["europa_capitales_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Europa", currentScore = euCapScore, maxScore = 44, caveatFamily)

                val amCapScore = (records["america_capitales_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "América", currentScore = amCapScore, maxScore = 35, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euCapScore = (records["asia_capitales_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Asia", currentScore = euCapScore, maxScore = 49, caveatFamily)

                val amCapScore = (records["africa_capitales_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "África", currentScore = amCapScore, maxScore = 54, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euCapScore = (records["oceania_capitales_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Oceanía", currentScore = euCapScore, maxScore = 14, caveatFamily)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: MAPA ---
            SectionTitle("Mapa", caveatFamily)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euMapScore = (records["europa_mapa_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Europa", currentScore = euMapScore, maxScore = 44, caveatFamily)

                val amMapScore = (records["america_mapa_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "América", currentScore = amMapScore, maxScore = 35, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euMapScore = (records["asia_mapa_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Asia", currentScore = euMapScore, maxScore = 49, caveatFamily)

                val amMapScore = (records["africa_mapa_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "África", currentScore = amMapScore, maxScore = 54, caveatFamily)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val euMapScore = (records["europa_mapa_score"]?.toInt() ?: 0) / 10
                ContinentScoreCard(modifier = Modifier.weight(1f), title = "Oceanía", currentScore = euMapScore, maxScore = 14, caveatFamily)
            }

            Spacer(modifier = Modifier.height(40.dp))
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
        color = Color(0xFFE1B44B), // Color doradito
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ContinentScoreCard(modifier: Modifier = Modifier, title: String, currentScore: Int, maxScore: Int, fontFamily: FontFamily) {
    // Calculamos el progreso (0.0 a 1.0) y evitamos que pase de 1.0
    val progress = if (maxScore > 0) (currentScore.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f) else 0f

    // Calculamos el porcentaje entero (Ej: 50)
    val percentage = (progress * 100).toInt()

    // Color: Amarillo bonito por defecto, Verde si está completado (100%)
    val barColor = if (percentage == 100) Color(0xFF4CAF50) else Color(0xFFFFC107)

    // Color del texto del porcentaje: Verde si completado, Gris si no
    val percentageColor = if (percentage == 100) Color(0xFF4CAF50) else Color.Gray

    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior: Título a la izquierda, Porcentaje a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 20.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(text = "$percentage%", fontSize = 18.sp, fontFamily = fontFamily, fontWeight = FontWeight.Bold, color = percentageColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F0F0)) // Gris muy clarito para el fondo de la barra
            ) {
                // Relleno de la barra (Amarillo o Verde)
                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(barColor))

                // Texto de la fracción (Ej: 22 / 44) centrado
                Text(
                    text = "$currentScore / $maxScore",
                    fontSize = 12.sp,
                    color = if (progress > 0.5f) Color.White else Color.DarkGray, // Cambia el color si la barra pasa por debajo
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}