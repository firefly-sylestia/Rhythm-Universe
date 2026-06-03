package com.cinemaverse.mcu.shared.presentation.theme.database

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/** Premium database identity tokens for the Marvel Database Reactor UI. */
enum class MarvelDatabaseUniverse {
    Avengers, Wakanda, Mystic, Cosmic, TVA, SpiderVerse, StreetLevel, Supernatural, Default
}

@Immutable
data class MarvelUniversePalette(
    val universe: MarvelDatabaseUniverse,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val glow: Color,
    val container: Color,
    val onContainer: Color,
    val outline: Color
)

@Immutable
data class PosterAdaptivePalette(
    val posterPrimary: Color,
    val posterSecondary: Color,
    val posterAccent: Color,
    val posterGlow: Color,
    val posterContainer: Color,
    val posterOnContainer: Color,
    val posterSurface: Color,
    val posterScrim: Color,
    val posterOutline: Color
)

@Immutable
data class MarvelDatabaseTokens(
    val universePalette: MarvelUniversePalette,
    val posterPalette: PosterAdaptivePalette,
    val frostedPanel: Color,
    val commandPanel: Color,
    val metadataPanel: Color,
    val timelineTrack: Color,
    val isDark: Boolean
)

val LocalMarvelDatabaseTokens = compositionLocalOf {
    MarvelDatabaseTokens(
        universePalette = universePaletteFor(MarvelDatabaseUniverse.Default, true),
        posterPalette = posterPaletteFor(MarvelDatabaseUniverse.Default, true),
        frostedPanel = Color(0xFF141821),
        commandPanel = Color(0xFF1B2230),
        metadataPanel = Color(0xFF202736),
        timelineTrack = Color(0xFF4EA8DE),
        isDark = true
    )
}

object MarvelDatabaseTheme {
    val tokens: MarvelDatabaseTokens
        @Composable get() = LocalMarvelDatabaseTokens.current
}

@Composable
fun rememberMarvelDatabaseTokens(
    universe: MarvelDatabaseUniverse = MarvelDatabaseUniverse.Default,
    posterSeed: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme()
): MarvelDatabaseTokens {
    val colorScheme = MaterialTheme.colorScheme
    val universePalette = universePaletteFor(universe, darkTheme)
    val posterPalette = posterPaletteFor(universe, darkTheme, posterSeed)
    val frosted by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF111722) else Color(0xFFF5F1EA),
        animationSpec = tween(220),
        label = "databaseFrostedPanel"
    )
    val command by animateColorAsState(
        targetValue = if (darkTheme) Color(0xFF17202D) else Color(0xFFFFFBF4),
        animationSpec = tween(220),
        label = "databaseCommandPanel"
    )
    val metadata by animateColorAsState(
        targetValue = if (darkTheme) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainer,
        animationSpec = tween(220),
        label = "databaseMetadataPanel"
    )
    return MarvelDatabaseTokens(
        universePalette = universePalette,
        posterPalette = posterPalette,
        frostedPanel = frosted,
        commandPanel = command,
        metadataPanel = metadata,
        timelineTrack = universePalette.accent,
        isDark = darkTheme
    )
}

@Stable
fun universeFor(title: String?, franchise: String?, phaseOrSaga: String? = null): MarvelDatabaseUniverse {
    val text = listOfNotNull(title, franchise, phaseOrSaga).joinToString(" ").lowercase()
    return when {
        listOf("wakanda", "black panther", "vibranium").any { it in text } -> MarvelDatabaseUniverse.Wakanda
        listOf("strange", "wanda", "witch", "mystic", "multiverse").any { it in text } -> MarvelDatabaseUniverse.Mystic
        listOf("guardians", "captain marvel", "eternals", "cosmic", "thor").any { it in text } -> MarvelDatabaseUniverse.Cosmic
        listOf("loki", "tva", "timeline", "chronological", "order").any { it in text } -> MarvelDatabaseUniverse.TVA
        listOf("spider", "daredevil", "defenders", "street").any { it in text } -> MarvelDatabaseUniverse.SpiderVerse
        listOf("blade", "werewolf", "moon knight", "supernatural").any { it in text } -> MarvelDatabaseUniverse.Supernatural
        listOf("avengers", "iron man", "captain america", "hulk").any { it in text } -> MarvelDatabaseUniverse.Avengers
        else -> MarvelDatabaseUniverse.Default
    }
}

fun universePaletteFor(universe: MarvelDatabaseUniverse, dark: Boolean): MarvelUniversePalette = when (universe) {
    MarvelDatabaseUniverse.Avengers -> MarvelUniversePalette(universe, Color(0xFFE53935), Color(0xFFFFC857), Color(0xFF4EA8DE), Color(0x994EA8DE), if (dark) Color(0xFF22151A) else Color(0xFFFFE9E5), if (dark) Color(0xFFFFD9D3) else Color(0xFF2B0808), Color(0xFFE86E5A))
    MarvelDatabaseUniverse.Wakanda -> MarvelUniversePalette(universe, Color(0xFF7C4DFF), Color(0xFF1565C0), Color(0xFFFFD166), Color(0x997C4DFF), if (dark) Color(0xFF171225) else Color(0xFFF2EAFE), if (dark) Color(0xFFE9DDFF) else Color(0xFF180A35), Color(0xFFB693FF))
    MarvelDatabaseUniverse.Mystic -> MarvelUniversePalette(universe, Color(0xFFFF7A1A), Color(0xFF00A6A6), Color(0xFFFFD166), Color(0x99FF7A1A), if (dark) Color(0xFF261714) else Color(0xFFFFEBDD), if (dark) Color(0xFFFFDDC9) else Color(0xFF321003), Color(0xFFFFA15C))
    MarvelDatabaseUniverse.Cosmic -> MarvelUniversePalette(universe, Color(0xFF9C27B0), Color(0xFFE040FB), Color(0xFF00D4FF), Color(0x9900D4FF), if (dark) Color(0xFF1D1530) else Color(0xFFF5E8FF), if (dark) Color(0xFFEAD8FF) else Color(0xFF210733), Color(0xFFB76BFF))
    MarvelDatabaseUniverse.TVA -> MarvelUniversePalette(universe, Color(0xFFFFB000), Color(0xFF607D3B), Color(0xFFD9A441), Color(0x99D9A441), if (dark) Color(0xFF211B12) else Color(0xFFFFF0D1), if (dark) Color(0xFFFFE1AD) else Color(0xFF2E1B00), Color(0xFFD3A34B))
    MarvelDatabaseUniverse.SpiderVerse -> MarvelUniversePalette(universe, Color(0xFFE51B23), Color(0xFF1C5CFF), Color(0xFFFFF4D6), Color(0x99E51B23), if (dark) Color(0xFF21151A) else Color(0xFFFFE8EA), if (dark) Color(0xFFFFD8DC) else Color(0xFF31060A), Color(0xFFEF5D67))
    MarvelDatabaseUniverse.StreetLevel -> MarvelUniversePalette(universe, Color(0xFFC62828), Color(0xFF607D8B), Color(0xFFFFE0B2), Color(0x99C62828), if (dark) Color(0xFF1E1717) else Color(0xFFF8ECE8), if (dark) Color(0xFFFFDAD4) else Color(0xFF2B0B08), Color(0xFFD05F56))
    MarvelDatabaseUniverse.Supernatural -> MarvelUniversePalette(universe, Color(0xFF8B0000), Color(0xFF6A4C93), Color(0xFFCDB4DB), Color(0x998B0000), if (dark) Color(0xFF1F1118) else Color(0xFFF6E7EF), if (dark) Color(0xFFFFD6E6) else Color(0xFF310413), Color(0xFFBC5B79))
    MarvelDatabaseUniverse.Default -> MarvelUniversePalette(universe, Color(0xFFD32F2F), Color(0xFFFFC857), Color(0xFF4EA8DE), Color(0x994EA8DE), if (dark) Color(0xFF121A26) else Color(0xFFFFF4E6), if (dark) Color(0xFFDDEBFF) else Color(0xFF071B2E), Color(0xFF6FB9E8))
}

fun posterPaletteFor(universe: MarvelDatabaseUniverse, dark: Boolean, seed: Color? = null): PosterAdaptivePalette {
    val base = universePaletteFor(universe, dark)
    val primary = seed ?: base.primary
    val container = if (dark) blend(primary, Color.Black, 0.72f) else blend(primary, Color.White, 0.82f)
    val onContainer = if (dark) Color(0xFFFFF7F0) else Color(0xFF15100D)
    return PosterAdaptivePalette(
        posterPrimary = primary,
        posterSecondary = base.secondary,
        posterAccent = base.accent,
        posterGlow = base.glow,
        posterContainer = container,
        posterOnContainer = onContainer,
        posterSurface = if (dark) Color(0xFF111821) else Color(0xFFFFFBF6),
        posterScrim = if (dark) Color(0xCC05070A) else Color(0xDDFDF8F0),
        posterOutline = base.outline
    )
}

private fun blend(foreground: Color, background: Color, amount: Float): Color = Color(
    red = foreground.red * (1f - amount) + background.red * amount,
    green = foreground.green * (1f - amount) + background.green * amount,
    blue = foreground.blue * (1f - amount) + background.blue * amount,
    alpha = 1f
)
