package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pangeo.ui.game.maps.MapsRepository

/**
 * ViewModel responsable de la lógica del Atlas Interactivo (Modo Estudio).
 * * Responsabilidades:
 * 1. Proveer el dataset vectorial de países desde el [MapsRepository].
 * 2. Gestionar el estado de selección táctil (High-lighting) en el mapa.
 * 3. Implementar la lógica de alternancia (Toggle) para mostrar/ocultar información.
 */
class StudyMapViewModel : ViewModel() {

    /**
     * Fuente de datos geográficos.
     * Reutilizamos el repositorio de Europa para garantizar que los IDs de los países
     * coincidan exactamente con los usados en el modo competitivo.
     */
    val countries = MapsRepository.getEuropeCountries()

    /**
     * Estado de selección actual.
     * Almacena el ID (ISO Code) del país que el usuario ha pulsado.
     * La UI observa este cambio para resaltar el país en el Canvas y mostrar la tarjeta de detalle.
     */
    val selectedCountryId = mutableStateOf<String?>(null)

    /**
     * Gestiona el evento de clic sobre una región del mapa.
     * * Comportamiento:
     * - Si el usuario pulsa un país ya seleccionado: Se deselecciona (Limpieza de UI).
     * - Si el usuario pulsa un país nuevo: Se actualiza la selección al nuevo ID.
     * * @param id Identificador único del país pulsado.
     */
    fun onCountryClick(id: String) {
        // Implementación de Lógica de Alternancia (Toggle Logic)
        selectedCountryId.value = if (selectedCountryId.value == id) null else id
    }

} //StudyMapViewModel