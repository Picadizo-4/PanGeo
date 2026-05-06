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
 * * Actualización: Sistema de 3 vidas y recarga de comodines dinámica.
 */
class CultureViewModel : ViewModel() {
    private var allQuestions = emptyList<CultureQuestion>()
    private var availableQuestions = mutableListOf<CultureQuestion>()

    // --- ESTADOS DE PROGRESIÓN ---
    val streak = mutableStateOf(0)
    val score = mutableStateOf(0)
    val lives = mutableStateOf(3) // Sistema de 3 vidas
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

    fun startGame(context: Context) {
        if (allQuestions.isEmpty()) {
            allQuestions = CultureRepository.loadQuestionsFromJson(context)
        }

        val faciles = allQuestions.filter { it.difficulty == 1 }.shuffled()
        val medias = allQuestions.filter { it.difficulty == 2 }.shuffled()
        val dificiles = allQuestions.filter { it.difficulty == 3 }.shuffled()

        availableQuestions = (faciles + medias + dificiles).toMutableList()

        streak.value = 0
        score.value = 0
        lives.value = 3 // Reiniciar vidas
        hasUsedFiftyFifty.value = false
        isGameOver.value = false
        isVictory.value = false
        isNewRecord.value = false
        showWildcardReward.value = false
        loadNextQuestion()
    }

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

    fun checkAnswer(answer: String) {
        if (selectedAnswer.value != null || answer.isBlank()) return
        selectedAnswer.value = answer
        val correct = currentQuestion.value?.correctAnswer

        if (answer == correct) {
            isAnswerCorrect.value = true
            streak.value += 1

            // Lógica de recarga de comodín ESCALONADA:
            // - Hasta racha 500: Cada 50 preguntas.
            // - Más de 500: Cada 25 preguntas.
            val reloadInterval = if (streak.value <= 500) 50 else 25
            if (streak.value % reloadInterval == 0 && streak.value > 0) {
                if (hasUsedFiftyFifty.value) {
                    hasUsedFiftyFifty.value = false
                    showWildcardReward.value = true
                }
            }

            if (streak.value > bestStreak.value) {
                bestStreak.value = streak.value
                isNewRecord.value = true
            }

            val multiplier = when {
                streak.value >= 10 -> 25
                streak.value >= 5 -> 15
                else -> 10
            }
            score.value += multiplier

            viewModelScope.launch {
                delay(600)
                showWildcardReward.value = false
                loadNextQuestion()
            }
        } else {
            isAnswerCorrect.value = false
            lives.value -= 1 // Restar vida

            viewModelScope.launch {
                delay(1200)
                if (lives.value > 0) {
                    // Si aún tiene vidas, continuamos a la siguiente pregunta
                    loadNextQuestion()
                } else {
                    // Fin del juego si no quedan vidas
                    isGameOver.value = true
                }
            }
        }
    }

    fun useFiftyFifty() {
        if (hasUsedFiftyFifty.value) return
        val question = currentQuestion.value ?: return
        val correct = question.correctAnswer

        if (streak.value >= 500) {
            currentOptions.value = currentOptions.value.map { if (it == correct) it else "" }
        } else {
            val incorrectOptions = currentOptions.value.filter { it != correct }.shuffled().take(2)
            currentOptions.value = currentOptions.value.map { if (incorrectOptions.contains(it)) "" else it }
        }

        hasUsedFiftyFifty.value = true
    }
}