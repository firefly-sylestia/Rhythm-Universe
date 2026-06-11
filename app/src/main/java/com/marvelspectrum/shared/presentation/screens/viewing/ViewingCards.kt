@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.marvelspectrum.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marvelspectrum.R
import com.marvelspectrum.shared.data.service.ViewingMetadataStore
import com.marvelspectrum.shared.data.viewing.McuAssetDataSource
import com.marvelspectrum.shared.data.viewing.WatchProvider
import com.marvelspectrum.shared.data.viewing.ViewingCastMember
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingList
import com.marvelspectrum.shared.data.viewing.ViewingListImportance
import com.marvelspectrum.shared.data.viewing.ViewingSearchCategory
import com.marvelspectrum.shared.data.viewing.ViewingSearchSortMode
import com.marvelspectrum.shared.data.viewing.ViewingSortMode
import com.marvelspectrum.shared.data.viewing.ViewingStatus
import com.marvelspectrum.shared.data.viewing.ViewingType
import com.marvelspectrum.shared.data.viewing.ViewingUserStatus
import com.marvelspectrum.shared.data.viewing.ViewingTrailer
import com.marvelspectrum.shared.presentation.components.icons.Icon
import com.marvelspectrum.shared.presentation.components.icons.MaterialSymbolIcon
import com.marvelspectrum.shared.presentation.components.icons.RhythmIcons
import com.marvelspectrum.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.marvelspectrum.shared.presentation.viewmodel.ViewingViewModel
import com.marvelspectrum.shared.util.ViewingArtworkUtils
import kotlinx.coroutines.delay

@Composable
internal fun PosterCard(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    PressableCard(onClick = onClick, modifier = Modifier.width(ViewingUi.posterWidth)) {
        PosterBackdrop(displayItem, Modifier.fillMaxWidth().aspectRatio(2f / 3f), ContentScale.Crop, SpectrumShapes.posterMask)
        Spacer(Modifier.height(10.dp))
        Text(displayItem.title, modifier = Modifier.height(40.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(listOfNotNull(displayItem.year, displayItem.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        CollectionArtwork(list, Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
        Spacer(Modifier.height(10.dp))
        Text(list.title, modifier = Modifier.height(40.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        Text("${list.items.size} titles • ${list.category ?: list.universe ?: "Viewing order"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun WideListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionArtwork(list, Modifier.size(64.dp, 88.dp).clip(RoundedCornerShape(18.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(list.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${list.items.size} titles • ${list.category ?: "Collection"}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}


@Composable
internal fun rememberEnrichedItem(item: ViewingItem): ViewingItem {
    var current by remember(item.id) { mutableStateOf(ViewingMetadataStore.itemFor(item)) }
    LaunchedEffect(item.id) {
        current = ViewingMetadataStore.itemFor(item)
        current = runCatching { ViewingMetadataStore.enrich(item) }.getOrElse { current }
    }
    return if (ViewingMetadataStore.useLocalPosters.value) current else current.copy(localPoster = null, localBackdrop = null)
}


@Composable
internal fun CollectionArtwork(list: ViewingList, modifier: Modifier, contentScale: ContentScale) {
    val preferLocalArtwork = ViewingMetadataStore.useLocalPosters.value
    val direct = ViewingArtworkUtils.resolveCollectionBackdrop(list, preferLocalArtwork)
    val artworkItems = remember(list.id, list.artworkItems, list.items, preferLocalArtwork) {
        list.artworkItems.ifEmpty { list.items }.filter { ViewingArtworkUtils.resolveCardPoster(it, preferLocalArtwork) != null }.take(4)
    }
    when {
        direct != null -> ArtworkImage(direct, "${list.title} artwork", modifier, contentScale, fallbackTitle = list.title, fallbackLabel = list.accentLabel ?: list.category ?: list.universe)
        artworkItems.size >= 2 -> CollectionArtworkMosaic(list, artworkItems, modifier)
        else -> BrandedArtworkPlaceholder(list.title, list.accentLabel ?: list.category ?: list.universe ?: "Collection", modifier, list.universe)
    }
}

@Composable
internal fun CollectionArtworkMosaic(list: ViewingList, items: List<ViewingItem>, modifier: Modifier) {
    Column(modifier.background(collectionBrush(list.universe ?: list.category))) {
        items.chunked(2).take(2).forEach { rowItems ->
            Row(Modifier.weight(1f)) {
                rowItems.forEach { item ->
                    PosterBackdrop(item, Modifier.weight(1f).fillMaxSize(), ContentScale.Crop)
                }
                if (rowItems.size == 1) BrandedArtworkPlaceholder(list.title, list.accentLabel ?: "Collection", Modifier.weight(1f).fillMaxSize(), list.universe)
            }
        }
    }
}

@Composable
internal fun BrandedArtworkPlaceholder(title: String, label: String?, modifier: Modifier, universe: String? = null) {
    Box(modifier.background(collectionBrush(universe ?: label)).semantics { contentDescription = "Artwork placeholder for $title" }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            Text(title.initials(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center, maxLines = 1)
            if (!label.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Image(painterResource(R.drawable.ic_cinemaverse), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(28.dp).graphicsLayer { alpha = 0.42f })
    }
}

@Composable
internal fun collectionBrush(seed: String?): Brush {
    val scheme = MaterialTheme.colorScheme
    val colors = when (seed) {
        "MCU", "Marvel" -> listOf(scheme.primaryContainer, scheme.tertiaryContainer)
        "DCU", "DCEU", "Elseworlds", "DC" -> listOf(scheme.secondaryContainer, scheme.primaryContainer)
        else -> listOf(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer)
    }
    return Brush.linearGradient(colors)
}

internal fun String.initials(): String = trim().split(Regex("\\s+")).filter { it.isNotBlank() && it.first().isLetterOrDigit() }.take(3).joinToString("") { it.first().uppercase() }.ifBlank { "CU" }

internal enum class ArtworkDisplayIntent { CARD_POSTER, HERO_BACKDROP }

@Composable
internal fun PosterBackdrop(
    item: ViewingItem,
    modifier: Modifier,
    contentScale: ContentScale,
    shape: Shape? = null,
    intent: ArtworkDisplayIntent = ArtworkDisplayIntent.CARD_POSTER
) {
    val m = if (shape != null) modifier.clip(shape) else modifier
    val preferLocalArtwork = ViewingMetadataStore.useLocalPosters.value
    val artwork = when (intent) {
        ArtworkDisplayIntent.CARD_POSTER -> ViewingArtworkUtils.resolveCardPoster(item, preferLocalArtwork)
        ArtworkDisplayIntent.HERO_BACKDROP -> ViewingArtworkUtils.resolveHeroBackdrop(item, preferLocalArtwork)
    }
    ArtworkImage(
        artwork,
        "Poster for ${item.title}",
        m,
        contentScale,
        fallbackTitle = item.title,
        fallbackLabel = item.universe ?: item.category ?: item.type.name.lowercase().replaceFirstChar { it.titlecase() },
        fallbackSeed = item.universe
    )
}

@Composable
internal fun ArtworkImage(data: String?, description: String, modifier: Modifier, contentScale: ContentScale, fallbackTitle: String? = null, fallbackLabel: String? = null, fallbackSeed: String? = null) {
    var loadFailed by remember(data) { mutableStateOf(false) }
    if (data.isNullOrBlank() || loadFailed) {
        BrandedArtworkPlaceholder(fallbackTitle ?: description.removePrefix("Poster for "), fallbackLabel, modifier.semantics { contentDescription = description }, fallbackSeed ?: fallbackLabel)
    } else {
        val context = LocalContext.current
        val request = remember(data) { ImageRequest.Builder(context).data(data).crossfade(true).memoryCacheKey(data).diskCacheKey(data).build() }
        AsyncImage(
            model = request,
            contentDescription = description,
            contentScale = contentScale,
            modifier = modifier,
            onError = { loadFailed = true }
        )
    }
}

@Composable
internal fun SettingsIconAction(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) { Icon(RhythmIcons.Settings, contentDescription = "Settings") }
}
