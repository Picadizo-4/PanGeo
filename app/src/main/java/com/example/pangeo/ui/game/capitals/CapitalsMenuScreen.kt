package com.example.pangeo.ui.game.capitals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R

/**
 * Pantalla de selección de región para el modo de juego de Capitales.
 * * Actúa como un Hub de navegación donde se presentan las regiones disponibles
 * y se previsualizan los contenidos bloqueados (Roadmap de la aplicación).
 * * @param onNavigateBack Callback para retornar al menú principal de juegos.
 * @param onNavigateToEurope Callback para iniciar la expedición en el continente europeo.
 */
@Composable
fun CapitalsMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEurope: () -> Unit
) {
    // Definición de tipografía manuscrita con manejo de fallback
    val caveatFamily = remember {
        try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
    }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Capa de fondo: Mapa mundi con baja opacidad (Watermark style)
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.06f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // --- NAVEGACIÓN Y TÍTULO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continentes",
                    fontSize = 40.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Selecciona una región para empezar tu expedición",
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- REGIÓN DISPONIBLE: EUROPA ---
            // Implementación del botón principal usando el esquema de color dorado de Capitales
            ContinentButton(
                title = "Europa",
                imageRes = R.drawable.mapaeuropa,
                onClick = onNavigateToEurope
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE CONTENIDO BLOQUEADO ---
            // Indica visualmente las futuras expansiones del catálogo geográfico
            Text(
                text = "Próximamente",
                fontSize = 24.sp,
                fontFamily = caveatFamily,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            LockedContinentButton("América")
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("Asia")
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("África")
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("Oceanía")

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Componente interno para representar una región activa.
 * * @param title Nombre del continente.
 * @param imageRes ID del recurso gráfico del mapa regional.
 * @param onClick Acción a ejecutar al seleccionar el continente.
 */
@Composable
private fun ContinentButton(
    title: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1B44B)) // Color dorado distintivo
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del icono con fondo traslúcido para mejorar el contraste
            Surface(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = title,
                fontSize = 26.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/**
 * Componente interno para representar regiones no disponibles.
 * Utiliza una opacidad reducida y un icono de candado para comunicar
 * el estado de bloqueo de forma intuitiva al usuario.
 */
@Composable
private fun LockedContinentButton(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .alpha(0.6f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Contenido bloqueado",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}