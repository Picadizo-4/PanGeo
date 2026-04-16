package com.example.pangeo.ui.game.maps

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
 * Pantalla de selección regional para el modo de juego de Mapas (Identificación Geográfica).
 * * Esta interfaz permite al usuario elegir en qué continente desea realizar su
 * expedición cartográfica. Utiliza una estética coherente con el resto de menús
 * pero diferenciada mediante el código de color verde esmeralda.
 * * @param onNavigateBack Acción para regresar al selector de tipos de desafío.
 * @param onNavigateToEurope Acción para iniciar el juego interactivo del mapa europeo.
 */
@Composable
fun MapsMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEurope: () -> Unit
) {
    // Gestión de tipografía de marca con manejo de excepciones de carga
    val caveatFamily = remember {
        try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif }
    }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Marca de agua de fondo para dar profundidad visual sin distraer del contenido
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
            // --- CABECERA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black
                    )
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

            // --- REGIÓN ACTIVA: EUROPA ---
            // Se utiliza el color verde agua para mantener la consistencia con la categoría en la HomeScreen
            PrivateContinentButton(
                title = "Europa",
                imageRes = R.drawable.mapaeuropa,
                color = Color(0xFFA3CFBF),
                onClick = onNavigateToEurope
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN "EN DESARROLLO" ---
            Text(
                text = "Próximamente",
                fontSize = 24.sp,
                fontFamily = caveatFamily,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            PrivateLockedContinentButton("América")
            Spacer(modifier = Modifier.height(16.dp))
            PrivateLockedContinentButton("Asia")
            Spacer(modifier = Modifier.height(16.dp))
            PrivateLockedContinentButton("África")
            Spacer(modifier = Modifier.height(16.dp))
            PrivateLockedContinentButton("Oceanía")

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Componente privado para evitar colisiones de nombres en el espacio de nombres global.
 * Representa un botón de continente funcional.
 */
@Composable
private fun PrivateContinentButton(
    title: String,
    imageRes: Int,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
 * Componente privado que visualiza continentes bloqueados con feedback visual
 * de inactividad (candado y opacidad reducida).
 */
@Composable
private fun PrivateLockedContinentButton(title: String) {
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
                contentDescription = "Región aún no disponible",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}