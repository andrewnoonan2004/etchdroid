package eu.depau.etchdroid.ui.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass

/**
 * Responsive layout form factors supporting phones (portrait & landscape)
 * as well as large screens (foldables, tablets, laptops, and desktop displays).
 */
enum class LayoutFormFactor {
    COMPACT,         // Standard phone portrait
    COMPACT_HEIGHT,  // Phone landscape (short height)
    EXPANDED         // Large screens: Tablets, foldables unfolded, laptops, and desktops
}

@Composable
fun rememberLayoutFormFactor(
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
): LayoutFormFactor {
    return remember(windowSizeClass) {
        val isCompactHeight = !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
        val isExpandedWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
        val isMediumWidth = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

        when {
            isExpandedWidth -> LayoutFormFactor.EXPANDED
            isCompactHeight -> LayoutFormFactor.COMPACT_HEIGHT
            isMediumWidth -> LayoutFormFactor.EXPANDED
            else -> LayoutFormFactor.COMPACT
        }
    }
}

@Composable
fun ScreenSizeLayoutSelector(
    modifier: Modifier = Modifier,
    normal: @Composable BoxScope.() -> Unit,
    compact: @Composable BoxScope.() -> Unit,
    expanded: (@Composable BoxScope.() -> Unit)? = null,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val formFactor = rememberLayoutFormFactor(windowSizeClass)

    AnimatedContent(
        targetState = formFactor,
        label = "adaptive layout switch"
    ) { currentFormFactor ->
        Box(modifier) {
            when (currentFormFactor) {
                LayoutFormFactor.EXPANDED -> {
                    if (expanded != null) {
                        expanded()
                    } else {
                        normal()
                    }
                }
                LayoutFormFactor.COMPACT_HEIGHT -> compact()
                LayoutFormFactor.COMPACT -> normal()
            }
        }
    }
}
