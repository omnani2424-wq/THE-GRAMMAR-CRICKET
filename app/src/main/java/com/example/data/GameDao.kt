package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC")
    fun getAllMatchRecords(): Flow<List<MatchRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchRecord(matchRecord: MatchRecord): Long

    @Query("SELECT * FROM grammar_stats")
    fun getAllGrammarStats(): Flow<List<GrammarStats>>

    @Query("SELECT * FROM grammar_stats WHERE category = :category LIMIT 1")
    suspend fun getGrammarStatsByCategory(category: String): GrammarStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGrammarStats(grammarStats: GrammarStats)

    @Query("DELETE FROM match_records")
    suspend fun clearAllMatches()

    @Query("DELETE FROM grammar_stats")
    suspend fun clearAllStats()
}
