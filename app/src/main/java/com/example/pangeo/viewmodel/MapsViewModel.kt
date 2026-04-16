package com.example.pangeo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pangeo.model.CountryPath
import com.example.pangeo.ui.game.maps.MapsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * ViewModel de alta fidelidad para la gestión del Mapa Interactivo.
 * * Responsabilidades:
 * 1. Orquestar el motor geográfico mediante flujos reactivos ([StateFlow]).
 * 2. Gestionar la máquina de estados de cada país (Correcto, Parcial, Error, Resaltado).
 * 3. Implementar lógica de intentos y retroalimentación visual (Blinking).
 * 4. Controlar el cronómetro de precisión para el ranking de expedición.
 */
class MapsViewModel : ViewModel() {

    // Recuperación de datos vectoriales del repositorio
    val europeCountries = MapsRepository.getEuropeCountries()

    // --- ESTADOS REACTIVOS (BACKING PROPERTIES) ---
    // Se utiliza StateFlow para una propagación de estado más robusta y observable.

    private val _targetCountry = MutableStateFlow<CountryPath?>(null)
    val targetCountry: StateFlow<CountryPath?> = _targetCountry.asStateFlow()

    // Diccionario de estados de países: Vincula el ID del país con su estatus visual en el Canvas
    private val _countryStates = MutableStateFlow<Map<String, String>>(emptyMap())
    val countryStates: StateFlow<Map<String, String>> = _countryStates.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _attempts = MutableStateFlow(0)
    val attempts: StateFlow<Int> = _attempts.asStateFlow()

    // Estado para gestionar el parpadeo visual de un país específico tras un error
    private val _blinkingCountry = MutableStateFlow<String?>(null)
    val blinkingCountry: StateFlow<String?> = _blinkingCountry.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0)
    val elapsedTime: StateFlow<Int> = _elapsedTime.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // --- PROPIEDADES DE CONTROL INTERNO ---
    val totalCountries = europeCountries.size
    private var remainingCountries = mutableListOf<CountryPath>()
    private var timerJob: Job? = null

    // Modo operativo del juego definido por la UI al inicio
    var gameMode = "botones"
        private set

    /**
     * Reinicializa la sesión de juego.
     * * Limpia el historial de estados y baraja el dataset de países para
     * evitar el aprendizaje por orden de aparición (Bias preventivo).
     */
    fun resetGame(mode: String = "botones") {
        gameMode = mode
        remainingCountries = europeCountries.shuffled().toMutableList()
        _countryStates.value = europeCountries.associate { it.id to "DEFAULT" }
        _score.value = 0
        _isGameOver.value = false
        _attempts.value = 0
        _blinkingCountry.value = null
        _elapsedTime.value = 0
        _currentIndex.value = 0

        startTimer()
        nextTarget()
    }

    /**
     * Selecciona el siguiente país objetivo del pool restante.
     * En modo escritura, aplica un resaltado visual (HIGHLIGHTED) para guiar al usuario.
     */
    private fun nextTarget() {
        if (remainingCountries.isNotEmpty()) {
            val next = remainingCountries.removeAt(0)
            _targetCountry.value = next
            _attempts.value = 0
            _currentIndex.value = totalCountries - remainingCountries.size - 1

            if (gameMode == "texto") {
                _countryStates.update { it + (next.id to "HIGHLIGHTED") }
            }
        } else {
            stopTimer()
            _isGameOver.value = true
        }
    }

    /**
     * Limpieza y normalización de entradas de texto.
     * Garantiza la equidad en la validación omitiendo diferencias de formato.
     */
    private fun String.limpiarTexto(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "").lowercase().trim()
    }

    // --- LÓGICA DE INTERACCIÓN ---

    /**
     * Procesa la selección táctil de un país en el mapa.
     * * Reglas de puntuación:
     * 1. Acierto directo (0 fallos): 10 XP + Estado Verde.
     * 2. Acierto tras un fallo: 5 XP + Estado Naranja (Parcial).
     * 3. Segundo fallo: Salto automático + Estado Rojo.
     */
    fun onCountrySelected(countryId: String) {
        if (gameMode != "botones") return

        val target = _targetCountry.value ?: return

        // Validación de idempotencia: Ignorar si el país ya fue resuelto
        val currentState = _countryStates.value[countryId]
        if (currentState == "CORRECT" || currentState == "CORRECT_PARTIAL" || currentState == "WRONG") return

        if (countryId == target.id) {
            if (_attempts.value == 0) {
                _countryStates.update { it + (target.id to "CORRECT") }
                _score.value += 10
            } else {
                _countryStates.update { it + (target.id to "CORRECT_PARTIAL") }
                _score.value += 5
            }
            nextTarget()
        } else {
            // Gestión de fallos
            _attempts.value += 1
            if (_attempts.value >= 2) {
                _countryStates.update { it + (target.id to "WRONG") }
                nextTarget()
            } else {
                // Feedback visual dinámico (Blinking) para el error cometido
                viewModelScope.launch {
                    _blinkingCountry.value = countryId
                    delay(600)
                    _blinkingCountry.value = null
                }
            }
        }
    }

    /**
     * Validación en tiempo real para el modo de escritura.
     */
    fun checkTextAnswer(input: String) {
        if (gameMode != "texto") return
        val target = _targetCountry.value ?: return

        if (input.limpiarTexto() == target.name.limpiarTexto()) {
            _countryStates.update { it + (target.id to "CORRECT") }
            _score.value += 10

            viewModelScope.launch {
                delay(700)
                nextTarget()
            }
        }
    }

    /**
     * Permite al usuario avanzar si desconoce la ubicación, penalizando la puntuación.
     */
    fun skipCountry() {
        if (gameMode != "texto") return
        val target = _targetCountry.value ?: return

        _countryStates.update { it + (target.id to "WRONG") }

        viewModelScope.launch {
            delay(400)
            nextTarget()
        }
    }

    // --- SUBSISTEMA DE TIEMPO ---

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedTime.value += 1
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    /**
     * Asegura el cierre de hilos y corrutinas al destruir el ViewModel
     * para optimizar el consumo de batería y memoria.
     */
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}