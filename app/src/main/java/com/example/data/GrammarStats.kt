package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_stats")
data class GrammarStats(
    @PrimaryKey val category: String,
    val totalAnswered: Int,
    val totalCorrect: Int
)
