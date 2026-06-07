package com.example.data

import com.example.model.GrammarQuestion
import com.example.service.GeminiService
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {

    val allMatchRecords: Flow<List<MatchRecord>> = gameDao.getAllMatchRecords()
    val allGrammarStats: Flow<List<GrammarStats>> = gameDao.getAllGrammarStats()

    suspend fun insertMatchResult(
        playerTeam: String,
        opponentTeam: String,
        runsScored: Int,
        wicketsLost: Int,
        answeredCount: Int,
        correctCount: Int
    ) {
        val record = MatchRecord(
            playerTeam = playerTeam,
            opponentTeam = opponentTeam,
            runsScored = runsScored,
            wicketsLost = wicketsLost,
            questionsAnswered = answeredCount,
            questionsCorrect = correctCount
        )
        gameDao.insertMatchRecord(record)
    }

    suspend fun updateGrammarStats(category: String, isCorrect: Boolean) {
        val currentStats = gameDao.getGrammarStatsByCategory(category)
        if (currentStats != null) {
            val updated = GrammarStats(
                category = category,
                totalAnswered = currentStats.totalAnswered + 1,
                totalCorrect = currentStats.totalCorrect + if (isCorrect) 1 else 0
            )
            gameDao.insertOrUpdateGrammarStats(updated)
        } else {
            val initial = GrammarStats(
                category = category,
                totalAnswered = 1,
                totalCorrect = if (isCorrect) 1 else 0
            )
            gameDao.insertOrUpdateGrammarStats(initial)
        }
    }

    suspend fun clearHistory() {
        gameDao.clearAllMatches()
        gameDao.clearAllStats()
    }

    /**
     * Prepares exactly 10 questions for the game loop.
     * If useAiGenerator is true, it attempts to fetch dynamic questions from Gemini,
     * falling back to preloaded offline list when API is unreachable or unavailable.
     */
    suspend fun loadMatchQuestions(useAiGenerator: Boolean, onProgress: (Int) -> Unit): List<GrammarQuestion> {
        val questions = mutableListOf<GrammarQuestion>()
        
        if (useAiGenerator && GeminiService.isApiKeyAvailable) {
            for (i in 1..10) {
                onProgress(i)
                val fetched = GeminiService.fetchDynamicGrammarQuestion()
                if (fetched != null) {
                    questions.add(fetched)
                } else {
                    // Fallback to a random preloaded questions if single call fails
                    val unusedPreloaded = GrammarQuestion.PRELOADED_QUESTIONS
                        .filter { pq -> questions.none { q -> q.id == pq.id } }
                    if (unusedPreloaded.isNotEmpty()) {
                        questions.add(unusedPreloaded.random())
                    } else {
                        questions.add(GrammarQuestion.PRELOADED_QUESTIONS.random())
                    }
                }
            }
        } else {
            // Offline Mode - Grab 10 unique questions or as many as available
            val shuffled = GrammarQuestion.PRELOADED_QUESTIONS.shuffled()
            val takeCount = minOf(10, shuffled.size)
            questions.addAll(shuffled.take(takeCount))
            
            // If we still need more to reach exactly 10, fill with duplicates (safely)
            while (questions.size < 10) {
                questions.add(GrammarQuestion.PRELOADED_QUESTIONS.random())
            }
        }
        return questions.take(10)
    }
}
