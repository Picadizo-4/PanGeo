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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val userData by viewModel.userData

    // --- ESTADO DEL SCROLL ---
    val scrollState = rememberScrollState()

    if (userData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4A60B2))
        }
        return
    }

    val userNickname = userData!!.nickname
    val userEmail = userData!!.email
    val userXP = userData!!.xp
    val userLevel = userData!!.level
    val userRank = userData!!.rank

    // --- NUEVA LÓGICA DE BARRA DE PROGRESO EXPONENCIAL ---
    // XP base del nivel actual (ej: Nivel 2 = 100 XP)
    val currentLevelBaseXP = 100 * ((userLevel - 1) * (userLevel - 1))

    // XP requerida para alcanzar el SIGUIENTE nivel (ej: Nivel 3 = 400 XP)
    val nextLevelXP = 100 * (userLevel * userLevel)

    // XP conseguida DENTRO de este nivel (ej: Tengo 250 XP -> Llevo 150 dentro del Nivel 2)
    val xpInCurrentLevel = userXP - currentLevelBaseXP

    // XP total que separa el nivel actual del siguiente (ej: de 100 a 400 = 300 XP)
    val xpNeededForNext = nextLevelXP - currentLevelBaseXP

    // Porcentaje de la barra de carga (de 0.0f a 1.0f)
    val progressFlow = if (xpNeededForNext > 0) {
        xpInCurrentLevel.toFloat() / xpNeededForNext.toFloat()
    } else {
        1f // Si llega al nivel 100, la barra se queda llena
    }

    val initialLetter = if (userNickname.isNotEmpty()) userNickname.first().uppercase().toString() else "E"

    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.04f)
        )

        // --- COLUMNA ADAPTABLE CON SCROLL ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Vital para pantallas pequeñas
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // --- AVATAR DINÁMICO (Ligeramente más pequeño) ---
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF4A60B2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialLetter,
                            fontSize = 65.sp,
                            fontFamily = caveatFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = (-4).dp, x = (-4).dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF39C12),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .shadow(4.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userLevel.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // NICKNAME ADAPTABLE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text(
                    text = userNickname,
                    fontSize = 42.sp, // Bajado un poco de 48 para pantallas estrechas
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false) // Permite que el texto se encoja
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = userEmail,
                fontSize = 24.sp,
                fontFamily = caveatFamily,
                color = Color.Gray
            )

            // --- ÁREA DE PROGRESO (Gamificación) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 25.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Rango: $userRank",
                        fontSize = 20.sp,
                        fontFamily = caveatFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF467742)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE1B44B), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nivel $userLevel",
                            fontSize = 22.sp,
                            fontFamily = caveatFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFlow.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE1B44B), Color(0xFFF39C12))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${userXP}XP / ${nextLevelXP}XP",
                    fontSize = 18.sp,
                    fontFamily = caveatFamily,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- BOTONES INFERIORES ADAPTABLES ---
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AD07B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("VOLVER", fontSize = 28.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onLogout) {
                Text("Cerrar Sesión", color = Color.Black, fontFamily = caveatFamily, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(40.dp)) // Espacio final para el scroll
        }

        // --- DIÁLOGO DE EDICIÓN (ADAPTABLE) ---
        // --- DIÁLOGO DE EDICIÓN ---
        if (showEditDialog) {
            var tempNickname by remember { mutableStateOf(userNickname) }
            var errorMessage by remember { mutableStateOf("") } // Para avisar si el nombre está repetido

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Cambiar Nickname", fontFamily = caveatFamily, fontSize = 26.sp) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempNickname,
                            onValueChange = {
                                if (it.length <= 15) {
                                    tempNickname = it
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Nuevo apodo", fontFamily = caveatFamily) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = errorMessage.isNotEmpty()
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = caveatFamily,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateNickname(tempNickname) { success, message ->
                            if (success) {
                                showEditDialog = false // Cerramos si todo fue bien
                            } else {
                                errorMessage = message // Mostramos el error (ej: "Nickname en uso")
                            }
                        }
                    }) {
                        Text("Guardar", fontWeight = FontWeight.Bold, fontFamily = caveatFamily, fontSize = 18.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancelar", fontFamily = caveatFamily, fontSize = 18.sp)
                    }
                }
            )
        }
    }
}