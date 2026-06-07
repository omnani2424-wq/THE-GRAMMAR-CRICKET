package com.example.model

import androidx.compose.ui.graphics.Color

enum class IPLTeam(
    val teamName: String,
    val shortName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val slogan: String,
    val iconChar: String
) {
    MI("Mumbai Indians", "MI", Color(0xFF004B87), Color(0xFFE3AA05), "Duniya Hila Denge Hum!", "⚡"),
    CSK("Chennai Super Kings", "CSK", Color(0xFFFBE116), Color(0xFF231F20), "Whistle Podu!", "🦁"),
    RCB("Royal Challengers Bengaluru", "RCB", Color(0xFF2B2A29), Color(0xFFEC1C24), "Play Bold!", "👑"),
    KKR("Kolkata Knight Riders", "KKR", Color(0xFF3A225D), Color(0xFFF1C40F), "Korbo Lorbo Jeetbo Re!", "⚔️"),
    DC("Delhi Capitals", "DC", Color(0xFF135092), Color(0xFFEA1C24), "Roar Macha!", "🐅"),
    RR("Rajasthan Royals", "RR", Color(0xFF254AA5), Color(0xFFE91E63), "Halla Bol!", "⚜️"),
    PBKS("Punjab Kings", "PBKS", Color(0xFFED1B24), Color(0xFFD1D3D4), "Sadda Punjab!", "🦁"),
    SRH("Sunrisers Hyderabad", "SRH", Color(0xFFF26522), Color(0xFF231F20), "Orange Army!", "🦅"),
    LSG("Lucknow Super Giants", "LSG", Color(0xFF005DA4), Color(0xFFEE3124), "Ab Apni Baari Hai!", "🏹"),
    GT("Gujarat Titans", "GT", Color(0xFF0B2240), Color(0xFFFFC221), "Aava De!", "🎯")
}
