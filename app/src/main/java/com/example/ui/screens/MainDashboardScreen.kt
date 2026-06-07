package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IPLTeam
import com.example.ui.components.GradientAppBackground
import com.example.ui.components.StatCard
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboardScreen(viewModel: GameViewModel) {
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val matchHistory by viewModel.matchHistory.collectAsState()
    val topicStats by viewModel.topicStats.collectAsState()

    // Calculate generic stats
    val totalMatches = matchHistory.size
    val totalRuns = matchHistory.sumOf { it.runsScored }
    val avgRuns = if (totalMatches > 0) totalRuns / totalMatches else 0
    val totalCorrect = matchHistory.sumOf { it.questionsCorrect }
    val totalAnswered = matchHistory.sumOf { it.questionsAnswered }
    val overallAccuracy = if (totalAnswered > 0) (totalCorrect * 100 / totalAnswered) else 0

    GradientAppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // User Profile Franchise Banner
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                border = BorderStroke(1.dp, selectedTeam.primaryColor.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .shadow(10.dp, shape = RoundedCornerShape(24.dp), ambientColor = selectedTeam.primaryColor.copy(alpha = 0.3f), spotColor = selectedTeam.primaryColor.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        selectedTeam.primaryColor,
                                        selectedTeam.secondaryColor
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedTeam.iconChar, fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedTeam.teamName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "\"${selectedTeam.slogan}\"",
                            fontSize = 12.sp,
                            color = selectedTeam.primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        val level = when {
                            overallAccuracy >= 90 -> "Hall of Fame Batsman 🏏"
                            overallAccuracy >= 75 -> "Powerplay Striker ⚡"
                            overallAccuracy >= 50 -> "All Rounder 🛡️"
                            else -> "Rookie Batsman 🌱"
                        }
                        
                        Text(
                            text = "Title: $level",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(GameScreenState.TeamSelection) },
                        modifier = Modifier
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("edit_team_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Franchise Team",
                            tint = Color.White
                        )
                    }
                }
            }

            // Stat summaries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(
                    title = "MATCHES",
                    value = totalMatches.toString(),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "AVG RUNS",
                    value = avgRuns.toString(),
                    color = selectedTeam.primaryColor,
                    modifier = Modifier.weight(1f),
                    subText = "Total: $totalRuns"
                )
                StatCard(
                    title = "ACCURACY",
                    value = "$overallAccuracy%",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    subText = "$totalCorrect / $totalAnswered"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main CTA - Play Tournament Match
            Button(
                onClick = { viewModel.startChampionshipMatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("play_match_button")
                    .shadow(10.dp, shape = RoundedCornerShape(18.dp), ambientColor = selectedTeam.primaryColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedTeam.primaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Match",
                    tint = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "PLAY CHAMPIONSHIP MATCH",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrolling view lists containing category strengths and histories
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grammar strengths card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "GRAMMAR PERFORMANCE ANALYTICS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (topicStats.isEmpty()) {
                                Text(
                                    text = "No analytics compiled yet. Complete custom matches to identify strengths, weak regions and review progress!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            } else {
                                topicStats.forEach { stat ->
                                    val percent = if (stat.totalAnswered > 0) (stat.totalCorrect * 100 / stat.totalAnswered) else 0
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stat.category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                            Text("$percent% accuracy (${stat.totalCorrect}/${stat.totalAnswered})", fontSize = 11.sp, color = selectedTeam.primaryColor, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { percent.toFloat() / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = selectedTeam.primaryColor,
                                            trackColor = Color(0xFF141318)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Match logs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT CHAMPIONSHIP MATCHES",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        if (matchHistory.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.resetStats() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.testTag("clear_history_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear History", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Logs", fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (matchHistory.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No historic records found. Go face your first delivery!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(matchHistory) { record ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${record.playerTeam} vs ${record.opponentTeam}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
                                    Text(
                                        text = dateStr,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${record.runsScored}/${record.wicketsLost}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = selectedTeam.primaryColor
                                    )
                                    Text(
                                        text = "Accuracy: ${if (record.questionsAnswered > 0) record.questionsCorrect * 100 / record.questionsAnswered else 0}%",
                                        fontSize = 11.sp,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Extra spacer padding
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
