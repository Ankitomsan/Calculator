package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorUiState

@Composable
fun CalculatorDisplay(
    uiState: CalculatorUiState,
    onToggleAngleMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onOpenHistory: () -> Unit,
    onCopyResult: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val exprScrollState = rememberScrollState()

    // Auto-scroll to end of expression as user types
    LaunchedEffect(uiState.expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Mode Chip (DEG / RAD)
                FilterChip(
                    selected = uiState.isRad,
                    onClick = onToggleAngleMode,
                    label = {
                        Text(
                            text = if (uiState.isRad) "RAD" else "DEG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("chip_deg_rad")
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Scientific keypad toggle
                    IconButton(
                        onClick = onToggleScientific,
                        modifier = Modifier.testTag("btn_toggle_scientific")
                    ) {
                        Icon(
                            imageVector = if (uiState.isScientificExpanded) Icons.Filled.Science else Icons.Outlined.Science,
                            contentDescription = "Toggle Scientific Functions",
                            tint = if (uiState.isScientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // History Button with count badge
                    BadgedBox(
                        badge = {
                            if (uiState.historyList.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = uiState.historyList.size.coerceAtMost(99).toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.testTag("btn_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Copy Button (visible if there's a result or expression)
                    val copyTarget = uiState.finalResult ?: uiState.liveResult.ifEmpty { uiState.expression }
                    if (copyTarget.isNotBlank()) {
                        IconButton(
                            onClick = { onCopyResult(copyTarget) },
                            modifier = Modifier.testTag("btn_copy")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy to clipboard",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expression line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                val expressionFontSize = when {
                    uiState.expression.length > 20 -> 24.sp
                    uiState.expression.length > 12 -> 32.sp
                    else -> 40.sp
                }

                Text(
                    text = uiState.expression.ifEmpty { "0" },
                    fontSize = if (uiState.isResultEvaluated) 24.sp else expressionFontSize,
                    color = if (uiState.isResultEvaluated) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (uiState.isResultEvaluated) FontWeight.Normal else FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("text_expression")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Result Line / Live Preview Line / Error Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("text_error")
                    )
                } else if (uiState.finalResult != null) {
                    // Evaluated final result
                    Text(
                        text = "= ${uiState.finalResult}",
                        fontSize = 42.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("text_final_result")
                    )
                } else if (uiState.liveResult.isNotEmpty()) {
                    // Live preview while user is typing
                    Text(
                        text = "= ${uiState.liveResult}",
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("text_live_result")
                    )
                }
            }
        }
    }
}
