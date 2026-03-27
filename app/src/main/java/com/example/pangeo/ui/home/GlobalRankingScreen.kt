package com.example.pangeo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pangeo.R
import com.example.pangeo.viewmodel.RankingViewModel

@Composable
fun GlobalRankingScreen(
    viewModel: RankingViewModel,
    onNavigateBack: () -> Unit
) {
    val caveatFamily = remember { try { FontFamily(Font(R.font.caveat)) } catch (e: Exception) { FontFamily.Serif } }

    // Controla qué pestaña está activa (0=Banderas, 1=Capitales, 2=Mapas)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Banderas", "Capitales", "Mapas")
    val internalCategories = listOf("banderas", "capitales", "mapa")

    val ranking by viewModel.rankingList
    val isLoading by viewModel.isLoading

    // Al cambiar de pestaña, pedimos los datos de esa categoría al ViewModel
    LaunchedEffect(selectedTabIndex) {
        viewModel.fetchGlobalRanking(internalCategories[selectedTabIndex])
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF6F7FB)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("Ranking Global", fontSize = 28.sp, fontFamily = caveatFamily, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PESTAÑAS ---
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF4A60B2)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = caveatFamily,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 20.sp,
                            color = if (selectedTabIndex == index) Color(0xFF4A60B2) else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LISTA DE RANKING ---
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4A60B2))
            }
        } else if (ranking.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no hay exploradores en esta categoría.", fontFamily = caveatFamily, fontSize = 20.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(ranking) { index, entry ->
                    RankingUserCard(position = index + 1, entry = entry, caveatFamily)
                }
            }
        }
    }
}

@Composable
fun RankingUserCard(position: Int, entry: com.example.pangeo.viewmodel.RankingEntry, fontFamily: FontFamily) {
    val medalColor = when(position) {
        1 -> Color(0xFFFFD700) // Oro
        2 -> Color(0xFFC0C0C0) // Plata
        3 -> Color(0xFFCD7F32) // Bronce
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(if (position <= 3) medalColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("#$position", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (position <= 3) medalColor else Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.nickname, fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
                Text(entry.userRank, fontFamily = fontFamily, fontSize = 16.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.totalScore} XP", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4CAF50))
                Text("${entry.totalTime} seg", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}