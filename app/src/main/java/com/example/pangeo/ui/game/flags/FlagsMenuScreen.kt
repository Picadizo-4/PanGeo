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
import androidx.compose.ui.draw.clip
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

@Composable
fun FlagsMenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEurope: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFDFDFD))) {
        // Fondo mapa mundi suave
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
            // Cabecera con botón volver
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Continentes",
                    fontSize = 36.sp,
                    fontFamily = caveatFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Selecciona una región para empezar tu expedición",
                fontSize = 20.sp,
                fontFamily = caveatFamily,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // --- OPCIÓN ACTIVA: EUROPA ---
            // Asegúrate de añadir una imagen llamada 'ic_europe' en drawable
            ContinentButton(
                title = "Europa",
                imageRes = R.drawable.mapaeuropa, // Cambia esto por tu nuevo icono de Europa
                fontFamily = caveatFamily,
                onClick = onNavigateToEurope
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- OPCIONES BLOQUEADAS (PRÓXIMAMENTE) ---
            Text(
                "Próximamente",
                fontSize = 22.sp,
                fontFamily = caveatFamily,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            LockedContinentButton("América", caveatFamily)
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("Asia", caveatFamily)
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("África", caveatFamily)
            Spacer(modifier = Modifier.height(16.dp))
            LockedContinentButton("Oceanía", caveatFamily)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ContinentButton(
    title: String,
    imageRes: Int,
    fontFamily: FontFamily,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A60B2))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
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
                fontSize = 38.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
fun LockedContinentButton(title: String, fontFamily: FontFamily) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .alpha(0.6f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Bloqueado",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}