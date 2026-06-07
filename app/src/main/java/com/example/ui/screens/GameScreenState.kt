package com.example.ui.screens

sealed class GameScreenState {
    object Dashboard : GameScreenState()
    object TeamSelection : GameScreenState()
    object MatchPreparation : GameScreenState()
    object Playing : GameScreenState()
    object Summary : GameScreenState()
}
