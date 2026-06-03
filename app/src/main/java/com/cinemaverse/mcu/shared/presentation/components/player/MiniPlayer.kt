package com.cinemaverse.mcu.shared.presentation.components.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinemaverse.mcu.R
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.theme.database.MarvelDatabaseUniverse
import com.cinemaverse.mcu.shared.presentation.theme.database.rememberMarvelDatabaseTokens

/**
 * Replaces the old music mini-player with a reachable selected-title preview.
 * The legacy Song argument is treated only as an internal compatibility carrier while the UI presents Marvel database actions.
 */
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isMediaLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    val tokens = rememberMarvelDatabaseTokens(MarvelDatabaseUniverse.Default)
    ElevatedCard(
        onClick = onPlayerClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = "Selected Marvel title preview: ${song.title}" },
        colors = CardDefaults.elevatedCardColors(containerColor = tokens.commandPanel),
        border = BorderStroke(1.dp, tokens.posterPalette.posterOutline.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_cinemaverse),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Selected database record", style = MaterialTheme.typography.labelSmall, color = tokens.posterPalette.posterAccent)
                Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOf(song.album, song.artist).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Marvel title metadata" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            AssistChip(onClick = onPlayerClick, label = { Text("Details") })
            IconButton(onClick = onDismiss) { Icon(RhythmIcons.Close, contentDescription = "Close selected title preview") }
        }
        Spacer(Modifier.height(2.dp))
    }
}
