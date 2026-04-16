package com.example.pangeo.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pangeo.ui.game.flags.FlagQuestion
import com.example.pangeo.ui.game.flags.FlagRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.Normalizer

/**
 * ViewModel encargado de la lógica del juego de Banderas.
 * * Responsabilidades:
 * 1. Gestión de la sesión de juego (Estado de carga, puntuación y progreso).
 * 2. Control de flujos asíncronos para el temporizador y el feedback de usuario.
 * 3. Normalización de entradas de texto para permitir una validación flexible (sin acentos/mayúsculas).
 */
class FlagsViewModel : ViewModel() {

    private var questionList = emptyList<FlagQuestion>()

    // --- ESTADOS DE LA UI (ESTADO REACTIVO) ---
    val currentQuestionIndex = mutableStateOf(0)
    val score = mutableStateOf(0)
    val isGameOver = mutableStateOf(false)
    val totalQuestions = mutableStateOf(0)

    /**
     * Pregunta actual (Bandera y opciones).
     * Se expone como lectura para la UI pero su mutación está restringida al ViewModel.
     */
    var currentQuestion: FlagQuestion? = null
        private set

    val selectedAnswer = mutableStateOf<String?>(null)
    val textFeedback = mutableStateOf<Boolean?>(null)

    // Gestión del tiempo de juego
    val elapsedTime = mutableStateOf(0)
    private var timerJob: Job? = null

    /**
     * Motor de Normalización de Texto:
     * Transforma la entrada del usuario para compararla con la respuesta correcta
     * omitiendo tildes, mayúsculas y espacios innecesarios.
     */
    private fun String.limpiarTexto(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{Mn}+".toRegex(), "").lowercase().trim()
    }

    // --- LÓGICA DE NEGOCIO ---

    /**
     * Reinicia el estado del juego y carga una nueva lista de preguntas.
     * * Implementa una doble aleatoriedad: baraja el orden de las banderas
     * y el orden de las opciones dentro de cada bandera.
     */
    fun resetGame() {
        val allQuestions = FlagRepository.getEuropeQuestions()

        // Barajamos y creamos copias inmutables con opciones mezcladas
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
     * Bloquea múltiples entradas y dispara la transición automática tras un breve delay.
     */
    fun checkAnswerSelectionMode(answer: String) {
        if (selectedAnswer.value != null) return

        selectedAnswer.value = answer
        val isCorrect = answer == currentQuestion?.correctAnswer

        if (isCorrect) {
            score.value += 10
        }

        viewModelScope.launch {
            delay(400) // Delay pedagógico para mostrar acierto/fallo
            moveToNextQuestion()
        }
    }

    /**
     * Validación para el Modo Escritura (Confirmación manual o salto).
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
     * Validación Automática en Tiempo Real:
     * Compara el texto mientras el usuario escribe, permitiendo un avance fluido
     * si la respuesta es correcta sin necesidad de pulsar "Aceptar".
     */
    fun checkAutoAnswerTextMode(userInput: String) {
        if (textFeedback.value != null) return

        val correctAnswer = currentQuestion?.correctAnswer ?: ""

        if (userInput.limpiarTexto() == correctAnswer.limpiarTexto() && correctAnswer.isNotBlank()) {
            textFeedback.value = true
            score.value += 10

            viewModelScope.launch {
                delay(700) // Feedback de éxito prolongado para el usuario
                moveToNextQuestion()
            }
        }
    }

    /**
     * Orquestador de la transición entre preguntas.
     * Finaliza la partida cuando se agota el dataset.
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

    // --- GESTIÓN DE CORRUTINAS (TIEMPO) ---

    /**
     * Inicia un Job periódico que incrementa el contador de segundos.
     * Se cancela automáticamente mediante el ciclo de vida del ViewModel.
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