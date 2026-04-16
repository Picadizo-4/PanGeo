package com.example.pangeo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel

/**
 * Representa una opción de juego dentro del panel principal.
 * * @property title Nombre descriptivo del módulo.
 * @property icon Icono representativo de la categoría.
 * @property mainColor Color temático para la identidad visual del botón.
 * @property route Identificador de navegación para el grafo de Compose.
 */
data class GameOption(val title: String, val icon: ImageVector, val mainColor: Color, val route: String)

/**
 * Pantalla principal (Dashboard) de la aplicación PanGeo.
 * * Actúa como punto de entrada principal tras la autenticación. Su responsabilidad es:
 * 1. Personalizar la experiencia mediante los datos del perfil de usuario.
 * 2. Orquestar el acceso a los diferentes modos de juego y estudio.
 * 3. Mantener la consistencia del branding (Molle/Caveat) y la jerarquía visual.
 *
 * @param viewModel Fuente de datos para la información del usuario logueado.
 * @param onNavigate Acción de navegación que recibe la ruta de destino.
 */
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    // Suscripción reactiva a los datos del perfil de usuario en Firebase
    val userData by viewModel.userData
    val displayName = userData?.nickname ?: "Explorador"

    // Gestión de tipografías personalizadas con manejo de errores de carga
    val molleFamily = remember { try { FontFamily(Font(R.font.molle)) } catch (e: Exception) { FontFamily.Serif } }
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    val scrollState = rememberScrollState()

    /**
     * Definición de la matriz de juegos.
     * Centralizamos aquí las propiedades de cada botón para facilitar futuras expansiones.
     */
    val gridOptions = listOf(
        GameOption("Mapas", Icons.Default.Place, Color(0xFFA3CFBF), "maps_menu"),
        GameOption("Banderas", Icons.Outlined.Flag, Color(0xFFD55B67), "banderas_menu"),
        GameOption("Capitales", Icons.Default.LocationCity, Color(0xFFE1B44B), "capitals_menu"),
        GameOption("Cultura", Icons.Default.Public, Color(0xFF467742), "cultura")
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Fondo decorativo sutil
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.08f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- SECCIÓN DE CABECERA Y PERFIL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Branding combinado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pan", fontSize = 40.sp, fontFamily = molleFamily, color = Color.Black)
                        Text("Geo", fontSize = 50.sp, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-3).dp))
                    }
                    // Bienvenida personalizada: Uso de Caveat para un tono cercano y orgánico
                    Text(
                        text = "¡Hola, $displayName!",
                        fontSize = 26.sp,
                        fontFamily = caveatFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Accesos rápidos a Perfil y Sistema de Logros
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onNavigate("perfil") },
                        modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Ir al perfil", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { onNavigate("achievements") },
                        modifier = Modifier.size(48.dp).shadow(4.dp, CircleShape).background(Color(0xFFF39C12), CircleShape)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Ver mis logros", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- PANEL DE ACTIVIDADES (GRID) ---
            // Implementación de una rejilla 2x2 manual para un control preciso del espaciado
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[0]) { onNavigate(gridOptions[0].route) } }
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[1]) { onNavigate(gridOptions[1].route) } }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[2]) { onNavigate(gridOptions[2].route) } }
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[3]) { onNavigate(gridOptions[3].route) } }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE APRENDIZAJE PASIVO ---
            // Destaca la "Biblioteca" con un diseño de banner horizontal para diferenciarlo de los juegos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shadow(6.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF4A60B2))
                    .clickable { onNavigate("estudio") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Biblioteca de Estudio",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Componente visual para las tarjetas de juego.
 * * Implementa un diseño de "burbuja" con iconos contrastados en fondo semitraslúcido
 * para mejorar el impacto visual y la consistencia del catálogo.
 *
 * @param option Objeto de configuración con datos de la tarjeta.
 * @param onClick Acción de navegación al ser pulsada.
 */
@Composable
fun GameCard(option: GameOption, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // Mantiene una proporción casi cuadrada
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(option.mainColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Contenedor del icono con técnica de transparencia (Glassmorphism sutil)
            Box(
                modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = option.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = option.title,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}