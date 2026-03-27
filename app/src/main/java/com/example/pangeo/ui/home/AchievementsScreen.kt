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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter // Importante para tintar la imagen
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp // Importante para el padding
import androidx.compose.ui.unit.sp
import com.example.pangeo.R

@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWorldMap: () -> Unit,
    onNavigateToRanking: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB)) // Fondo grisáceo muy claro para que resalten las tarjetas
            .padding(16.dp)
    ) {
        // Cabecera
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logros", fontSize = 32.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid de categorías
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // CONFIGURACIÓN PEDIDA: Mapa del mundo
                CategoryCard(
                    title = "Mapa del mundo",
                    imageRes = R.drawable.mapamundo,
                    // Amarillo claro suave
                    containerColor = Color(0xFFFFFDE7),
                    // Resaltamos el mapa en el azul principal de la app
                    iconTint = Color(0xFF4A60B2),
                    onClick = onNavigateToWorldMap
                )
            }
            item {
                // --- NUEVA TARJETA DE RANKING ---
                CategoryCard(
                    title = "Ranking Global",
                    imageRes = R.drawable.ranking,
                    containerColor = Color(0xFFFCE4EC), // Un rosita/rojizo claro muy chulo
                    iconTint = Color(0xFFD81B60), // Color magenta/rojo oscuro
                    onClick = onNavigateToRanking
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    imageRes: Int,
    containerColor: Color = Color.White,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // =========================================================================
    // --- NUEVO: Material3 Card con Elevación y Padding Exterior ---
    // Usamos padding periférico para dar espacio a la sombra difusa.
    // =========================================================================
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // =============================================================
            // --- AQUÍ ESTÁ LA SOLUCIÓN PROFESIONAL ---
            // Añadimos padding exterior (espacio para la sombra).
            // ¡Es vital que vaya ANTES del aspectRatio!
            // =============================================================
            .padding(16.dp)
            .aspectRatio(0.85f), // Proporción vertical tipo Duolingo
        shape = RoundedCornerShape(20.dp), // Bordes bien redondeados
        colors = CardDefaults.cardColors(containerColor = containerColor),

        // Mantenemos la elevación profesional (sombra)
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp, // Sombra notable
            pressedElevation = 2.dp,   // Efecto de pulsación
            hoveredElevation = 10.dp
        )
    ) {
        // Mantenemos el clickable y el contenido igual
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.weight(1f).padding(8.dp),
                colorFilter = iconTint?.let { ColorFilter.tint(it) }
            )
            Text(
                text = title,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = if (iconTint != null) Color.Black else Color.Unspecified,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}