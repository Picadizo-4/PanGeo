package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ViewModel especializado en la gestión de marcas personales del usuario.
 * * Responsabilidades:
 * 1. Recuperar el historial completo de puntuaciones y tiempos del usuario activo.
 * 2. Mapear los datos dinámicos de Firestore a un formato estructurado para la UI.
 * 3. Gestionar los estados de carga para evitar parpadeos visuales en la pantalla de logros.
 */
class RecordsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Diccionario de récords:
     * Almacena pares clave-valor (ej: "europa_banderas_botones_score" -> 440)
     * Se utiliza un Map para permitir la escalabilidad; si añades nuevos modos de juego,
     * este ViewModel los recuperará automáticamente sin cambios en el código.
     */
    val personalRecords = mutableStateOf<Map<String, Long>>(emptyMap())
    val isLoading = mutableStateOf(false)

    /**
     * Consulta el documento de récords del usuario actual en Firestore.
     * * Lógica Técnica:
     * Accede directamente al documento identificado por el UID del usuario,
     * lo cual es la operación más eficiente y económica en términos de lectura en Firebase (O(1)).
     */
    fun fetchUserRecords() {
        val uid = auth.currentUser?.uid ?: return
        isLoading.value = true

        db.collection("records").document(uid).get()
            .addOnSuccessListener { document ->
                personalRecords.value = if (document.exists()) {
                    /**
                     * Mapeo y Casting Seguro:
                     * Firestore devuelve los números como Long por defecto. Filtramos
                     * y aseguramos que cada valor sea tratado correctamente para evitar
                     * ClassCastExceptions durante la renderización en la UI.
                     */
                    document.data?.mapValues { it.value as? Long ?: 0L } ?: emptyMap()
                } else {
                    emptyMap()
                }
                isLoading.value = false
            }
            .addOnFailureListener {
                // En caso de error de red, liberamos el estado de carga
                isLoading.value = false
            }
    }
}