package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

// Esta clase guarda los datos de cada jugador en la lista
data class RankingEntry(
    val nickname: String,
    val totalScore: Int,
    val totalTime: Int,
    val userRank: String
)

class RankingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Aquí se guardará la lista ordenada de jugadores
    val rankingList = mutableStateOf<List<RankingEntry>>(emptyList())
    val isLoading = mutableStateOf(false)

    // category puede ser: "banderas", "capitales" o "mapa"
    fun fetchGlobalRanking(category: String) {
        isLoading.value = true
        rankingList.value = emptyList() // Limpiamos la lista anterior

        val continentes = listOf("europa", "america", "asia", "africa", "oceania")

        // 1. Descargamos todos los récords de todos los jugadores
        db.collection("records").get().addOnSuccessListener { recordsDocs ->
            val tempRanking = mutableListOf<RankingEntry>()
            var pendingUsers = recordsDocs.size()

            if (pendingUsers == 0) {
                isLoading.value = false
                return@addOnSuccessListener
            }

            for (recordDoc in recordsDocs) {
                val uid = recordDoc.id
                var totalScore = 0
                var totalTime = 0

                // 2. Sumamos la puntuación y tiempo de todos los continentes para esta categoría
                for (cont in continentes) {
                    val score = recordDoc.getLong("${cont}_${category}_score")?.toInt() ?: 0
                    val time = recordDoc.getLong("${cont}_${category}_time")?.toInt() ?: 0
                    if (score > 0) {
                        totalScore += score
                        totalTime += time // Solo sumamos el tiempo si realmente ha jugado ese continente
                    }
                }

                if (totalScore > 0) {
                    // 3. Si tiene puntos, buscamos su nombre en la base de datos de usuarios
                    db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                        val nickname = userDoc.getString("nickname") ?: "Explorador"
                        val userRank = userDoc.getString("rank") ?: "Recluta"

                        tempRanking.add(RankingEntry(nickname, totalScore, totalTime, userRank))

                        pendingUsers--
                        if (pendingUsers <= 0) {
                            // 4. Cuando tenemos a todos, ordenamos: 1º Más XP, 2º Menos Tiempo
                            rankingList.value = tempRanking.sortedWith(
                                compareByDescending<RankingEntry> { it.totalScore }.thenBy { it.totalTime }
                            )
                            isLoading.value = false
                        }
                    }.addOnFailureListener { pendingUsers-- }
                } else {
                    pendingUsers--
                    if (pendingUsers <= 0) isLoading.value = false
                }
            }
        }.addOnFailureListener { isLoading.value = false }
    }
}