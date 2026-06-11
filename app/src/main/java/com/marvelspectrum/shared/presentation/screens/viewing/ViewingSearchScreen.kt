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
fun ViewingSearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (ViewingItem) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Loading Search",
            message = viewingState.errorMessage ?: "Preparing Cinemaverse search filters and results…",
            modifier = modifier,
            onBack = onBack
        )
        return
    }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedUniverse by rememberSaveable { mutableStateOf("All") }
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf(ViewingSearchCategory.ESSENTIAL) }
    var sortMode by rememberSaveable { mutableStateOf(ViewingSearchSortMode.RELEVANCE) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = data.findItem(selectedItemId)
    if (selectedItem != null) {
        androidx.activity.compose.BackHandler { selectedItemId = null }
        ViewingDetailScreen(item = selectedItem, onBack = { selectedItemId = null }, modifier = modifier, viewingViewModel = viewingViewModel)
        return
    }

    val genres = remember(data) { data.allItems.flatMap { it.genres }.distinct().sorted().take(28) }
    val (rawItems, rawLists) = remember(query, data) { data.search(query) }
    val filteredItems = remember(query, selectedUniverse, selectedType, selectedGenre, selectedCategory, sortMode, rawItems, data) {
        rawItems.asSequence()
            .filter { selectedUniverse == "All" || it.universe == selectedUniverse || (selectedUniverse == "Marvel" && it.universe in setOf("MCU", "Marvel")) || (selectedUniverse == "DC" && it.universe in setOf("DCU", "DCEU", "Elseworlds")) }
            .filter { selectedType == "All" || it.type.name.equals(selectedType, ignoreCase = true) }
            .filter { selectedGenre == null || selectedGenre in it.genres }
            .filter { item ->
                when (selectedCategory) {
                    ViewingSearchCategory.ESSENTIAL -> data.allLists.any { it.importance == ViewingListImportance.PRIMARY && item.id in it.itemIds }
                    ViewingSearchCategory.SERIES -> item.type == ViewingType.SERIES || item.category?.contains("Series", true) == true
                    ViewingSearchCategory.SPECIALS -> item.type in setOf(ViewingType.SPECIAL, ViewingType.SHORT, ViewingType.ONE_SHOT)
                    ViewingSearchCategory.UPCOMING -> item.status != ViewingStatus.RELEASED
                    ViewingSearchCategory.SAVED -> ViewingMetadataStore.statusesFor(item).isNotEmpty()
                    else -> true
                }
            }
            .toList()
            .sortedForSearch(sortMode)
    }
    val matchingLists = remember(rawLists, selectedCategory) {
        rawLists.filter { list ->
            when (selectedCategory) {
                ViewingSearchCategory.ESSENTIAL -> list.importance == ViewingListImportance.PRIMARY
                ViewingSearchCategory.COLLECTIONS -> list.importance in setOf(ViewingListImportance.PRIMARY, ViewingListImportance.SECONDARY)
                ViewingSearchCategory.PHASES -> list.category?.contains("Phase", true) == true || !list.phase.isNullOrBlank()
                ViewingSearchCategory.SAGAS -> !list.saga.isNullOrBlank()
                ViewingSearchCategory.RELEASE_ORDER -> list.category?.contains("Release", true) == true
                ViewingSearchCategory.TIMELINE -> list.category?.contains("Chronological", true) == true
                else -> list.importance != ViewingListImportance.HIDDEN
            }
        }.distinctBy { it.id }.take(8)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Cinemaverse Search") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                actions = { SettingsIconAction(onOpenSettings) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(SpectrumSpacing.screenPadding, 10.dp, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
            verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)
        ) {
            item { ExpressiveSearchField(query, { query = it }) }
            item { SearchChipRail("Universe", listOf("All", "Marvel", "DC", "MCU", "DCEU", "DCU", "Elseworlds"), selectedUniverse) { selectedUniverse = it } }
            item { SearchChipRail("Type", listOf("All", "Movie", "Series", "Special", "Short", "One_Shot"), selectedType) { selectedType = it } }
            item { CategoryChipRail(selectedCategory) { selectedCategory = it } }
            item { SearchCompactFilters(genres, selectedGenre, { selectedGenre = it }, sortMode, { sortMode = it }) }
            val recent = ViewingMetadataStore.recentItems(data).filter { it in filteredItems }.take(5)
            if (recent.isNotEmpty()) item { ResultSection("Recently viewed", "Last opened in Cinemaverse", recent, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
            if (matchingLists.isNotEmpty()) item { ListRail("Matching collections", "Essential and generated orders", matchingLists, onOpenList = {}) }
            val topMatches = filteredItems.filterNot { it in recent }.take(8)
            if (topMatches.isNotEmpty()) item { ResultSection("Top matches", "Best title matches for your filters", topMatches, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
            val remaining = filteredItems.drop(topMatches.size).filterNot { it in recent }
            if (remaining.isNotEmpty()) {
                item { SectionHeader("All results", "${remaining.size} more titles grouped by phase/chapter") }
                groupedViewingItems(remaining, ViewingSortMode.PHASE) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail(item) }
            }
            if (filteredItems.isEmpty() && matchingLists.isEmpty()) item { EmptyState("No viewing results", "Try Marvel, DC, Timeline, Trailers, Specials, or clear a filter.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

internal fun SearchCompactFilters(
    genres: List<String>,
    selectedGenre: String?,
    onGenre: (String?) -> Unit,
    sortMode: ViewingSearchSortMode,
    onSort: (ViewingSearchSortMode) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CompactDropdown(
            label = "Genre",
            selected = selectedGenre ?: "All genres",
            options = listOf<String?>(null) + genres,
            optionLabel = { it ?: "All genres" },
            onSelect = onGenre,
            modifier = Modifier.weight(1f)
        )
        CompactDropdown(
            label = "Sort",
            selected = sortMode.label,
            options = ViewingSearchSortMode.entries,
            optionLabel = { it.label },
            onSelect = onSort,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable

@Composable
internal fun ExpressiveSearchField(query: String, onQuery: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        placeholder = { Text("Search movies, series, phases, genres, cast…") },
        leadingIcon = { Icon(RhythmIcons.Search, contentDescription = null) },
        trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { onQuery("") }) { Icon(RhythmIcons.Close, contentDescription = "Clear search") } },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun SearchChipRail(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
            items(values) { value -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, leadingIcon = if (selected == value) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null, label = { Text(value.replace('_', '-')) }) }
        }
    }
}

@Composable
internal fun GenreChipRail(genres: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Genres", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
            items(genres) { genre -> FilterChip(selected = selected == genre, onClick = { onSelect(genre) }, leadingIcon = { Text("•", color = if (selected == genre) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary) }, label = { Text(genre) }) }
        }
    }
}

@Composable
internal fun CategoryChipRail(selected: ViewingSearchCategory, onSelect: (ViewingSearchCategory) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(ViewingSearchCategory.entries) { category -> FilterChip(selected = selected == category, onClick = { onSelect(category) }, leadingIcon = if (selected == category) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null, label = { Text(category.label) }) }
    }
}

@Composable
internal fun SearchSortRail(sortMode: ViewingSearchSortMode, onSort: (ViewingSearchSortMode) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(ViewingSearchSortMode.entries) { mode -> SpectrumPillTab(selected = sortMode == mode, onClick = { onSort(mode) }) { Text(mode.label) } }
    }
}

@Composable
internal fun ResultSection(title: String, subtitle: String, items: List<ViewingItem>, onOpen: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        items.forEachIndexed { index, item -> ViewingOrderRow(item, index + 1, onClick = { onOpen(item) }) }
    }
}


internal fun List<ViewingItem>.sortedForSearch(mode: ViewingSearchSortMode): List<ViewingItem> = when (mode) {
    ViewingSearchSortMode.RELEASE_DATE -> sortedFor(ViewingSortMode.RELEASE)
    ViewingSearchSortMode.CHRONOLOGICAL -> sortedFor(ViewingSortMode.CHRONOLOGICAL)
    ViewingSearchSortMode.RATING -> sortedFor(ViewingSortMode.RATING)
    ViewingSearchSortMode.RUNTIME -> sortedFor(ViewingSortMode.RUNTIME)
    ViewingSearchSortMode.TITLE -> sortedFor(ViewingSortMode.TITLE)
    ViewingSearchSortMode.RELEVANCE -> this
}


@Composable

