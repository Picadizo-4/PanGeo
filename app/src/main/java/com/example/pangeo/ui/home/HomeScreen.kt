package com.example.pangeo.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // NUEVO
import androidx.compose.foundation.verticalScroll // NUEVO
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

data class GameOption(val title: String, val icon: ImageVector, val mainColor: Color, val route: String)

@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavigate: (String) -> Unit
) {
    val userData by viewModel.userData
    val displayName = userData?.nickname ?: "Explorador"

    val molleFamily = remember { try { FontFamily(Font(R.font.molle)) } catch (e: Exception) { FontFamily.Serif } }
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // Estado de scroll para pantallas pequeñas
    val scrollState = rememberScrollState()

    val gridOptions = listOf(
        GameOption("Mapas", Icons.Default.Place, Color(0xFFA3CFBF), "mapa"),
        GameOption("Banderas", Icons.Outlined.Flag, Color(0xFFD55B67), "banderas_menu"),
        GameOption("Capitales", Icons.Default.LocationCity, Color(0xFFE1B44B), "capitales"),
        GameOption("Cultura", Icons.Default.Public, Color(0xFF467742), "cultura")
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.10f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Permite scroll si el contenido excede la pantalla
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // CABECERA ADAPTABLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pan", fontSize = 40.sp, fontFamily = molleFamily, color = Color.Black)
                        Text("Geo", fontSize = 50.sp, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-3).dp))
                    }
                    Text(
                        "¡Hola, $displayName!",
                        fontSize = 24.sp,
                        fontFamily = caveatFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onNavigate("perfil") },
                        modifier = Modifier.size(50.dp).shadow(6.dp, CircleShape).background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { onNavigate("achievements") },
                        modifier = Modifier.size(50.dp).shadow(6.dp, CircleShape).background(Color(0xFFF39C12), CircleShape)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Logros", tint = Color.White)
                    }
                }
            }

            // Espacio flexible en lugar de 100dp fijos
            Spacer(modifier = Modifier.height(40.dp))

            // GRID DE JUEGOS (Usando Rows y Weights para máxima adaptabilidad)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[0], caveatFamily) { onNavigate(gridOptions[0].route) } }
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[1], caveatFamily) { onNavigate(gridOptions[1].route) } }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[2], caveatFamily) { onNavigate(gridOptions[2].route) } }
                    Box(modifier = Modifier.weight(1f)) { GameCard(gridOptions[3], caveatFamily) { onNavigate(gridOptions[3].route) } }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN ESTUDIO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF4A60B2))
                    .clickable { onNavigate("estudio") },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Estudio", fontSize = 32.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GameCard(option: GameOption, fontFamily: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f) // Esto hace que la tarjeta sea casi cuadrada según el ancho del móvil
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(option.mainColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = option.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = option.title, fontSize = 28.sp, fontFamily = fontFamily, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}