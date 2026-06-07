package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerTeam: String,
    val opponentTeam: String,
    val runsScored: Int,
    val wicketsLost: Int,
    val questionsAnswered: Int,
    val questionsCorrect: Int,
    val timestamp: Long = System.currentTimeMillis()
)
