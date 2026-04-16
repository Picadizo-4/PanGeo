package com.example.pangeo.ui.study

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pangeo.R
import com.example.pangeo.viewmodel.StudyFlagsViewModel

/**
 * Pantalla de estudio de banderas (Flashcards).
 * * Implementa una rejilla de tarjetas interactivas con efecto de rotación 3D.
 * Esta técnica permite al usuario realizar un estudio visual de las banderas
 * y verificar el nombre del país y su capital al "voltear" la tarjeta.
 *
 * @param onNavigateBack Callback para retornar al menú de estudio.
 * @param viewModel Provee la lista de banderas cargadas desde el repositorio.
 */
@Composable
fun StudyFlagsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudyFlagsViewModel = viewModel()
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val flags = viewModel.flagsList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(16.dp)
    ) {
        // --- CABECERA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Diccionario de Banderas",
                fontSize = 32.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Rejilla de Banderas:
         * Se utiliza [LazyVerticalGrid] para optimizar la memoria renderizando solo las
         * tarjetas visibles. La disposición de 2 columnas es ideal para el formato
         * de tarjetas informativas.
         */
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = flags) { flag ->
                FlipFlagCard(flag.correctAnswer, flag.flagImageRes)
            }
        }
    }
}

/**
 * Componente de tarjeta con efecto de volteo (Flip).
 * * Utiliza [graphicsLayer] para animar la rotación en el eje Y.
 * Implementa una lógica de cara A/B:
 * - Cara A: Muestra la bandera a pantalla completa.
 * - Cara B: Muestra el nombre del país, su capital y una miniatura de la bandera.
 *
 * @param countryName Nombre del país que se mostrará en el reverso.
 * @param flagResId Recurso de la bandera para ambas caras.
 */
@Composable
fun FlipFlagCard(countryName: String, flagResId: Int) {
    var isFlipped by remember { mutableStateOf(false) }

    // Animación de rotación suave de 0 a 180 grados
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    /**
     * Diccionario de apoyo local:
     * Mapea el nombre del país a su capital para enriquecer la información del reverso
     * sin necesidad de peticiones adicionales al repositorio en este contexto.
     */
    val capital = remember(countryName) {
        when (countryName) {
            "Albania" -> "Tirana"
            "Andorra" -> "Andorra la Vella"
            "Austria" -> "Viena"
            "Bielorrusia" -> "Minsk"
            "Bélgica" -> "Bruselas"
            "Bosnia y Herzegovina" -> "Sarajevo"
            "Bulgaria" -> "Sofía"
            "Croacia" -> "Zagreb"
            "República Checa" -> "Praga"
            "Dinamarca" -> "Copenhague"
            "Estonia" -> "Tallin"
            "Finlandia" -> "Helsinki"
            "Francia" -> "París"
            "Alemania" -> "Berlín"
            "Grecia" -> "Atenas"
            "Hungría" -> "Budapest"
            "Islandia" -> "Reikiavik"
            "Irlanda" -> "Dublín"
            "Italia" -> "Roma"
            "Letonia" -> "Riga"
            "Lituania" -> "Vilna"
            "Luxemburgo" -> "Luxemburgo"
            "Malta" -> "La Valeta"
            "Moldavia" -> "Chisináu"
            "Mónaco" -> "Mónaco"
            "Montenegro" -> "Podgorica"
            "Países Bajos" -> "Ámsterdam"
            "Noruega" -> "Oslo"
            "Polonia" -> "Varsovia"
            "Portugal" -> "Lisboa"
            "Rumanía" -> "Bucarest"
            "Rusia" -> "Moscú"
            "San Marino" -> "San Marino"
            "Serbia" -> "Belgrado"
            "Eslovaquia" -> "Bratislava"
            "Eslovenia" -> "Liubliana"
            "España" -> "Madrid"
            "Suecia" -> "Estocolmo"
            "Suiza" -> "Berna"
            "Ucrania" -> "Kiev"
            "Vaticano" -> "Ciudad del Vaticano"
            "Reino Unido" -> "Londres"
            else -> ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .graphicsLayer {
                rotationY = rotation
                // cameraDistance es crucial para evitar el efecto de "estiramiento"
                // durante la rotación 3D en pantallas de alta densidad.
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Lógica de renderizado condicional según el ángulo de rotación
            if (rotation <= 90f) {
                // --- CARA A: Identificación Visual ---
                Image(
                    painter = painterResource(id = flagResId),
                    contentDescription = "Bandera de $countryName",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // --- CARA B: Información de Retorno ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF4A60B2)) // Azul identidad PanGeo
                        .graphicsLayer { rotationY = 180f } // Invertimos el contenido para que se lea bien
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Miniatura de referencia
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(width = 60.dp, height = 40.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = flagResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = countryName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    if (capital.isNotEmpty()) {
                        Text(
                            text = "($capital)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}