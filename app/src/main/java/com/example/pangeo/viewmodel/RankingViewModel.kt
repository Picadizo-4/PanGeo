package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Modelo de datos para representar a un usuario en la tabla de clasificación.
 * * @property nickname Apodo público del explorador.
 * @property totalScore Sumatoria de puntos obtenidos en la categoría seleccionada (todos los continentes).
 * @property totalTime Sumatoria del tiempo empleado en completar las expediciones.
 * @property userRank Título honorífico según el nivel del usuario.
 */
data class RankingEntry(
    val nickname: String,
    val totalScore: Int,
    val totalTime: Int,
    val userRank: String
)

/**
 * ViewModel encargado de la gestión del Ranking Global.
 * * Responsabilidades:
 * 1. Consultar de forma masiva los récords de todos los usuarios.
 * 2. Agregar puntuaciones multidimensionales (Continente x Categoría).
 * 3. Resolver la identidad de los usuarios vinculados a cada récord.
 * 4. Ordenar los resultados bajo criterios de mérito (Puntos DESC, Tiempo ASC).
 */
class RankingViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Estados de UI
    val rankingList = mutableStateOf<List<RankingEntry>>(emptyList())
    val isLoading = mutableStateOf(false)

    /**
     * Recupera y procesa el ranking global para una modalidad específica.
     * * El algoritmo realiza las siguientes operaciones:
     * 1. Recupera la colección completa de 'records'.
     * 2. Itera por cada usuario sumando sus marcas en los 5 continentes.
     * 3. Realiza una petición secundaria a 'users' para obtener metadatos de perfil.
     * 4. Utiliza un semáforo de control ([pendingUsers]) para finalizar el proceso asíncrono.
     * * @param category Identificador de la modalidad (ej. "banderas_botones").
     */
    fun fetchGlobalRanking(category: String) {
        isLoading.value = true
        rankingList.value = emptyList()

        db.collection("records").get().addOnSuccessListener { recordsDocs ->
            val tempRanking = mutableListOf<RankingEntry>()
            val continentes = listOf("europa", "america", "asia", "africa", "oceania")

            // Semáforo para controlar la concurrencia de peticiones de red
            var pendingUsers = recordsDocs.size()

            if (pendingUsers == 0) {
                isLoading.value = false
                return@addOnSuccessListener
            }

            for (recordDoc in recordsDocs) {
                val uid = recordDoc.id
                var totalScore = 0
                var totalTime = 0

                // Acumulación de métricas por región geográfica
                for (cont in continentes) {
                    totalScore += recordDoc.getLong("${cont}_${category}_score")?.toInt() ?: 0
                    totalTime += recordDoc.getLong("${cont}_${category}_time")?.toInt() ?: 0
                }

                // Solo procesamos usuarios que tengan actividad registrada en esta categoría
                if (totalScore > 0) {
                    db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            tempRanking.add(RankingEntry(
                                nickname = userDoc.getString("nickname") ?: "Explorador",
                                totalScore = totalScore,
                                totalTime = totalTime,
                                userRank = userDoc.getString("rank") ?: "Recluta"
                            ))
                        }
                        // Decremento del semáforo tras éxito
                        pendingUsers--
                        if (pendingUsers <= 0) finalize(tempRanking)
                    }.addOnFailureListener {
                        // Continuamos el flujo incluso si falla una petición individual
                        pendingUsers--
                        if (pendingUsers <= 0) finalize(tempRanking)
                    }
                } else {
                    // Si el usuario no tiene puntos, descartamos y restamos del contador pendiente
                    pendingUsers--
                    if (pendingUsers <= 0) finalize(tempRanking)
                }
            }
        }
    }

    /**
     * Aplica la lógica de ordenamiento final y actualiza el estado de la UI.
     * * Criterios de desempate:
     * 1. Prioridad: Puntuación total descendente.
     * 2. Secundaria: Tiempo total ascendente (premio a la velocidad).
     */
    private fun finalize(list: List<RankingEntry>) {
        rankingList.value = list.sortedWith(
            compareByDescending<RankingEntry> { it.totalScore }.thenBy { it.totalTime }
        )
        isLoading.value = false
    }
}