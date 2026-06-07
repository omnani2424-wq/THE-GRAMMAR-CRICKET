package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IPLTeam
import com.example.model.GrammarQuestion
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.MatchAnimState

@Composable
fun MatchPlayScreen(viewModel: GameViewModel) {
    val selectedTeam by viewModel.selectedTeam.collectAsState()
    val opponentTeam by viewModel.opponentTeam.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val currentIdx by viewModel.currentQuestionIndex.collectAsState()
    
    val runs by viewModel.runs.collectAsState()
    val wickets by viewModel.wickets.collectAsState()
    
    val animState by viewModel.animState.collectAsState()
    val ballProgress by viewModel.ballProgress.collectAsState()
    val ballHeight by viewModel.ballHeight.collectAsState()
    val ballAngle by viewModel.ballAngle.collectAsState()
    
    val selectedOption by viewModel.selectedOption.collectAsState()
    val isEvaluated by viewModel.isEvaluated.collectAsState()

    val currentQ = questions.getOrNull(currentIdx)

    // Crowd & Physics: Crowd cheer particle loop
    var cheerParticles by remember { mutableStateOf(emptyList<CheerParticle>()) }

    LaunchedEffect(animState) {
        if (animState == MatchAnimState.HitSix || animState == MatchAnimState.HitFour) {
            val newParticles = List(75) { id ->
                CheerParticle(
                    id = id,
                    x = (0.12f + kotlin.random.Random.nextFloat() * 0.76f),
                    y = 0.25f, // starting near the crowd grandstand horizon
                    vx = (kotlin.random.Random.nextFloat() - 0.5f) * 0.025f,
                    vy = -(0.018f + kotlin.random.Random.nextFloat() * 0.038f),
                    color = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFF3B82F6), // Sky Blue
                        Color(0xFF10B981), // Emerald
                        Color(0xFFEF4444), // Crimson
                        Color(0xFFFBE116), // Yellow
                        Color(0xFFA855F7), // Amethyst Purple
                        Color(0xFF22D3EE)  // Bright Cyan
                    ).random(),
                    size = 4f + kotlin.random.Random.nextFloat() * 8f,
                    alpha = 1.0f,
                    life = 55 + kotlin.random.Random.nextInt(45)
                )
            }
            cheerParticles = newParticles
            
            while (cheerParticles.isNotEmpty()) {
                kotlinx.coroutines.delay(16)
                cheerParticles = cheerParticles.mapNotNull { p ->
                    if (p.life <= 0) null
                    else {
                        p.copy(
                            x = (p.x + p.vx).coerceIn(0f, 1f),
                            y = (p.y + p.vy).coerceIn(0f, 1f),
                            vy = p.vy + 0.00035f, // realistic gravitational pull down
                            alpha = (p.life.toFloat() / 100f).coerceIn(0f, 1f),
                            life = p.life - 1
                        )
                    }
                }
            }
        } else {
            cheerParticles = emptyList()
        }
    }

    // Scoreboard animation: Flashing background indicator
    val scoreboardBgColor by animateColorAsState(
        targetValue = when (animState) {
            MatchAnimState.HitSix, MatchAnimState.HitFour -> Color(0xFF064E3B) // emerald flash on boundary
            MatchAnimState.OutBowled, MatchAnimState.OutCaught -> Color(0xFF7F1D1D) // ruby alert on wicket
            else -> Color(0xFF1C1B1F) // standard professional gray container
        },
        animationSpec = tween(durationMillis = 400),
        label = "ScoreboardBg"
    )

    // Scoreboard animation: Score numbers scale pulse trigger
    var scorePulse by remember { mutableStateOf(1f) }
    LaunchedEffect(runs, wickets) {
        if (runs > 0 || wickets > 0) {
            animate(
                initialValue = 1f,
                targetValue = 1.3f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            ) { value, _ -> scorePulse = value }
            animate(
                initialValue = 1.3f,
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            ) { value, _ -> scorePulse = value }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Professional Pitch Dark
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Core Header Scoreboard (Animate and flash upon score changes!)
            Card(
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = scoreboardBgColor),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Current Batting Team Scorecard
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(selectedTeam.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedTeam.iconChar, fontSize = 12.sp, color = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedTeam.shortName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$runs/$wickets",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = selectedTeam.primaryColor,
                                    modifier = Modifier.graphicsLayer(
                                        scaleX = scorePulse,
                                        scaleY = scorePulse,
                                        transformOrigin = TransformOrigin(0f, 0.5f)
                                    )
                                )
                                Text(
                                    text = "  (Over ${currentIdx}.${(if (animState == MatchAnimState.Bowling) "0" else "1")})",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Right: vs Opponent indicator
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF050505)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Opponent: ", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(opponentTeam.iconChar + " " + opponentTeam.shortName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Match Progress Tracker Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (i in 0 until 10) {
                                val segmentColor = when {
                                    i < currentIdx -> Color(0xFF10B981)
                                    i == currentIdx -> if (isEvaluated) {
                                        val isCorrect = selectedOption == currentQ?.correctIndex
                                        if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                                    } else {
                                        Color(0xFF2563EB)
                                    }
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(segmentColor)
                                )
                            }
                        }
                        Text(
                            text = "OVR ${currentIdx}/10",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 2. Cinematic 3D Arena Viewport (Contains Stadium & floating question overlays)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fills the rest of screen and shows stunning depth
            ) {
                StadiumCanvas(
                    animState = animState,
                    ballProgress = ballProgress,
                    ballHeight = ballHeight,
                    ballAngle = ballAngle,
                    selectedTeam = selectedTeam,
                    opponentTeam = opponentTeam,
                    cheerParticles = cheerParticles
                )

                // Match events announcer overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val eventText = when (animState) {
                        MatchAnimState.HitSix -> "👋 MASSIVE SIX! OVER THE BOUNDARY!"
                        MatchAnimState.HitFour -> "🔥 CRACKING FOUR! PAST THE FIELDER!"
                        MatchAnimState.OutBowled -> "🔴 CLEAN BOWLED! STUMPS FLYING!"
                        MatchAnimState.OutCaught -> "🏏 OUT! CAUGHT AT THE COVERS!"
                        MatchAnimState.DotBall -> "🛡️ SOLID DEFENSE! DOT BALL."
                        MatchAnimState.Bowling -> "⚾ BOWLER RUNNING IN..."
                        else -> null
                    }

                    if (eventText != null) {
                        val revealColor = when (animState) {
                            MatchAnimState.HitSix, MatchAnimState.HitFour -> Color(0xFF10B981)
                            MatchAnimState.OutBowled, MatchAnimState.OutCaught -> Color(0xFFEF4444)
                            else -> Color(0xFF3B82F6)
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = revealColor),
                            modifier = Modifier.shadow(8.dp, shape = RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = eventText,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // 3. Immersive HUD Overlays (Floating frosted Glassmorphism card overlays)
                if (currentQ != null) {
                    if (animState == MatchAnimState.Frozen || isEvaluated) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)) // Dim backfield nicely for readability
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QuestionAndOptionsView(
                                question = currentQ,
                                selectedOption = selectedOption,
                                isEvaluated = isEvaluated,
                                selectedTeam = selectedTeam,
                                onOptionSelected = { viewModel.selectAnswer(it) },
                                onSubmit = { viewModel.submitAnswer() },
                                onNext = { viewModel.proceedToNext() }
                            )
                        }
                    } else {
                        // Bowler run-up pre-delivery scene loader
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.20f)),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val msg = when (animState) {
                                MatchAnimState.Idle -> "PREPARING NEW CRICKET DELIVERY..."
                                else -> "GET READY TO DEFEND YOUR WICKETS!"
                            }
                            CircularProgressIndicator(color = selectedTeam.primaryColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = msg,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Crowd & Physics: Cheer Particle representation
data class CheerParticle(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val alpha: Float,
    val life: Int
)

@Composable
fun StadiumCanvas(
    animState: MatchAnimState,
    ballProgress: Float,
    ballHeight: Float,
    ballAngle: Float,
    selectedTeam: IPLTeam,
    opponentTeam: IPLTeam,
    cheerParticles: List<CheerParticle>
) {
    // Cinematic Game Loop: Camera transitions
    // - Wide stadium view (scale 1.0) during hits/wickets/completions so viewers see the full play
    // - Bowler close-up (scale 1.75) zoomed near bowler's crease during bowling run-ups
    // - Batter-focused view (scale 1.45) focused near batting crease when question overlay pops up
    val cameraScale by animateFloatAsState(
        targetValue = when (animState) {
            MatchAnimState.Bowling, MatchAnimState.Idle -> 1.75f
            MatchAnimState.Frozen -> 1.45f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "CameraScale"
    )

    val cameraPivotXFactor by animateFloatAsState(
        targetValue = when (animState) {
            MatchAnimState.Bowling, MatchAnimState.Idle -> 0.50f
            MatchAnimState.Frozen -> 0.45f
            else -> 0.50f
        },
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "CameraPivotX"
    )

    val cameraPivotYFactor by animateFloatAsState(
        targetValue = when (animState) {
            MatchAnimState.Bowling, MatchAnimState.Idle -> 0.28f // far-end bowler crease zooming
            MatchAnimState.Frozen -> 0.78f                       // close-end batsman crease zooming
            else -> 0.50f                                        // neutral center focus on field
        },
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "CameraPivotY"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A2C1A), // deep green twilight stadium canopy
                        Color(0xFF2D5A27)  // professional outfield green
                    )
                )
            )
    ) {
        val w = size.width
        val h = size.height

        // --- SECTION A: STATIONARY BACKGROUND STADIUM & FLOODLIGHTS (Creates gorgeous parallax and stable atmosphere) ---
        val horizonY = h * 0.25f
        
        // Crowd background stand
        drawRect(
            color = Color(0xFF1C1B1F), // Professional Polish
            size = Size(w, horizonY)
        )
        // Golden stadium spotlight beams radiating down with console-quality Bloom floodlights
        drawSpotlightBeam(w * 0.15f, 0f, w * 0.35f, h)
        drawSpotlightBeam(w * 0.85f, 0f, w * 0.65f, h)

        // Outfield Green Grass Base background
        drawRect(
            color = Color(0xFF2D5A27), // Polished turf green
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // --- SECTION B: CINEMATICALLY TRANSFORMED PLAY FIELDS & CHARACTERS (Controlled by Camera Controller) ---
        val pivotX = w * cameraPivotXFactor
        val pivotY = h * cameraPivotYFactor

        withTransform({
            scale(scaleX = cameraScale, scaleY = cameraScale, pivot = Offset(pivotX, pivotY))
        }) {
            // 2. Draw 3D Dirt Pitch (Trapezoid with vanishing perspective!)
            val fieldPath = Path().apply {
                moveTo(w * 0.44f, horizonY + 10f) // far end (narrower)
                lineTo(w * 0.56f, horizonY + 10f)
                lineTo(w * 0.70f, h + 100f)        // close end (wider, extends past view)
                lineTo(w * 0.30f, h + 100f)
                close()
            }
            drawPath(
                path = fieldPath,
                color = Color(0xFFCCAC82) // smooth light brown dirt pitch
            )

            // Crease markings (white lines)
            // Bowling crease (top end)
            drawLine(
                color = Color.White,
                start = Offset(w * 0.43f, horizonY + 40f),
                end = Offset(w * 0.57f, horizonY + 40f),
                strokeWidth = 3f
            )
            // Batting crease (bottom end)
            drawLine(
                color = Color.White,
                start = Offset(w * 0.33f, h * 0.85f),
                end = Offset(w * 0.67f, h * 0.85f),
                strokeWidth = 5f
            )

            // 3. Draw Wickets
            // Stumps top end (small, far away)
            drawWickets(
                centerX = w * 0.5f,
                centerY = horizonY + 35f,
                width = 15f,
                height = 25f,
                isHit = false
            )

            // Stumps bottom end (large, close-up)
            val areStumpsHit = (animState == MatchAnimState.OutBowled)
            drawWickets(
                centerX = w * 0.5f,
                centerY = h * 0.83f,
                width = 60f,
                height = 100f,
                isHit = areStumpsHit
            )

            // 4. Draw Bowler stick figure (Opponent)
            // Animates moving forward from top left toward wickets
            val bowlerOffsetFactor = if (animState == MatchAnimState.Bowling) (1f - ballProgress).coerceIn(0f, 1f) else 1f
            val bowlerX = w * 0.47f - (bowlerOffsetFactor * 40f)
            val bowlerY = horizonY + 15f + ( (1f - bowlerOffsetFactor) * 20f)
            
            drawStickFigure(
                centerX = bowlerX,
                centerY = bowlerY,
                scale = 0.4f,
                color = opponentTeam.primaryColor,
                isBatting = false
            )

            // 5. Draw Batsman stick figure (User)
            // Located at the bottom close crease, swings bat on HitSix/HitFour
            val isBatsmanSwinging = (animState == MatchAnimState.HitSix || animState == MatchAnimState.HitFour)
            drawStickFigure(
                centerX = w * 0.42f,
                centerY = h * 0.76f,
                scale = 1.3f,
                color = selectedTeam.primaryColor,
                isBatting = true,
                isSwinging = isBatsmanSwinging
            )

            // 6. Draw Cricket Ball
            val isBallFlying = (animState == MatchAnimState.Bowling || animState == MatchAnimState.HitSix || animState == MatchAnimState.HitFour || animState == MatchAnimState.OutBowled || animState == MatchAnimState.OutCaught)
            
            if (isBallFlying || animState == MatchAnimState.Frozen) {
                // Ball Coordinates Interpolations
                val startY = horizonY + 35f
                val endY = h * 0.82f
                
                val currentX: Float
                val currentY: Float
                val currentRadius: Float

                if (ballProgress <= 1.0f) {
                    // Bowling down pitch toward batsman
                    val p = ballProgress
                    currentX = (w * 0.5f) + (ballAngle * p * 1.5f)
                    currentY = startY + (endY - startY) * p - ballHeight
                    // Scales larger as it approaches screen camera
                    currentRadius = 4f + (14f * p)
                } else {
                    // Ball struck! Struck ball expands and flies off screen
                    val p = (ballProgress - 1.0f) / 2.5f // normalized progression past batsman [0..1]
                    val pathDirectionSign = if (animState == MatchAnimState.HitSix) 1f else -1f
                    
                    currentX = (w * 0.5f) + (ballAngle * 1.5f) + (pathDirectionSign * w * 0.6f * p)
                    currentY = endY - ( (h * 0.6f) * p ) - ballHeight
                    // Expand radius as it flies higher, then shrink
                    currentRadius = 18f + (24f * sinShape(p))
                }

                // Draw leather ball (gorgeous red radial glow!)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B)),
                        center = Offset(currentX, currentY),
                        radius = currentRadius
                    ),
                    radius = currentRadius,
                    center = Offset(currentX, currentY)
                )

                // Dynamic shadow of ball on the ground
                drawCircle(
                    color = Color.Black.copy(alpha = 0.35f),
                    radius = currentRadius * 0.7f,
                    center = Offset(currentX, currentY + ballHeight + (currentRadius * 0.5f))
                )
            }

            // Draw Cheer Particles inside cinematic coordinated zoom space!
            cheerParticles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x * w, p.y * h)
                )
            }
        }
    }
}

// Visual Polish: Add a multi-layered bloom/glow effect to the stadium floodlights to achieve a high-end, console-quality aesthetic.
private fun DrawScope.drawSpotlightBeam(sourceX: Float, sourceY: Float, targetX: Float, targetY: Float) {
    // 1. Draw volumetric spotlight light cylinder
    val beamPath = Path().apply {
        moveTo(sourceX - 6f, sourceY)
        lineTo(sourceX + 6f, sourceY)
        lineTo(targetX + 220f, targetY)
        lineTo(targetX - 220f, targetY)
        close()
    }
    drawPath(
        path = beamPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.20f), Color.Transparent)
        )
    )

    // 2. Multi-layered radial gradients to simulate ultra-realistic light halo scattering (Bloom glow)
    // White-hot core bulb
    drawCircle(
        color = Color.White,
        radius = 8f,
        center = Offset(sourceX, sourceY)
    )
    // Golden-yellow inner saturated glow halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.85f), Color.Transparent),
            center = Offset(sourceX, sourceY),
            radius = 22f
        ),
        radius = 22f,
        center = Offset(sourceX, sourceY)
    )
    // Soft amber middle glow halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.45f), Color.Transparent),
            center = Offset(sourceX, sourceY),
            radius = 60f
        ),
        radius = 60f,
        center = Offset(sourceX, sourceY)
    )
    // Atmospheric ambient scatter halo (large scale, very low opacity)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xDDEFAB84).copy(alpha = 0.15f), Color.Transparent),
            center = Offset(sourceX, sourceY),
            radius = 110f
        ),
        radius = 110f,
        center = Offset(sourceX, sourceY)
    )
}

private fun sinShape(normalized: Float): Float {
    return (4 * normalized * (1 - normalized))
}

private fun DrawScope.drawWickets(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    isHit: Boolean
) {
    val stumpWidth = width / 7f
    val spacing = (width - (stumpWidth * 3)) / 2f

    val baseLeftX = centerX - (width / 2)

    for (i in 0..2) {
        val sx = baseLeftX + i * (stumpWidth + spacing)
        
        // If stumps are hit by incorrect answer, draw them flying apart asynchronously!
        val angle = if (isHit) {
            when (i) {
                0 -> -35f
                1 -> 15f
                else -> 45f
            }
        } else {
            0f
        }

        val offsetY = if (isHit) -height * 0.2f else 0f
        val offsetX = if (isHit) {
            when (i) {
                0 -> -width * 0.4f
                1 -> width * 0.1f
                else -> width * 0.5f
            }
        } else {
            0f
        }

        translate(left = offsetX, top = offsetY) {
            rotate(degrees = angle, pivot = Offset(sx + stumpWidth/2, centerY + height)) {
                drawRect(
                    color = Color(0xFFB45309), // wooden brown stumps
                    topLeft = Offset(sx, centerY),
                    size = Size(stumpWidth, height)
                )
            }
        }
    }

    // Draw bails (two horizontal top caps)
    if (!isHit) {
        drawRect(
            color = Color(0xFF78350F),
            topLeft = Offset(baseLeftX - 3f, centerY - 6f),
            size = Size(width + 6f, height * 0.08f)
        )
    }
}

private fun DrawScope.drawStickFigure(
    centerX: Float,
    centerY: Float,
    scale: Float,
    color: Color,
    isBatting: Boolean,
    isSwinging: Boolean = false
) {
    val headRadius = 12f * scale
    val spineLength = 32f * scale

    // Coordinates center
    val headCenter = Offset(centerX, centerY)
    val neck = Offset(centerX, centerY + headRadius)
    val pelvis = Offset(centerX, centerY + headRadius + spineLength)

    // Draw solid Head sphere
    drawCircle(
        color = color,
        radius = headRadius,
        center = headCenter
    )

    // Body spine line
    drawLine(
        color = color,
        start = neck,
        end = pelvis,
        strokeWidth = 3f * scale
    )

    // Shoulder line
    val shoulderY = neck.y + (8f * scale)
    val leftShoulder = Offset(centerX - 12f * scale, shoulderY)
    val rightShoulder = Offset(centerX + 12f * scale, shoulderY)
    drawLine(
        color = color,
        start = leftShoulder,
        end = rightShoulder,
        strokeWidth = 3f * scale
    )

    if (isBatting) {
        // BATSMAN ARMS & CRICKET BAT DRAWING
        // Right hand holds bat, left swings
        val handReachX = if (isSwinging) headCenter.x + 36f * scale else headCenter.x - 14f * scale
        val handReachY = headCenter.y + 24f * scale
        
        drawLine(
            color = color,
            start = rightShoulder,
            end = Offset(handReachX, handReachY),
            strokeWidth = 3f * scale
        )

        // Draw structural wooden cricket Bat
        val batAngle = if (isSwinging) -30f else 65f
        rotate(degrees = batAngle, pivot = Offset(handReachX, handReachY)) {
            // Main wide bat face
            drawRect(
                color = Color(0xFFD97706), // glowing golden wooden bat
                topLeft = Offset(handReachX - 4f * scale, handReachY),
                size = Size(8f * scale, 36f * scale)
            )
            // Black handle grip
            drawLine(
                color = Color.Black,
                start = Offset(handReachX, handReachY),
                end = Offset(handReachX, handReachY - 14f * scale),
                strokeWidth = 2.5f * scale
            )
        }

    } else {
        // BOWLER ARMS
        // Reaching up diagonally to deliver bowling release
        drawLine(
            color = color,
            start = leftShoulder,
            end = Offset(centerX - 18f * scale, shoulderY - 12f * scale),
            strokeWidth = 3f * scale
        )
        drawLine(
            color = color,
            start = rightShoulder,
            end = Offset(centerX + 16f * scale, shoulderY + 16f * scale),
            strokeWidth = 3f * scale
        )
    }

    // Legs drawing (sturdy sports stance!)
    val leftFoot = Offset(centerX - 16f * scale, pelvis.y + 28f * scale)
    val rightFoot = Offset(centerX + 16f * scale, pelvis.y + 28f * scale)

    drawLine(
        color = color,
        start = pelvis,
        end = leftFoot,
        strokeWidth = 3f * scale
    )
    drawLine(
        color = color,
        start = pelvis,
        end = rightFoot,
        strokeWidth = 3f * scale
    )
}

@Composable
fun QuestionAndOptionsView(
    question: GrammarQuestion,
    selectedOption: Int?,
    isEvaluated: Boolean,
    selectedTeam: IPLTeam,
    onOptionSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Core Glassmorphism Base Card Panel (frosted, elegant panel with white reflections)
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xDC111827) // ~86% Dark frosted slate glass
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), // translucent frosty border
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Question Category Label Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = selectedTeam.primaryColor.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, selectedTeam.primaryColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = question.category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = selectedTeam.primaryColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    "Grammar Challenge Checklist",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question Statement Translucent Paragraph Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F293B).copy(alpha = 0.45f)), // ultra subtle glass backing
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = selectedTeam.primaryColor,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = question.questionText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multiple Choices list with glowing transparent states
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                question.options.forEachIndexed { idx, option ->
                    val isSelected = (selectedOption == idx)
                    val isCorrect = (question.correctIndex == idx)
                    
                    // Translucent aesthetic neon border colors
                    val borderOutlineColor = when {
                        isEvaluated && isCorrect -> Color(0xFF10B981) // emerald accent
                        isEvaluated && isSelected && !isCorrect -> Color(0xFFEF4444) // warning red
                        isSelected -> selectedTeam.primaryColor // user selection glow
                        else -> Color.White.copy(alpha = 0.08f) // elegant standard border
                    }

                    val optionBgColor = when {
                        isEvaluated && isCorrect -> Color(0x2810B981) // premium translucent green strike
                        isEvaluated && isSelected && !isCorrect -> Color(0x28EF4444) // premium translucent red dismissal
                        isSelected -> selectedTeam.primaryColor.copy(alpha = 0.16f) // soft primary hover tint
                        else -> Color(0x12FFFFFF) // clean translucent glass option backing
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (isSelected || (isEvaluated && isCorrect)) 2.dp else 1.dp, borderOutlineColor),
                        colors = CardDefaults.cardColors(containerColor = optionBgColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isEvaluated) { onOptionSelected(idx) }
                            .testTag("option_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val optionPrefix = when (idx) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                else -> "D"
                            }
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) selectedTeam.primaryColor else Color(0x25FFFFFF))
                                    .border(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionPrefix,
                                    color = if (isSelected) (if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White) else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = option,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            if (isEvaluated && isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Submit & Explanation Sections (Clean high contrast and integrated coaching insights)
            if (!isEvaluated) {
                Button(
                    onClick = onSubmit,
                    enabled = selectedOption != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_answer_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedTeam.primaryColor,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Text(
                        "STRIKE DELIVERY",
                        fontWeight = FontWeight.Black,
                        color = if (selectedTeam.primaryColor == Color(0xFFFBE116)) Color.Black else Color.White,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Frosted warning/success explanation card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x181E3A8A)), // clean deep blue-purple glass tint
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GRAMMAR INSIGHT & COACH RULES", fontWeight = FontWeight.Black, color = Color(0xFF93C5FD), fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.explanation,
                            color = Color(0xFFBFDBFE),
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_ball_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("FACE NEXT DELIVERY", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
