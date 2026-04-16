package com.example.pangeo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R

/**
 * Pantalla de Logros y Récords de PanGeo.
 * * Centraliza el acceso a las estadísticas personales y a la clasificación global.
 * Utiliza una disposición de cuadrícula (Grid) para facilitar el acceso rápido
 * a los diferentes módulos de gamificación.
 *
 * @param onNavigateBack Acción para regresar al menú principal.
 * @param onNavigateToPersonalRecords Acción para ver el historial y logros propios.
 * @param onNavigateToRanking Acción para consultar la tabla de líderes mundial.
 */
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPersonalRecords: () -> Unit,
    onNavigateToRanking: () -> Unit
) {
    // Definición de tipografía de marca con gestión de seguridad en la carga
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp)
    ) {
        // --- BARRA DE NAVEGACIÓN SUPERIOR ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar al inicio")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logros y Récords",
                fontSize = 36.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Rejilla de Categorías:
         * Se opta por una [LazyVerticalGrid] de 2 columnas fijas.
         * Se aplica 'contentPadding' lateral para asegurar que las sombras proyectadas
         * no sean recortadas por los límites del contenedor principal (Shadow Clipping).
         */
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 20.dp, start = 4.dp, end = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                CategoryCard(
                    title = "Logros personales",
                    imageRes = R.drawable.mapamundo,
                    containerColor = Color(0xFFFFFDE7),
                    iconTint = Color(0xFFF39C12),
                    onClick = onNavigateToPersonalRecords
                )
            }
            item {
                CategoryCard(
                    title = "Ranking Global",
                    imageRes = R.drawable.ranking,
                    containerColor = Color(0xFFFFF0A2),
                    iconTint = Color(0xFFF39C12),
                    iconScale = 1.6f, // Ajuste visual por la densidad del icono de ranking
                    onClick = onNavigateToRanking
                )
            }
        }
    }
}

/**
 * Componente de tarjeta para categorías de logros.
 * * Implementa un sistema de sombras personalizado mediante un [Box] envolvente.
 * Esto permite un control más preciso sobre el 'ambientColor' y evita que la sombra
 * se corte prematuramente, logrando una estética "Neumórfica" suave.
 *
 * @param title Texto descriptivo de la sección.
 * @param imageRes ID del recurso gráfico.
 * @param containerColor Color temático de la tarjeta.
 * @param iconTint Tinte opcional para unificar la paleta de colores de los iconos.
 * @param iconScale Factor de escala para corregir visualmente iconos de distintos tamaños.
 * @param onClick Acción de navegación.
 */
@Composable
fun CategoryCard(
    title: String,
    imageRes: Int,
    containerColor: Color = Color.White,
    iconTint: Color? = null,
    iconScale: Float = 1f,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f) // Mantiene la tarjeta casi cuadrada
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            // Se desactiva la elevación nativa de la Card para usar la sombra personalizada del Box
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Contenedor del icono con peso flexible para centrado vertical
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.scale(iconScale),
                        colorFilter = iconTint?.let { ColorFilter.tint(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }
}