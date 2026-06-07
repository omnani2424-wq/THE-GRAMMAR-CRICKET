package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IPLTeam
import com.example.model.GrammarQuestion
import com.example.ui.components.GradientAppBackground
import com.example.viewmodel.GameViewModel

@Composable
fun MatchSummaryScreen(viewModel: GameViewModel) {
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val opponentTeam by viewModel.opponentTeam.collectAsState()
    val runs by viewModel.runs.collectAsState()
    val wickets by viewModel.wickets.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val questions by viewModel.questions.collectAsState()

    val accuracy = correctCount * 10 // scale to % (10 questions)
    val isWin = accuracy >= 50

    GradientAppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // 1. Victory announcement
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(
                        width = 2.dp,
                        color = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isWin) "🏆 CHAMPIONSHIP VICTORY!" else "🏏 MATCH FINISHED",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isWin) {
                            "Fabulous batting! Your grammar defense was impenetrable, leading ${selectedTeam.shortName} to a resounding win over ${opponentTeam.shortName}!"
                        } else {
                            "Tough delivery! ${opponentTeam.shortName}'s bowlers found gaps in your grammar armor. Retake training to return stronger!"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            // 2. Scorecard detail grid
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FINAL SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                        Text("$runs/$wickets", fontSize = 32.sp, fontWeight = FontWeight.Black, color = selectedTeam.primaryColor)
                        Text(selectedTeam.shortName, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }

                    // Vertical Divider
                    Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.White.copy(alpha = 0.08f)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ACCURACY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                        Text("$accuracy%", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        Text("$correctCount / 10 correct", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Question Reviews List
            Text(
                text = "MATCH STAT REVIEW",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(questions) { idx, question ->
                    // Just listing the GMAT question and category
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)), // Professional Polish
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = selectedTeam.primaryColor.copy(alpha = 0.1f)),
                                    border = BorderStroke(1.dp, selectedTeam.primaryColor.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = question.category.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = selectedTeam.primaryColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Text("Delivery #${idx + 1}", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.questionText,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Display the answer choices indicating correct choice
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Correct Answer: ${question.options[question.correctIndex]}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = question.explanation,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 4. Return to Dashboard Button
            Button(
                onClick = { viewModel.navigateTo(GameScreenState.Dashboard) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(56.dp)
                    .testTag("summary_back_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedTeam.primaryColor)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "RETURN TO CLINIC DASHBOARD",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White
                )
            }
        }
    }
}
