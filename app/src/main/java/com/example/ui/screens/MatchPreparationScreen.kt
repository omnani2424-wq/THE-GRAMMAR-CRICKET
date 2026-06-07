package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.GeminiService
import com.example.ui.components.GradientAppBackground
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchPreparationScreen(viewModel: GameViewModel) {
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val opponentTeam by viewModel.opponentTeam.collectAsState()
    val useAiQuestions by viewModel.useAiQuestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()

    val isApiKeyAvailable = GeminiService.isApiKeyAvailable

    // Subtle spinner infinite rotation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    GradientAppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Match Selection & Config", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        if (!isLoading) {
                            IconButton(onClick = { viewModel.navigateTo(GameScreenState.Dashboard) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isLoading) {
                    // Match Setup Configuration Layout
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Team versus card
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(selectedTeam.iconChar, fontSize = 38.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(selectedTeam.shortName, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                    }

                                    Text(
                                        "VS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = selectedTeam.primaryColor,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(opponentTeam.iconChar, fontSize = 38.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(opponentTeam.shortName, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "CHOOSE QUESTION DECK GENERATION",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )

                             // Select Deck Row 1: Offline Curated Deck
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = borderStrokeForMode(!useAiQuestions, selectedTeam.primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setUseAiQuestions(false) }
                                    .testTag("offline_pool_selector"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)) // Professional Polish
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !useAiQuestions,
                                        onClick = { viewModel.setUseAiQuestions(false) },
                                        colors = RadioButtonDefaults.colors(selectedColor = selectedTeam.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Offline Curated Deck", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("A pool of GMAT/SAT level preloaded questions. Zero lag, high correctness, immediate starts.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Select Deck Row 2: Gemini AI Content Generator Mode
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = borderStrokeForMode(useAiQuestions, selectedTeam.primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setUseAiQuestions(true) }
                                    .testTag("ai_pool_selector"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)) // Professional Polish
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = useAiQuestions,
                                        onClick = { viewModel.setUseAiQuestions(true) },
                                        colors = RadioButtonDefaults.colors(selectedColor = selectedTeam.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Gemini AI Content Deck", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text("Generates endless, dynamic custom questions. Incorporates random IPL vocabulary themes natively!", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Warning or Info indicators
                            AnimatedVisibility(visible = useAiQuestions) {
                                if (isApiKeyAvailable) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = selectedTeam.primaryColor, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Gemini 3.5 Flash is actively configured. Real-time generation requires an internet connection.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF450A0A), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "API key missing in AI Studio Secrets! We will fall back gracefully to the preloaded offline pool when starting.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.startChampionshipMatch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("begin_match_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = selectedTeam.primaryColor)
                        ) {
                            Text(
                                "PLAY MATCH - 10 OVERS",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Loading State with progress bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1B1F)), // Professional Polish
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { loadingProgress.toFloat() / 100f },
                                modifier = Modifier
                                    .size(84.dp)
                                    .rotate(angle),
                                color = selectedTeam.primaryColor,
                                strokeWidth = 6.dp,
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                            Text(
                                text = "${loadingProgress}%",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "CURATING GRANDSTAND EXPERIENCE",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            if (useAiQuestions && isApiKeyAvailable) {
                                "Calling Gemini 3.5 Flash to synthesize 10 custom advanced grammar questions tailored with cricket accents..."
                            } else {
                                "Shuffling, checking and loading offline curated GMAT questions pool..."
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun borderStrokeForMode(selected: Boolean, color: Color): BorderStroke {
    // Custom helper to represent selected border cleanly
    return if (selected) {
        BorderStroke(2.dp, color)
    } else {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    }
}
