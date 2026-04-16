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

/**
 * Pantalla de Perfil del Usuario.
 * * Gestiona la visualización del progreso del jugador, incluyendo:
 * 1. Sistema de Niveles: Lógica parabólica de XP para calcular el progreso al siguiente rango.
 * 2. Identidad: Gestión del avatar generado y edición del apodo (Nickname).
 * 3. Sesión: Control de salida segura del ecosistema PanGeo.
 * * @param viewModel Fuente de verdad para los datos del usuario y lógica de actualización.
 * @param onNavigateBack Acción para regresar al tablero principal.
 * @param onLogout Acción para invalidar la sesión actual y retornar al Login.
 */
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val userData by viewModel.userData
    val scrollState = rememberScrollState()

    /**
     * Gestión de Carga:
     * Si los datos desde Firebase aún no han sido resueltos, se presenta un
     * indicador de carga para evitar errores de puntero nulo en la interfaz.
     */
    if (userData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4A60B2))
        }
        return
    }

    // Extracción segura de propiedades tras validación de nulidad
    val userNickname = userData!!.nickname
    val userEmail = userData!!.email
    val userXP = userData!!.xp
    val userLevel = userData!!.level
    val userRank = userData!!.rank

    /**
     * Lógica de Progresión (RPG Mechanics):
     * Calcula el porcentaje de avance dentro del nivel actual basándose en una
     * curva de dificultad incremental.
     */
    val currentLevelBaseXP = 100 * ((userLevel - 1) * (userLevel - 1))
    val nextLevelXP = 100 * (userLevel * userLevel)
    val xpInCurrentLevel = userXP - currentLevelBaseXP
    val xpNeededForNext = nextLevelXP - currentLevelBaseXP
    val progressFlow = if (xpNeededForNext > 0) xpInCurrentLevel.toFloat() / xpNeededForNext.toFloat() else 1f

    val initialLetter = if (userNickname.isNotEmpty()) userNickname.first().uppercase().toString() else "E"
    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Marca de agua de fondo
        Image(
            painter = painterResource(id = R.drawable.mapamundo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.04f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- SECCIÓN DE AVATAR Y NIVEL ---
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(120.dp).shadow(8.dp, CircleShape).background(Color.White, CircleShape).padding(4.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF4A60B2), CircleShape), contentAlignment = Alignment.Center) {
                        Text(
                            text = initialLetter,
                            fontSize = 60.sp,
                            fontFamily = caveatFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                // Badge de nivel superpuesto
                Surface(
                    color = Color(0xFFF39C12),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp).align(Alignment.BottomEnd).shadow(4.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = userLevel.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.SansSerif)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- IDENTIDAD DEL EXPLORADOR ---
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = userNickname,
                    fontSize = 36.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, "Editar apodo", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Text(text = userEmail, fontSize = 16.sp, fontFamily = FontFamily.SansSerif, color = Color.Gray)

            // --- PANEL DE PROGRESO Y RANGO ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rango: $userRank", fontSize = 16.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = Color(0xFF467742))
                    Text("Nivel $userLevel", fontSize = 16.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso personalizada con degradado
                Box(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEEEEE))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFlow.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFE1B44B), Color(0xFFF39C12))))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${userXP} / ${nextLevelXP} XP",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ACCIONES GLOBALES ---
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6AD07B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("VOLVER AL MENÚ", fontSize = 18.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLogout) {
                Text("Cerrar Sesión", color = Color.Red, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Diálogo de edición de Nickname con validación reactiva
        if (showEditDialog) {
            var tempNickname by remember { mutableStateOf(userNickname) }
            var errorMessage by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Apodo", fontFamily = caveatFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempNickname,
                            onValueChange = { if (it.length <= 15) { tempNickname = it; errorMessage = "" } },
                            label = { Text("Nuevo apodo") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Default)
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateNickname(tempNickname) { success, message ->
                            if (success) showEditDialog = false else errorMessage = message
                        }
                    }) { Text("GUARDAR", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("CANCELAR") }
                }
            )
        }
    }
}