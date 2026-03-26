package com.example.pangeo.model

// Importante: Los nombres de las variables deben coincidir con los que usemos en Firestore
data class User(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "Nuevo Explorador",
    val xp: Int = 0,
    val level: Int = 1,
    val rank: String = "Recluta de Pangeo"
)