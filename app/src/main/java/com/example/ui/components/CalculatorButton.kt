package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.FunctionButtonDark
import com.example.ui.theme.FunctionButtonTextDark
import com.example.ui.theme.NumericButtonDark
import com.example.ui.theme.NumericButtonTextDark
import com.example.ui.theme.OperatorButtonDark
import com.example.ui.theme.OperatorButtonTextDark
import com.example.ui.theme.ScientificButtonDark
import com.example.ui.theme.ScientificButtonTextDark

enum class ButtonType {
    NUMERIC,
    OPERATOR,
    EQUALS,
    FUNCTION,
    SCIENTIFIC
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.NUMERIC,
    testTag: String = "btn_$text",
    fontSize: TextUnit = 24.sp,
    isHighlighted: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "button_scale"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val backgroundColor: Color
    val contentColor: Color
    var gradientBrush: Brush? = null

    when (type) {
        ButtonType.NUMERIC -> {
            backgroundColor = if (isDark) NumericButtonDark else MaterialTheme.colorScheme.surface
            contentColor = if (isDark) NumericButtonTextDark else MaterialTheme.colorScheme.onSurface
        }
        ButtonType.OPERATOR -> {
            backgroundColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                if (isDark) OperatorButtonDark else MaterialTheme.colorScheme.primaryContainer
            }
            contentColor = if (isHighlighted) {
                Color.White
            } else {
                if (isDark) OperatorButtonTextDark else MaterialTheme.colorScheme.onPrimaryContainer
            }
        }
        ButtonType.EQUALS -> {
            backgroundColor = AccentOrange
            contentColor = Color.White
            gradientBrush = Brush.linearGradient(
                colors = listOf(AccentOrange, AccentOrangeLight)
            )
        }
        ButtonType.FUNCTION -> {
            backgroundColor = if (isDark) FunctionButtonDark else MaterialTheme.colorScheme.secondaryContainer
            contentColor = if (isDark) FunctionButtonTextDark else MaterialTheme.colorScheme.onSecondaryContainer
        }
        ButtonType.SCIENTIFIC -> {
            backgroundColor = if (isDark) ScientificButtonDark else MaterialTheme.colorScheme.surfaceVariant
            contentColor = if (isDark) ScientificButtonTextDark else MaterialTheme.colorScheme.tertiary
        }
    }

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
            .scale(scale)
            .clip(shape)
            .then(
                if (gradientBrush != null) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier.background(backgroundColor)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .testTag(testTag)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
