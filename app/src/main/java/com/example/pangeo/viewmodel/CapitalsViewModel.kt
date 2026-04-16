package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pangeo.ui.game.capitals.CapitalQuestion
import com.example.pangeo.ui.game.capitals.CapitalsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * ViewModel encargado de la lógica del juego de Capitales.
 * * Responsabilidades:
 * 1. Gestión del ciclo de vida de la partida (Inicio, Progreso, Fin).
 * 2. Control del temporizador mediante Corrutinas.
 * 3. Validación de respuestas en dos modalidades (Selección y Escritura).
 * 4. Normalización de texto para mejorar la tolerancia a errores de escritura.
 */
class CapitalsViewModel : ViewModel() {

    // Fuente de datos de geografía
    private val repository = CapitalsRepository()
    private var questionList = emptyList<CapitalQuestion>()

    // --- ESTADOS REACTIVOS (UI STATE) ---
    val currentQuestionIndex = mutableStateOf(0)
    val score = mutableStateOf(0)
    val isGameOver = mutableStateOf(false)
    val totalQuestions = mutableStateOf(0)

    /**
     * Pregunta actual visible en la UI.
     * Se mantiene como propiedad privada con acceso público para garantizar
     * que solo el ViewModel pueda modificar la lógica de progresión.
     */
    var currentQuestion: CapitalQuestion? = null
        private set

    val selectedAnswer = mutableStateOf<String?>(null)
    val textFeedback = mutableStateOf<Boolean?>(null)

    // Cronómetro de la expedición
    val elapsedTime = mutableStateOf(0)
    private var timerJob: Job? = null

    /**
     * Motor de Normalización:
     * Elimina acentos (diacríticos), convierte a minúsculas y limpia espacios.
     * Esto permite que "París", "paris" y "PARIS " sean aceptados como correctos.
     */
    private fun String.limpiarTexto(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "").lowercase().trim()
    }

    // --- CICLO DE VIDA DEL JUEGO ---

    /**
     * Inicializa una nueva partida.
     * * Baraja las preguntas y sus opciones internas para garantizar
     * que cada expedición sea única (Randomization).
     */
    fun resetGame() {
        val allQuestions = repository.getEuropeCapitals()

        // Aplicamos barajado doble: de la lista y de las opciones de cada pregunta
        questionList = allQuestions.shuffled().map { it.copy(options = it.options.shuffled()) }
        totalQuestions.value = questionList.size
        currentQuestionIndex.value = 0
        score.value = 0
        isGameOver.value = false
        selectedAnswer.value = null
        textFeedback.value = null
        currentQuestion = questionList.firstOrNull()

        elapsedTime.value = 0
        startTimer()
    }

    /**
     * Validación para el Modo Selección.
     * Utiliza un pequeño delay para permitir que el usuario vea el feedback visual (verde/rojo)
     * antes de saltar automáticamente a la siguiente pregunta.
     */
    fun checkAnswerSelectionMode(answer: String) {
        if (selectedAnswer.value != null) return // Evita múltiples clics en la misma pregunta

        selectedAnswer.value = answer
        val isCorrect = answer == currentQuestion?.correctAnswer

        if (isCorrect) {
            score.value += 10
        }

        viewModelScope.launch {
            delay(400) // Feedback visual UX
            moveToNextQuestion()
        }
    }

    /**
     * Validación para el Modo Escritura (Manual).
     * Se dispara cuando el usuario confirma su respuesta o decide saltar.
     */
    fun checkAnswerTextMode(userInput: String) {
        if (textFeedback.value != null) return

        val correctAnswer = currentQuestion?.correctAnswer ?: ""
        val isCorrect = userInput.limpiarTexto() == correctAnswer.limpiarTexto()

        textFeedback.value = isCorrect
        if (isCorrect) score.value += 10

        viewModelScope.launch {
            delay(300)
            moveToNextQuestion()
        }
    }

    /**
     * Validación "al vuelo" (Auto-check).
     * Permite avanzar automáticamente si el usuario escribe la respuesta correcta
     * sin necesidad de pulsar un botón de confirmación.
     */
    fun checkAutoAnswerTextMode(userInput: String) {
        if (textFeedback.value != null) return

        val correctAnswer = currentQuestion?.correctAnswer ?: ""

        if (userInput.limpiarTexto() == correctAnswer.limpiarTexto() && correctAnswer.isNotBlank()) {
            textFeedback.value = true
            score.value += 10

            viewModelScope.launch {
                delay(700) // Delay ligeramente mayor para que el usuario note su acierto
                moveToNextQuestion()
            }
        }
    }

    /**
     * Lógica de transición entre preguntas.
     * Detecta si se ha alcanzado el límite de la lista para finalizar la partida.
     */
    private fun moveToNextQuestion() {
        selectedAnswer.value = null
        textFeedback.value = null

        val nextIndex = currentQuestionIndex.value + 1
        if (nextIndex < questionList.size) {
            currentQuestionIndex.value = nextIndex
            currentQuestion = questionList[nextIndex]
        } else {
            isGameOver.value = true
            stopTimer()
        }
    }

    // --- GESTIÓN DEL TIEMPO (COROUTINES) ---

    /**
     * Inicia el cronómetro en un hilo secundario utilizando el scope del ViewModel.
     * Se detiene automáticamente si el ViewModel se destruye o si el juego termina.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                elapsedTime.value += 1
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }
}