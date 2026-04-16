package com.example.pangeo.ui.games.flags

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
 * Pantalla de selección regional para el modo de juego de Banderas.
 * * Provee un acceso jerárquico a las expediciones por continente.
 * * Sigue el patrón de diseño de "descubrimiento progresivo", mostrando
 * claramente qué contenido está listo para jugar y cuál se encuentra en desarrollo.
 *
 * @param onNavigateBack Acción para regresar al selector de tipos de juego.
 * @param onNavigateToEurope Acción para iniciar el desafío de banderas europeas.
 */
@Composable
fun FlagsMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEurope: () -> Unit
) {
    // Definición de tipografía manuscrita para coherencia de marca
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Fondo temático: Mapa mundi sutil (marca de agua)
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
            // --- CABECERA DE NAVEGACIÓN ---
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

            // --- CONTENIDO ACTIVO: EUROPA ---
            // Se utiliza el color granate/rojo característico de la sección de banderas
            ContinentButton(
                title = "Europa",
                imageRes = R.drawable.mapaeuropa,
                onClick = onNavigateToEurope
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE PRÓXIMOS LANZAMIENTOS ---
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
 * Componente de tarjeta para continentes habilitados.
 * * @param title Nombre de la región geográfica.
 * @param imageRes Referencia al recurso visual del mapa regional.
 * @param onClick Evento de navegación al ser pulsado.
 */
@Composable
fun ContinentButton(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD55B67))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor circular para el icono regional
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
 * Componente visual para regiones en desarrollo.
 * Utiliza una estética de "deshabilitado" mediante colores neutros y opacidad baja.
 */
@Composable
fun LockedContinentButton(title: String) {
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
                contentDescription = "Bloqueado",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}