package com.example.pangeo.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R

/**
 * Pantalla de Menú de la "Biblioteca de Estudio".
 * * Actúa como un Hub educativo donde el usuario puede elegir entre diferentes
 * metodologías de aprendizaje (Atlas, Diccionario o Guía).
 * * A diferencia de los menús de juego, aquí se utiliza un diseño de tarjetas
 * horizontales para dar cabida a subtítulos descriptivos que guían al estudiante.
 *
 * @param onNavigateBack Retorno al Dashboard principal.
 * @param onNavigateToStudyMap Navegación al Atlas interactivo.
 * @param onNavigateToStudyFlags Navegación a las Flashcards de banderas.
 * @param onNavigateToStudyCapitals Navegación a la lista de estudio de capitales.
 */
@Composable
fun StudyMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudyMap: () -> Unit,
    onNavigateToStudyFlags: () -> Unit,
    onNavigateToStudyCapitals: () -> Unit
) {
    // Tipografía de marca consistente con el resto de la aplicación
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(24.dp)
    ) {
        // --- CABECERA ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al Dashboard",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Biblioteca de Estudio",
                fontSize = 32.sp,
                fontFamily = caveatFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Texto de apoyo para definir el "Modo de Estudio" (Sin estrés)
        Text(
            text = "Explora y aprende a tu ritmo sin presión de tiempo ni puntuación.",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- LISTADO DE CATEGORÍAS EDUCATIVAS ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StudyCategoryButton(
                title = "Atlas Geográfico",
                subtitle = "Explora los mapas y países",
                icon = Icons.Default.Place,
                color = Color(0xFFA3CFBF), // Verde Mapas
                onClick = onNavigateToStudyMap
            )

            StudyCategoryButton(
                title = "Diccionario de Banderas",
                subtitle = "Identifica colores y emblemas",
                icon = Icons.Outlined.Flag,
                color = Color(0xFFD55B67), // Rojo Banderas
                onClick = onNavigateToStudyFlags
            )

            StudyCategoryButton(
                title = "Guía de Capitales",
                subtitle = "Sedes administrativas del mundo",
                icon = Icons.Default.LocationCity,
                color = Color(0xFFE1B44B), // Dorado Capitales
                onClick = onNavigateToStudyCapitals
            )
        }
    }
}

/**
 * Componente de botón de categoría especializado para el estudio.
 * * Incluye un indicador lateral de color (Accent Bar) que ayuda a la
 * categorización visual rápida.
 * * @param title Nombre de la sección de estudio.
 * @param subtitle Breve explicación del objetivo educativo.
 * @param icon Icono temático.
 * @param color Color de identidad del módulo.
 */
@Composable
fun StudyCategoryButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Contenedor del Icono: Estilo circular con transparencia (Glassmorphism sutil)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Información Textual
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        /**
         * Barra de Acento Lateral:
         * Detalle estético que refuerza la pertenencia a un módulo específico
         * (Mapas, Banderas o Capitales) mediante el color.
         */
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(color)
                .align(Alignment.CenterStart)
        )
    }
}