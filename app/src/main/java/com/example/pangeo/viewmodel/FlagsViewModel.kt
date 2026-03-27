package com.example.pangeo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pangeo.ui.game.flags.FlagQuestion
import com.example.pangeo.ui.game.flags.FlagRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FlagsViewModel : ViewModel() {
    private var allQuestions: List<FlagQuestion> = emptyList()

    private val _currentQuestionIndex = mutableIntStateOf(0)
    val currentQuestionIndex: State<Int> = _currentQuestionIndex

    private val _score = mutableIntStateOf(0)
    val score: State<Int> = _score

    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    private val _selectedAnswer = mutableStateOf<String?>(null)
    val selectedAnswer: State<String?> = _selectedAnswer

    private val _elapsedTime = mutableIntStateOf(0)
    val elapsedTime: State<Int> = _elapsedTime

    // NUEVO: Para saber el total de preguntas y pintar el contador "1 / 44"
    private val _totalQuestions = mutableIntStateOf(0)
    val totalQuestions: State<Int> = _totalQuestions

    private var timerJob: Job? = null

    val currentQuestion: FlagQuestion?
        get() = if (allQuestions.isNotEmpty() && _currentQuestionIndex.intValue < allQuestions.size) {
            allQuestions[_currentQuestionIndex.intValue]
        } else null

    fun checkAnswer(selected: String) {
        if (_selectedAnswer.value != null) return

        _selectedAnswer.value = selected

        viewModelScope.launch {
            if (selected == currentQuestion?.correctAnswer) {
                _score.intValue += 10
            }

            delay(500) // 0.5 segundos de pausa

            if (_currentQuestionIndex.intValue < allQuestions.size - 1) {
                _currentQuestionIndex.intValue++
                _selectedAnswer.value = null
            } else {
                _isGameOver.value = true
                stopTimer()
            }
        }
    }

    fun resetGame() {
        _currentQuestionIndex.intValue = 0
        _score.intValue = 0
        _isGameOver.value = false
        _selectedAnswer.value = null
        _elapsedTime.intValue = 0

        // 1. Cargamos TODAS las preguntas del repositorio
        val repoQuestions = FlagRepository.getEuropeQuestions()

        // 2. Las barajamos y barajamos sus opciones
        allQuestions = repoQuestions.shuffled().map { question ->
            question.copy(options = question.options.shuffled())
        }

        // 3. Actualizamos el total (ahora marcará 44 automáticamente)
        _totalQuestions.intValue = allQuestions.size

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedTime.intValue++
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }
}