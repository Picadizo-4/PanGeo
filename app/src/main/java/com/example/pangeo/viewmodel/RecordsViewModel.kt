package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecordsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Guardaremos los récords en un mapa (Ej: "europa_banderas_score" -> 440)
    val personalRecords = mutableStateOf<Map<String, Long>>(emptyMap())
    val isLoading = mutableStateOf(false)

    fun fetchUserRecords() {
        val uid = auth.currentUser?.uid ?: return
        isLoading.value = true

        db.collection("records").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.data != null) {
                    // Convertimos los datos a un mapa de String -> Long
                    val recordsMap = document.data!!.mapValues { it.value as? Long ?: 0L }
                    personalRecords.value = recordsMap
                } else {
                    personalRecords.value = emptyMap()
                }
                isLoading.value = false
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }
}