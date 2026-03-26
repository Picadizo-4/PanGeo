package com.example.pangeo.ui.auth // Asegúrate de que el package coincida con tu estructura

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    viewModel: AuthViewModel, // --- AÑADIMOS EL VIEWMODEL ---
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // --- CONEXIÓN CON DATOS REALES DE FIRESTORE ---
    val userData by viewModel.userData

    // Si los datos no han cargado, mostramos un indicador de progreso
    if (userData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4A60B2))
        }
        return
    }

    // Extraemos las variables del objeto User de Firestore
    val userNickname = userData!!.nickname
    val userEmail = userData!!.email
    val userXP = userData!!.xp
    val userLevel = userData!!.level
    val userRank = userData!!.rank

    // Lógica para el siguiente nivel (puedes ajustarla según tu sistema de progresión)
    val nextLevelXP = userLevel * 5000
    val progressFlow = userXP.toFloat() / nextLevelXP.toFloat()

    // Obtener la inicial dinámicamente
    val initialLetter = if (userNickname.isNotEmpty()) userNickname.first().uppercase().toString() else "E"

    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.04f)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- AVATAR DINÁMICO ---
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(12.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF4A60B2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialLetter, // --- INICIAL REAL ---
                            fontSize = 75.sp,
                            fontFamily = caveatFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = (-4).dp, x = (-6).dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF39C12),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-3).dp, y = (-3).dp)
                        .shadow(6.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userLevel.toString(), // --- NIVEL REAL ---
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NICKNAME Y EMAIL REALES
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    text = userNickname,
                    fontSize = 48.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Text(
                text = userEmail,
                fontSize = 28.sp,
                fontFamily = caveatFamily,
                color = Color.Gray
            )

            // --- GAMIFICACIÓN REAL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 35.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Rango: $userRank",
                        fontSize = 22.sp,
                        fontFamily = caveatFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF467742)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE1B44B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nivel $userLevel",
                            fontSize = 26.sp,
                            fontFamily = caveatFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                // BARRA DE XP REAL
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
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
                    text = "${userXP}XP / ${nextLevelXP}XP para el siguiente nivel",
                    fontSize = 18.sp,
                    fontFamily = caveatFamily,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateBack, // Cambiado a volver atrás
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AD07B)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("VOLVER", fontSize = 30.sp, fontFamily = caveatFamily, fontWeight = FontWeight.ExtraBold)
            }

            // Botón pequeño para cerrar sesión opcional
            TextButton(onClick = onLogout) {
                Text("Cerrar Sesión", color = Color.Red, fontFamily = caveatFamily, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Diálogo de edición (Aquí podrías añadir la lógica para actualizar Firestore)
        if (showEditDialog) {
            var tempNickname by remember { mutableStateOf(userNickname) }
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Nuevo Nickname", fontFamily = caveatFamily, fontSize = 28.sp) },
                text = {
                    OutlinedTextField(
                        value = tempNickname,
                        onValueChange = { if (it.length <= 15) tempNickname = it },
                        label = { Text("Apodo") },
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        // Aquí deberías llamar a una función del viewModel para actualizar Firestore
                        showEditDialog = false
                    }) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}