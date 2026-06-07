package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.model.IPLTeam
import com.example.model.GrammarQuestion
import com.example.ui.screens.GameScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class MatchAnimState {
    Idle,
    Bowling,
    Frozen,
    HitSix,
    HitFour,
    OutBowled,
    OutCaught,
    DotBall
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())

    // UI Screen switching state
    private val _screenState = MutableStateFlow<GameScreenState>(GameScreenState.Dashboard)
    val screenState: StateFlow<GameScreenState> = _screenState.asStateFlow()

    // Database observation
    private val _matchHistory = MutableStateFlow<List<MatchRecord>>(emptyList())
    val matchHistory: StateFlow<List<MatchRecord>> = _matchHistory.asStateFlow()

    private val _topicStats = MutableStateFlow<List<GrammarStats>>(emptyList())
    val topicStats: StateFlow<List<GrammarStats>> = _topicStats.asStateFlow()

    // Config options
    private val _selectedTeam = MutableStateFlow(IPLTeam.CSK)
    val selectedTeam: StateFlow<IPLTeam> = _selectedTeam.asStateFlow()

    private val _opponentTeam = MutableStateFlow(IPLTeam.MI)
    val opponentTeam: StateFlow<IPLTeam> = _opponentTeam.asStateFlow()

    private val _useAiQuestions = MutableStateFlow(false)
    val useAiQuestions: StateFlow<Boolean> = _useAiQuestions.asStateFlow()

    // Match Playing State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _questions = MutableStateFlow<List<GrammarQuestion>>(emptyList())
    val questions: StateFlow<List<GrammarQuestion>> = _questions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    // Scorecard parameters
    private val _runs = MutableStateFlow(0)
    val runs: StateFlow<Int> = _runs.asStateFlow()

    private val _wickets = MutableStateFlow(0)
    val wickets: StateFlow<Int> = _wickets.asStateFlow()

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount.asStateFlow()

    // Animation States
    private val _animState = MutableStateFlow(MatchAnimState.Idle)
    val animState: StateFlow<MatchAnimState> = _animState.asStateFlow()

    // Value from 0.0 to 1.0 driving coordinates on pitch
    private val _ballProgress = MutableStateFlow(0f)
    val ballProgress: StateFlow<Float> = _ballProgress.asStateFlow()

    // Ball height/projection offset during flight
    private val _ballHeight = MutableStateFlow(0f)
    val ballHeight: StateFlow<Float> = _ballHeight.asStateFlow()

    // Transverse offset of ball flight path
    private val _ballAngle = MutableStateFlow(0f)
    val ballAngle: StateFlow<Float> = _ballAngle.asStateFlow()

    // Selection/Evaluation details
    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption.asStateFlow()

    private val _isEvaluated = MutableStateFlow(false)
    val isEvaluated: StateFlow<Boolean> = _isEvaluated.asStateFlow()

    private var animationJob: Job? = null

    init {
        // Observe database flows using Coroutines
        viewModelScope.launch {
            repository.allMatchRecords.collectLatest {
                _matchHistory.value = it
            }
        }
        viewModelScope.launch {
            repository.allGrammarStats.collectLatest {
                _topicStats.value = it
            }
        }
    }

    fun selectTeam(team: IPLTeam) {
        _selectedTeam.value = team
        // Pick an opponent team that isn't the selected team
        val opponents = IPLTeam.values().filter { it != team }
        _opponentTeam.value = opponents.random()
    }

    fun setUseAiQuestions(value: Boolean) {
        _useAiQuestions.value = value
    }

    fun navigateTo(state: GameScreenState) {
        _screenState.value = state
    }

    /**
     * Resets match values and starts loading questions.
     */
    fun startChampionshipMatch() {
        _isLoading.value = true
        _loadingProgress.value = 0
        _runs.value = 0
        _wickets.value = 0
        _correctCount.value = 0
        _currentQuestionIndex.value = 0
        _selectedOption.value = null
        _isEvaluated.value = false
        _animState.value = MatchAnimState.Idle
        _ballProgress.value = 0f
        _ballHeight.value = 0f
        _ballAngle.value = 0f

        navigateTo(GameScreenState.MatchPreparation)

        viewModelScope.launch {
            try {
                val matchQuestions = repository.loadMatchQuestions(_useAiQuestions.value) { progress ->
                    _loadingProgress.value = progress * 10 // scale to percentage (1-10)
                }
                _questions.value = matchQuestions
                _isLoading.value = false
                navigateTo(GameScreenState.Playing)
                // Start the very first bowling sequence!
                prepareNewDelivery()
            } catch (e: Exception) {
                _isLoading.value = false
                navigateTo(GameScreenState.Dashboard)
            }
        }
    }

    /**
     * Initializes ball vectors and rolls the delivery.
     */
    fun prepareNewDelivery() {
        animationJob?.cancel()
        _animState.value = MatchAnimState.Idle
        _ballProgress.value = 0f
        _ballHeight.value = 0f
        _ballAngle.value = 0f
        _selectedOption.value = null
        _isEvaluated.value = false

        animationJob = viewModelScope.launch {
            delay(1000) // Small breather
            _animState.value = MatchAnimState.Bowling

            // Animate bowling down the pitch (0.0 to 1.0)
            val duration = 1800 // milliseconds
            val intervals = 30
            val delayTime = (duration / intervals).toLong()

            for (i in 1..intervals) {
                val t = i.toFloat() / intervals
                _ballProgress.value = t

                // Parabolic physical arc representing standard 3D bounce
                // Max height around t = 0.35, bouncing, then another small arc
                if (t < 0.6f) {
                    _ballHeight.value = sinShape(t / 0.6f) * 60f
                } else {
                    _ballHeight.value = sinShape((t - 0.6f) / 0.4f) * 25f
                }
                
                // Random subtle spinner variation
                _ballAngle.value = (t * 15f) - 5f

                delay(delayTime)
            }

            // Ball reaches bat - Freeze!
            _animState.value = MatchAnimState.Frozen
        }
    }

    private fun sinShape(normalized: Float): Float {
        return (4 * normalized * (1 - normalized)) // parabolic arc peaking at 1 at 0.5
    }

    fun selectAnswer(optionIndex: Int) {
        if (_isEvaluated.value) return
        _selectedOption.value = optionIndex
    }

    fun submitAnswer() {
        val currentIdx = _currentQuestionIndex.value
        val currentQ = _questions.value.getOrNull(currentIdx) ?: return
        val chosen = _selectedOption.value ?: return

        _isEvaluated.value = true
        val isCorrect = (chosen == currentQ.correctIndex)

        // Stop bowling animation if active
        animationJob?.cancel()

        animationJob = viewModelScope.launch {
            if (isCorrect) {
                _correctCount.value += 1
                
                // Strike selection: Decide whether Sixer or Foursome
                val strikeEvent = if (Random.nextBoolean()) {
                    _runs.value += 6
                    MatchAnimState.HitSix
                } else {
                    _runs.value += 4
                    MatchAnimState.HitFour
                }
                _animState.value = strikeEvent

                // Animate hit: Ball flies out of park (expanding size, soaring up, scaling back down)
                val duration = 2200
                val steps = 40
                val delayTime = (duration / steps).toLong()

                for (i in 1..steps) {
                    val t = i.toFloat() / steps
                    _ballProgress.value = 1.0f + t * 2.5f // Travels past batsman
                    _ballHeight.value = sinShape(t) * 200f // Soars into sky
                    _ballAngle.value = _ballAngle.value + (if (strikeEvent == MatchAnimState.HitSix) 4f else -3f)
                    delay(delayTime)
                }

            } else {
                // Incorrect answer: Out bowled, out caught, or dot ball!
                _wickets.value += 1
                val dismissals = listOf(MatchAnimState.OutBowled, MatchAnimState.OutCaught, MatchAnimState.DotBall)
                val event = dismissals.random()
                _animState.value = event

                // Animate miss! Ball crashes into wickets or fielders catch
                val duration = 1500
                val steps = 30
                val delayTime = (duration / steps).toLong()

                for (i in 1..steps) {
                    val t = i.toFloat() / steps
                    if (event == MatchAnimState.OutBowled) {
                        _ballProgress.value = 1.0f + t * 0.2f // Ball stops at wickets
                        _ballHeight.value = 2f
                    } else {
                        // Flying edge caught
                        _ballProgress.value = 1.0f + t * 1.5f
                        _ballHeight.value = sinShape(t) * 90f
                    }
                    delay(delayTime)
                }
            }

            // Persist statistical analysis per question category in DB asynchronously!
            repository.updateGrammarStats(currentQ.category, isCorrect)
        }
    }

    fun proceedToNext() {
        animationJob?.cancel()
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < 10) {
            _currentQuestionIndex.value = nextIdx
            prepareNewDelivery()
        } else {
            // Match complete - save match to DB, then show summary!
            viewModelScope.launch {
                repository.insertMatchResult(
                    playerTeam = _selectedTeam.value.teamName,
                    opponentTeam = _opponentTeam.value.teamName,
                    runsScored = _runs.value,
                    wicketsLost = _wickets.value,
                    answeredCount = 10,
                    correctCount = _correctCount.value
                )
                navigateTo(GameScreenState.Summary)
            }
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
