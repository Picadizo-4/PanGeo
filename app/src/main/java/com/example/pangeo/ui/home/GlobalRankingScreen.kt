package com.example.pangeo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.RankingViewModel

/**
 * Pantalla de Clasificación Global de PanGeo.
 * * Provee una visualización competitiva de los mejores exploradores, segmentada por
 * categorías de juego. Implementa carga perezosa (Lazy Loading) y estados de carga
 * (Loading States) para una experiencia de usuario fluida al consultar datos remotos.
 *
 * @param viewModel Lógica de obtención y filtrado del ranking desde el backend.
 * @param onNavigateBack Callback para regresar a la sección de Logros.
 */
@Composable
fun GlobalRankingScreen(viewModel: RankingViewModel, onNavigateBack: () -> Unit) {
    // Tipografía de cabecera con fallback seguro
    val caveatFamily = remember {
        try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
    }

    /**
     * Mapeo de Categorías:
     * Vincula los identificadores técnicos de la base de datos con nombres legibles
     * para el usuario final en la interfaz.
     */
    val categories = listOf(
        "banderas_botones" to "Banderas (Test)",
        "banderas_texto" to "Banderas (Escritura)",
        "capitales_botones" to "Capitales (Test)",
        "capitales_texto" to "Capitales (Escritura)",
        "mapas_botones" to "Mapas (Selección)",
        "mapas_texto" to "Mapas (Escritura)"
    )

    // Estado del selector de categoría
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Observación de flujos de datos reactivos del ViewModel
    val ranking by viewModel.rankingList
    val isLoading by viewModel.isLoading

    /**
     * Sincronización de Datos:
     * Dispara una nueva petición al servidor cada vez que el usuario cambia
     * el filtro de categoría.
     */
    LaunchedEffect(selectedIndex) {
        viewModel.fetchGlobalRanking(categories[selectedIndex].first)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F7FB)).padding(16.dp)) {
        // --- CABECERA ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text(
                text = "Ranking Global",
                fontSize = 32.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold
            )
        }

        /**
         * Selector de Categorías (Chips):
         * Implementa un scroll horizontal para dispositivos con pantallas estrechas,
         * permitiendo navegar por todos los modos de juego de forma ergonómica.
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { index, pair ->
                FilterChip(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    label = { Text(pair.second) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4A60B2),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Gestión visual del estado de carga (Network Latency Feedback)
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4A60B2))
            }
        } else {
            /**
             * Lista de Clasificación:
             * Utiliza [LazyColumn] para renderizar eficientemente solo los elementos
             * visibles en pantalla, optimizando el uso de memoria.
             */
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(ranking) { index, entry ->
                    RankingUserCard(position = index + 1, entry = entry)
                }
            }
        }
    }
}

/**
 * Componente visual para una entrada individual del ranking.
 * * Implementa un código de colores para el podio (Oro, Plata, Bronce)
 * con el fin de reforzar el reconocimiento del éxito del usuario.
 *
 * @param position Puesto en la clasificación (1-based index).
 * @param entry Datos del perfil y rendimiento del usuario.
 */
@Composable
fun RankingUserCard(position: Int, entry: com.example.pangeo.viewmodel.RankingEntry) {
    // Lógica cromática basada en mérito deportivo (Podio)
    val medalColor = when(position) {
        1 -> Color(0xFFFFD700) // Oro
        2 -> Color(0xFFC0C0C0) // Plata
        3 -> Color(0xFFCD7F32) // Bronce
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de posición con énfasis tipográfico
            Text(
                text = "#$position",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = medalColor,
                modifier = Modifier.width(45.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.nickname,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = entry.userRank,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Métricas de rendimiento: XP acumulado y Eficiencia temporal
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.totalScore} XP",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${entry.totalTime}s",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}