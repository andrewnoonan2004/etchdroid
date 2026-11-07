package eu.depau.etchdroid.ui.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Adds a pointing hand mouse cursor on hover for desktop, laptop, and mouse users.
 */
fun Modifier.hoverHandCursor(): Modifier = this.pointerHoverIcon(PointerIcon.Hand)

/**
 * Accessible high-visibility focus ring indicator for keyboard navigation (Tab/Shift-Tab/D-pad).
 */
fun Modifier.expressiveFocusBorder(
    shape: Shape = RoundedCornerShape(16.dp),
    focusBorderColor: Color? = null,
    borderWidth: Dp = 2.5.dp
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val color = focusBorderColor ?: MaterialTheme.colorScheme.primary

    this
        .focusable(interactionSource = interactionSource)
        .border(
            width = when {
                isFocused -> borderWidth
                isHovered -> 1.dp
                else -> 0.dp
            },
            color = when {
                isFocused -> color
                isHovered -> color.copy(alpha = 0.4f)
                else -> Color.Transparent
            },
            shape = shape
        )
}

/**
 * Combines mouse hover pointer icon, keyboard focus indicator, and optional click action.
 */
fun Modifier.interactiveElement(
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val mod = this
        .hoverHandCursor()
        .expressiveFocusBorder(shape = shape)

    if (onClick != null) {
        mod.clickable(onClick = onClick)
    } else {
        mod
    }
}

/**
 * Hardware keyboard shortcut listener for large screen / desktop usage.
 * Supports:
 * - Ctrl/Cmd + O: Open file
 * - Escape: Cancel / Dismiss / Back
 * - Ctrl/Cmd + R or F5: Refresh
 */
fun Modifier.handleKeyboardShortcuts(
    onOpenFile: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onDismissOrBack: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null
): Modifier = this.onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false

    val isCtrlOrMeta = event.isCtrlPressed || event.isMetaPressed

    when {
        isCtrlOrMeta && event.key == Key.O && onOpenFile != null -> {
            onOpenFile()
            true
        }
        (event.key == Key.Escape || event.key == Key.Back) && (onBack != null || onDismissOrBack != null) -> {
            (onBack ?: onDismissOrBack)?.invoke()
            true
        }
        (event.key == Key.Enter || event.key == Key.NumPadEnter) && onConfirm != null -> {
            onConfirm()
            true
        }
        ((isCtrlOrMeta && event.key == Key.R) || event.key == Key.F5) && onRefresh != null -> {
            onRefresh()
            true
        }
        isCtrlOrMeta && (event.key == Key.Comma || event.key == Key.S) && onSettings != null -> {
            onSettings()
            true
        }
        else -> false
    }
}

/**
 * Material 3 Expressive animated wavy progress indicator.
 * Renders an organic, smooth sinusoidal wave along the active progress track.
 */
@Composable
fun ExpressiveWavyProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float = -1f, // 0.0f to 1.0f (or < 0 for indeterminate)
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    waveHeight: Dp = 4.dp,
    strokeWidth: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgressTransition")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val strokePx = strokeWidth.toPx()
        val waveAmplitudePx = waveHeight.toPx()

        // Draw track
        drawRoundRect(
            color = trackColor,
            topLeft = Offset(0f, centerY - (strokePx / 2f)),
            size = Size(width, strokePx),
            cornerRadius = CornerRadius(strokePx / 2f, strokePx / 2f)
        )

        // Draw active wavy progress
        val activeWidth = if (progress >= 0f) {
            (width * progress.coerceIn(0f, 1f))
        } else {
            width
        }

        if (activeWidth > 2f) {
            val path = Path()
            val step = 3f
            val wavelength = 40.dp.toPx()
            val frequency = (2f * Math.PI / wavelength).toFloat()

            var x = 0f
            var first = true

            while (x <= activeWidth) {
                val y = centerY + waveAmplitudePx * sin(frequency * x - phaseOffset)
                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
                x += step
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Composable
fun ExpressiveWavyProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    waveHeight: Dp = 4.dp,
    strokeWidth: Dp = 6.dp
) {
    ExpressiveWavyProgressIndicator(
        progress = progress(),
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        waveHeight = waveHeight,
        strokeWidth = strokeWidth
    )
}
