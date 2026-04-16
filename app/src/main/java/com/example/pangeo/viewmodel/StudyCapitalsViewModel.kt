package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pangeo.ui.game.maps.MapsRepository
import java.text.Normalizer

/**
 * ViewModel responsable de la lógica del buscador en la Guía de Capitales.
 * * Responsabilidades:
 * 1. Proveer la fuente de datos completa de países europeos.
 * 2. Gestionar el estado de filtrado reactivo basado en la consulta del usuario.
 * 3. Implementar algoritmos de normalización de texto para búsquedas "fuzzy" (tolerantes a acentos).
 */
class StudyCapitalsViewModel : ViewModel() {

    // Dataset inmutable de referencia
    val allCountries = MapsRepository.getEuropeCountries()

    // --- ESTADOS REACTIVOS ---
    // filteredCountries: Lista que la UI observa para renderizar las tarjetas.
    var filteredCountries = mutableStateOf(allCountries)
    // searchQuery: El texto actual del campo de búsqueda.
    var searchQuery = mutableStateOf("")

    /**
     * Actualiza el filtro de la lista según la entrada del usuario.
     * * Lógica de filtrado:
     * El algoritmo compara el nombre del país con la consulta de dos formas:
     * - Coincidencia directa (sensible a caracteres especiales).
     * - Coincidencia normalizada (omitiendo tildes y diacríticos).
     * * @param query Texto introducido por el usuario.
     */
    fun onSearchChange(query: String) {
        searchQuery.value = query

        val normalizedQuery = query.lowercase().trim()

        filteredCountries.value = if (normalizedQuery.isEmpty()) {
            allCountries
        } else {
            allCountries.filter { country ->
                val normalizedCountryName = country.name.lowercase()

                // Estrategia de búsqueda dual:
                // 1. Contiene la cadena tal cual (ej. "España")
                normalizedCountryName.contains(normalizedQuery) ||
                        // 2. Contiene la cadena normalizada (ej. "Espana" coincide con "España")
                        removeAccents(normalizedCountryName).contains(removeAccents(normalizedQuery))
            }
        }
    }

    /**
     * Función de normalización Unicode.
     * Separa los caracteres de sus tildes y marcas diacríticas para eliminarlos,
     * facilitando comparaciones de texto puras.
     * * Ejemplo: "Árbol" -> "Arbol"
     */
    private fun removeAccents(text: String): String {
        // Normalización NFD: separa la 'A' de su tilde '´'
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        // Regex para eliminar las marcas diacríticas separadas
        return temp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}