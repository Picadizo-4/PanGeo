package com.example.pangeo.model

import androidx.compose.ui.graphics.Path

data class CountryPath(
    val id: String,    // Código del país (ej: "ES")
    val name: String,  // Nombre (ej: "España")
    val path: Path     // El dibujo vectorial
)