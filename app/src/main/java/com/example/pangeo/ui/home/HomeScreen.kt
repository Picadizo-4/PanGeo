package com.example.pangeo.ui.home

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel // IMPORTANTE

data class GameOption(val title: String, val icon: ImageVector, val mainColor: Color, val route: String)

@Composable
fun HomeScreen(
    viewModel: AuthViewModel, // --- AÑADIMOS EL VIEWMODEL ---
    onNavigate: (String) -> Unit
) {
    // --- OBTENER DATOS REALES ---
    val userData by viewModel.userData
    // Si el nombre no ha cargado, ponemos "Explorador" por defecto
    val displayName = userData?.nickname ?: "Explorador"

    val molleFamily = remember { try { FontFamily(Font(R.font.molle)) } catch (e: Exception) { FontFamily.Serif } }
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    val gridOptions = listOf(
        GameOption("Mapas", Icons.Default.Place, Color(0xFFA3CFBF), "mapa"),
        GameOption("Banderas", Icons.Outlined.Flag, Color(0xFFD55B67), "banderas"),
        GameOption("Capitales", Icons.Default.LocationCity, Color(0xFFE1B44B), "capitales"),
        GameOption("Cultura", Icons.Default.Public, Color(0xFF467742), "cultura")
    )

    val estudioOption = GameOption("Estudio", Icons.Default.MenuBook, Color(0xFF4A60B2), "estudio")

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.10f)
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pan", fontSize = 45.sp, fontFamily = molleFamily, color = Color.Black)
                        Text("Geo", fontSize = 55.sp, fontFamily = caveatFamily, color = Color.Black, modifier = Modifier.offset(y = (-3).dp))
                    }
                    // --- AQUÍ USAMOS EL NOMBRE REAL ---
                    Text(
                        "¡Hola, $displayName!",
                        fontSize = 28.sp,
                        fontFamily = caveatFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.offset(y = (-5).dp).padding(top = 20.dp)
                ) {
                    IconButton(
                        onClick = { onNavigate("perfil") },
                        modifier = Modifier.size(60.dp).shadow(10.dp, CircleShape).background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { onNavigate("logros") },
                        modifier = Modifier.size(60.dp).shadow(10.dp, CircleShape).background(Color(0xFFF39C12), CircleShape)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Logros", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
            ) {
                items(gridOptions) { option ->
                    GameCard(option, caveatFamily) { onNavigate(option.route) }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(90.dp).shadow(10.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp))
                    .background(estudioOption.mainColor).clickable { onNavigate(estudioOption.route) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(imageVector = estudioOption.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = estudioOption.title, fontSize = 38.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GameCard(option: GameOption, fontFamily: FontFamily, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(150.dp).shadow(10.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp))
            .background(option.mainColor).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = option.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = option.title, fontSize = 35.sp, fontFamily = fontFamily, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}