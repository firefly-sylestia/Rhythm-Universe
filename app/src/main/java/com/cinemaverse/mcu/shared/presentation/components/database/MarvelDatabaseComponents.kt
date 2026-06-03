package com.cinemaverse.mcu.shared.presentation.components.database

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinemaverse.mcu.shared.presentation.theme.database.LocalMarvelDatabaseTokens
import com.cinemaverse.mcu.shared.presentation.theme.database.MarvelDatabaseTokens
import com.cinemaverse.mcu.shared.presentation.theme.database.MarvelDatabaseUniverse
import com.cinemaverse.mcu.shared.presentation.theme.database.rememberMarvelDatabaseTokens

object MarvelDatabaseMetrics {
    val ScreenHPad = 22.dp
    val CardPad = 18.dp
    val SectionGap = 26.dp
    val RowGap = 10.dp
    val TouchTarget = 48.dp
    val BottomReachPad = 132.dp
}

@Composable
fun MarvelDatabaseScaffold(
    modifier: Modifier = Modifier,
    universe: MarvelDatabaseUniverse = MarvelDatabaseUniverse.Default,
    posterSeed: Color? = null,
    commandBar: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val tokens = rememberMarvelDatabaseTokens(universe = universe, posterSeed = posterSeed)
    CompositionLocalProvider(LocalMarvelDatabaseTokens provides tokens) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(databaseBackground(tokens))
        ) {
            content(PaddingValues(bottom = if (commandBar == null) 0.dp else 86.dp))
            if (commandBar != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    color = tokens.commandPanel,
                    shape = RoundedCornerShape(30.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, tokens.posterPalette.posterOutline.copy(alpha = 0.45f))
                ) { commandBar() }
            }
        }
    }
}

@Composable
fun DatabaseSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    val tokens = LocalMarvelDatabaseTokens.current
    Surface(
        modifier = modifier
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) tokens.posterPalette.posterAccent else tokens.posterPalette.posterOutline.copy(alpha = 0.34f),
                shape = RoundedCornerShape(28.dp)
            ),
        color = if (selected) tokens.posterPalette.posterContainer else tokens.metadataPanel,
        contentColor = if (selected) tokens.posterPalette.posterOnContainer else MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = if (selected) 8.dp else 2.dp
    ) { content() }
}

@Composable
fun DatabaseSectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(LocalMarvelDatabaseTokens.current.posterPalette.posterAccent)
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (!subtitle.isNullOrBlank()) Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun DatabaseCommandBar(
    primaryLabel: String,
    secondaryLabel: String? = null,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onPrimary, modifier = Modifier.weight(1f).height(MarvelDatabaseMetrics.TouchTarget)) { Text(primaryLabel) }
        if (secondaryLabel != null && onSecondary != null) {
            OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f).height(MarvelDatabaseMetrics.TouchTarget)) { Text(secondaryLabel) }
        }
    }
}

@Composable
fun DatabaseCard(
    title: String,
    subtitle: String,
    label: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = androidx.compose.runtime.remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.975f else 1f, tween(160, easing = FastOutSlowInEasing), label = "databaseCardPress")
    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        colors = CardDefaults.cardColors(containerColor = if (selected) LocalMarvelDatabaseTokens.current.posterPalette.posterContainer else LocalMarvelDatabaseTokens.current.metadataPanel),
        border = BorderStroke(1.dp, LocalMarvelDatabaseTokens.current.posterPalette.posterOutline.copy(alpha = if (selected) 0.8f else 0.35f)),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(Modifier.padding(MarvelDatabaseMetrics.CardPad), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (label != null) Text(label, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = LocalMarvelDatabaseTokens.current.posterPalette.posterAccent)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun UniverseBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = LocalMarvelDatabaseTokens.current.universePalette.container,
        contentColor = LocalMarvelDatabaseTokens.current.universePalette.onContainer,
        border = BorderStroke(1.dp, LocalMarvelDatabaseTokens.current.universePalette.outline.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(50)
    ) { Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun TimelineTrack(label: String, progress: Float, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text("${(progress.coerceIn(0f, 1f) * 100).toInt()}% indexed", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
            color = LocalMarvelDatabaseTokens.current.timelineTrack,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun MetadataPanel(title: String, value: String, modifier: Modifier = Modifier) {
    DatabaseSurface(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = LocalMarvelDatabaseTokens.current.posterPalette.posterAccent)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun DatabaseFilterChips(options: List<String>, selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(selected = option == selected, onClick = { onSelected(option) }, label = { Text(option) })
        }
    }
}

private fun databaseBackground(tokens: MarvelDatabaseTokens): Brush = Brush.verticalGradient(
    listOf(
        tokens.posterPalette.posterContainer,
        if (tokens.isDark) Color(0xFF060A10) else Color(0xFFFFFBF5),
        if (tokens.isDark) Color(0xFF101722) else Color(0xFFF6EFE7)
    )
)
