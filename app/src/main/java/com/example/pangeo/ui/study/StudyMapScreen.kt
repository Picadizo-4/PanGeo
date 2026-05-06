package com.example.pangeo.ui.study

import android.graphics.RectF
import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pangeo.R
import com.example.pangeo.viewmodel.StudyMapViewModel

/**
 * Pantalla de exploración libre (Atlas Interactivo).
 * * Permite al usuario interactuar con un mapa vectorial de Europa para aprender
 * la ubicación de los países y sus banderas asociadas mediante gestos táctiles.
 * * Características técnicas:
 * 1. Proyección Cartográfica: Traduce toques en la pantalla a coordenadas del SVG.
 * 2. Cámara 2D: Implementa Pan y Zoom (Pinch-to-zoom) con límites de seguridad.
 * 3. Mapeo de Recursos Dinámico: Resuelve IDs de banderas en tiempo de ejecución.
 *
 * @param onNavigateBack Callback para el retorno al menú de estudio.
 * @param viewModel Gestiona el estado de selección del país y provee los datos geográficos.
 */
@Composable
fun StudyMapScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudyMapViewModel = viewModel()
) {
    val context = LocalContext.current
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    val countries = viewModel.countries
    val selectedId by viewModel.selectedCountryId

    // Estados de transformación de la cámara (Zoom y Pan)
    var zoom by remember { mutableFloatStateOf(1.8f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F7FB))) {

        /**
         * Renderizado del Mapa Vectorial (Canvas):
         * Se encarga de dibujar cada país y gestionar los gestos táctiles.
         */
        Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        /**
                         * Gestión de Zoom y Pan:
                         * Permite escalar el mapa hasta x10 para visualizar microestados (ej. Vaticano).
                         */
                        detectTransformGestures { centroid, pan, zoomChange, _ ->
                            val oldZoom = zoom
                            zoom = (zoom * zoomChange).coerceIn(1f, 10f)
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val center = Offset(w / 2f, h / 2f)

                            val newPanX = (panX + pan.x) - (centroid.x - center.x) * (zoom / oldZoom - 1f)
                            val newPanY = (panY + pan.y) - (centroid.y - center.y) * (zoom / oldZoom - 1f)

                            val maxX = (w * (zoom - 1f) / 2f) + (w * 0.4f)
                            val maxY = (h * (zoom - 1f) / 2f) + (h * 0.4f)
                            panX = newPanX.coerceIn(-maxX, maxX)
                            panY = newPanY.coerceIn(-maxY, maxY)
                        }
                    }
                    .pointerInput(Unit) {
                        /**
                         * Detección de Colisión (Hit Testing):
                         * Convierte el punto de presión en pantalla a la escala real del SVG original (1000x684).
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

                            // Transformación inversa para hallar la coordenada real en el SVG
                            val viewX = (offset.x - (panX + center.x * (1f - zoom))) / zoom
                            val viewY = (offset.y - (panY + center.y * (1f - zoom))) / zoom
                            val realX = (viewX - baseX) / baseScale
                            val realY = (viewY - baseY) / baseScale

                            var clickedId: String? = null
                            // Comprobación de colisión sobre el polígono del país (Región)
                            for (country in countries.reversed()) {
                                val path = country.path.asAndroidPath()
                                val rectF = RectF()
                                path.computeBounds(rectF, true)
                                val region = Region().apply { setPath(path, Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())) }
                                if (region.contains(realX.toInt(), realY.toInt())) {
                                    clickedId = country.id
                                    break
                                }
                            }
                            clickedId?.let { viewModel.onCountryClick(it) } ?: run { viewModel.selectedCountryId.value = null }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                // Aplicación de transformaciones en cascada
                translate(left = panX + center.x * (1f - zoom), top = panY + center.y * (1f - zoom)) {
                    scale(scale = zoom, pivot = Offset.Zero) {
                        val svgW = 1000f
                        val svgH = 684f
                        val baseScale = minOf(w / svgW, h / svgH) * 0.95f
                        val baseX = (w - (svgW * baseScale)) / 2f
                        val baseY = (h - (svgH * baseScale)) / 2f

                        translate(left = baseX, top = baseY) {
                            scale(scale = baseScale, pivot = Offset.Zero) {
                                countries.forEach { country ->
                                    // Resaltado cromático del país seleccionado
                                    val color = if (country.id == selectedId) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
                                    drawPath(path = country.path, color = color)
                                    drawPath(path = country.path, color = Color.Black.copy(0.1f), style = Stroke(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- INTERFAZ DE NAVEGACIÓN (HUD) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.shadow(4.dp, CircleShape).background(Color.White, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "Atlas: Europa",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 20.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- TARJETA DE DETALLE DEL PAÍS ---
        selectedId?.let { id ->
            val country = countries.find { it.id == id }
            country?.let {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 40.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = it.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4A60B2),
                                    lineHeight = 26.sp
                                )
                                Text(
                                    text = "Toca el mapa para cerrar",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            /**
                             * Resolución dinámica de la Bandera:
                             * Traduce el código ISO del país al nombre del recurso drawable
                             * definido en el repositorio del proyecto.
                             */
                            val flagResId = remember(id) {
                                val resName = when (id) {
                                    "ES" -> "eu_spain"
                                    "FR" -> "eu_france"
                                    "IT" -> "eu_italy"
                                    "DE" -> "eu_germany"
                                    "GB" -> "eu_greatbritain"
                                    "PT" -> "eu_portugal"
                                    "NL" -> "eu_netherlands"
                                    "BE" -> "eu_belgium"
                                    "CH" -> "eu_switzerland"
                                    "AT" -> "eu_austria"
                                    "CZ" -> "eu_czechrepublic"
                                    "PL" -> "eu_poland"
                                    "DK" -> "eu_denmark"
                                    "NO" -> "eu_norway"
                                    "SE" -> "eu_sweden"
                                    "FI" -> "eu_finland"
                                    "EE" -> "eu_estonia"
                                    "LV" -> "eu_latvia"
                                    "LT" -> "eu_lithuania"
                                    "BY" -> "eu_belarus"
                                    "UA" -> "eu_ukraine"
                                    "MD" -> "eu_moldova"
                                    "RO" -> "eu_romania"
                                    "BG" -> "eu_bulgaria"
                                    "GR" -> "eu_greece"
                                    "AL" -> "eu_albania"
                                    "MK" -> "eu_macedonia"
                                    "ME" -> "eu_montenegro"
                                    "RS" -> "eu_serbia"
                                    "BA" -> "eu_bosnia"
                                    "SI" -> "eu_slovenia"
                                    "HR" -> "eu_croatia"
                                    "HU" -> "eu_hungary"
                                    "SK" -> "eu_slovakia"
                                    "IE" -> "eu_ireland"
                                    "IS" -> "eu_iceland"
                                    "LU" -> "eu_luxembourg"
                                    "CY" -> "eu_cyprus"
                                    "XK" -> "eu_kosovo"
                                    "AD" -> "eu_andorra"
                                    "LI" -> "eu_liechtenstein"
                                    "MC" -> "eu_monaco"
                                    "SM" -> "eu_sanmarino"
                                    "VA" -> "eu_vatikan"
                                    "MT" -> "eu_malta"
                                    "TR" -> "eu_turkey"
                                    "GE" -> "eu_georgia"
                                    "AM" -> "eu_armenia"

                                    else -> "eu_${id.lowercase()}"
                                }
                                context.resources.getIdentifier(resName, "drawable", context.packageName)
                            }

                            if (flagResId != 0) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    modifier = Modifier.size(width = 70.dp, height = 45.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = flagResId),
                                        contentDescription = "Bandera de ${it.name}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}