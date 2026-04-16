package com.example.pangeo.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pangeo.ui.game.culture.CultureQuestion
import com.example.pangeo.ui.game.culture.CultureRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel que orquesta el modo "Supervivencia Cultural".
 * * Características Senior implementadas:
 * 1. Gestión de Dificultad: Ordena el pool de preguntas de fácil a difícil.
 * 2. Sistema de Rachas (Streaks): Multiplicadores de puntuación según el desempeño.
 * 3. Lógica de Comodines Dinámica: El comodín 50/50 se recarga y evoluciona a "Eliminación Total".
 * 4. Control de Victoria Absoluta: Gestiona el fin del pool de preguntas.
 */
class CultureViewModel : ViewModel() {
    private var allQuestions = emptyList<CultureQuestion>()
    private var availableQuestions = mutableListOf<CultureQuestion>()

    // --- ESTADOS DE PROGRESIÓN ---
    val streak = mutableStateOf(0)
    val score = mutableStateOf(0)
    val isGameOver = mutableStateOf(false)
    val isVictory = mutableStateOf(false)
    val hasUsedFiftyFifty = mutableStateOf(false)
    val bestStreak = mutableStateOf(0)
    val isNewRecord = mutableStateOf(false)

    // Feedback visual para recompensas (recarga de comodín)
    val showWildcardReward = mutableStateOf(false)

    // --- ESTADOS DE LA PREGUNTA ACTUAL ---
    var currentQuestion = mutableStateOf<CultureQuestion?>(null)
    var currentOptions = mutableStateOf<List<String>>(emptyList())
    val selectedAnswer = mutableStateOf<String?>(null)
    val isAnswerCorrect = mutableStateOf<Boolean?>(null)

    /**
     * Inicializa la sesión de supervivencia.
     * * Lógica de carga: Agrupa preguntas por dificultad y las concatena para asegurar
     * que el usuario no enfrente preguntas imposibles al inicio de su racha.
     */
    fun startGame(context: Context) {
        if (allQuestions.isEmpty()) {
            allQuestions = CultureRepository.loadQuestionsFromJson(context)
        }

        // Estructura de dificultad: Fácil -> Media -> Difícil
        val faciles = allQuestions.filter { it.difficulty == 1 }.shuffled()
        val medias = allQuestions.filter { it.difficulty == 2 }.shuffled()
        val dificiles = allQuestions.filter { it.difficulty == 3 }.shuffled()

        availableQuestions = (faciles + medias + dificiles).toMutableList()

        // Reset de estados globales
        streak.value = 0
        score.value = 0
        hasUsedFiftyFifty.value = false
        isGameOver.value = false
        isVictory.value = false
        isNewRecord.value = false
        showWildcardReward.value = false
        loadNextQuestion()
    }

    /**
     * Extrae la siguiente pregunta del pool.
     * Si no quedan preguntas, dispara el estado de Victoria Absoluta.
     */
    private fun loadNextQuestion() {
        if (availableQuestions.isEmpty()) {
            isVictory.value = true
            isGameOver.value = true
            return
        }
        selectedAnswer.value = null
        isAnswerCorrect.value = null
        val nextQ = availableQuestions.removeAt(0)
        currentQuestion.value = nextQ
        currentOptions.value = nextQ.options.shuffled()
    }

    /**
     * Validación de respuesta con sistema de multiplicadores y recompensas.
     * * Multiplicadores:
     * - Racha < 5: 10 XP
     * - Racha 5-9: 15 XP
     * - Racha 10+: 25 XP
     */
    fun checkAnswer(answer: String) {
        if (selectedAnswer.value != null || answer.isBlank()) return
        selectedAnswer.value = answer
        val correct = currentQuestion.value?.correctAnswer

        if (answer == correct) {
            isAnswerCorrect.value = true
            streak.value += 1

            // Lógica de recarga de comodín: Cada 100 aciertos
            if (streak.value % 100 == 0 && streak.value > 0) {
                if (hasUsedFiftyFifty.value) {
                    hasUsedFiftyFifty.value = false
                    showWildcardReward.value = true
                }
            }

            // Gestión de récord personal en tiempo real
            if (streak.value > bestStreak.value) {
                bestStreak.value = streak.value
                isNewRecord.value = true
            }

            // Cálculo de puntuación dinámica
            val multiplier = when {
                streak.value >= 10 -> 25
                streak.value >= 5 -> 15
                else -> 10
            }
            score.value += multiplier

            viewModelScope.launch {
                delay(600) // Delay para feedback visual en la UI
                showWildcardReward.value = false
                loadNextQuestion()
            }
        } else {
            // Error en supervivencia: Fin inmediato del juego (Muerte Súbita)
            isAnswerCorrect.value = false
            viewModelScope.launch {
                delay(1200)
                isGameOver.value = true
            }
        }
    }

    /**
     * Implementación del Comodín con evolución de poder.
     * * Racha < 500: Elimina 2 distractores (50/50 clásico).
     * * Racha >= 500: Modo "Eliminación Total" (Solo queda la respuesta correcta).
     */
    fun useFiftyFifty() {
        if (hasUsedFiftyFifty.value) return
        val question = currentQuestion.value ?: return
        val correct = question.correctAnswer

        if (streak.value >= 500) {
            // Filtrado total: Las incorrectas se convierten en cadenas vacías
            currentOptions.value = currentOptions.value.map { if (it == correct) it else "" }
        } else {
            // Selección aleatoria de 2 incorrectas para eliminar
            val incorrectOptions = currentOptions.value.filter { it != correct }.shuffled().take(2)
            currentOptions.value = currentOptions.value.map { if (incorrectOptions.contains(it)) "" else it }
        }

        hasUsedFiftyFifty.value = true
    }
}