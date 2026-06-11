@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.marvelspectrum.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.core.animateDpAsState
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
fun ViewingHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    openSearchInternally: Boolean = true,
    homeReselectionKey: Int = 0,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Preparing Cinemaverse",
            message = viewingState.errorMessage ?: "Loading your Marvel and DC viewing catalog…",
            modifier = modifier
        )
        return
    }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(homeReselectionKey) {
        selectedItemId = null
        selectedListId = null
        showSearch = false
    }
    val selectedItem = data.findItem(selectedItemId)
    val selectedList = data.findList(selectedListId)

    androidx.activity.compose.BackHandler(enabled = selectedItem != null || selectedList != null || showSearch) {
        when {
            selectedItem != null -> selectedItemId = null
            selectedList != null -> selectedListId = null
            showSearch -> showSearch = false
        }
    }

    when {
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null }, viewingViewModel = viewingViewModel)
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id })
        showSearch -> ViewingSearchScreen(onBack = { showSearch = false }, onOpenDetail = { selectedItemId = it.id }, onOpenSettings = onOpenSettings, viewingViewModel = viewingViewModel)
        else -> ViewingHomeContent(
            data = data,
            onOpenLibrary = onOpenLibrary,
            onOpenSearch = {
                if (openSearchInternally) {
                    showSearch = true
                } else {
                    onOpenSearch()
                }
            },
            onOpenSettings = onOpenSettings,
            onOpenItem = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail() },
            onOpenList = { selectedListId = it.id },
            modifier = modifier
        )
    }
}

@Composable
internal fun ViewingHomeContent(
    data: McuAssetDataSource.ViewingAssetData,
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenItem: (ViewingItem) -> Unit,
    onOpenList: (ViewingList) -> Unit,
    modifier: Modifier = Modifier
) {
    val marvel = remember(data) { data.allItems.filter { it.universe == "MCU" }.take(14) }
    val dc = remember(data) { data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }.take(14) }
    val lists = remember(data) { data.allLists.visibleManagedLists().take(8) }
    val recent = ViewingMetadataStore.recentItems(data)
    val continueItems = remember(data, recent) {
        (recent + data.allItems.filter { item ->
            val statuses = ViewingMetadataStore.statusesFor(item)
            ViewingUserStatus.WATCHING in statuses || ViewingUserStatus.WATCH_LATER in statuses
        }).distinctBy { it.id }.take(12)
    }
    val trailerItems = remember(data) { data.allItems.filter { it.hasAnyTrailer() }.take(16) }
    val upcomingItems = remember(data) { data.allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(14) }
    val becauseYouWatched = remember(data, recent) {
        val last = recent.firstOrNull()
        if (last == null) emptyList() else data.allItems.filter { item ->
            item.id != last.id && (item.universe == last.universe || item.franchise == last.franchise || item.saga == last.saga || item.genres.any(last.genres::contains))
        }.take(12)
    }
    var trailerPreview by remember { mutableStateOf<ViewingItem?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(SpectrumSpacing.screenPadding, ViewingUi.topPad, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
        verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.sectionGap)
    ) {
        item { CinemaverseHeader(title = "Marvel Spectrum", subtitle = "Your cinematic universe, beautifully organized", onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings) }
        item { LibraryTabs("Continue", onTab = { onOpenLibrary() }) }
        item { SectionIdentityBlock(RhythmIcons.Play, "Continue your spectrum", "${continueItems.size} ready", "Recent activity, saved titles, and the stories waiting for you") }
        item {
            FeaturedTitleCarousel(
                items = remember(data) { data.homeFeaturedTitles() },
                featuredList = data.featuredList,
                onOpenItem = onOpenItem,
                onOpenLibrary = onOpenLibrary
            )
        }
        if (recent.isNotEmpty()) item { CinemaActivityMiniSurface(recent.first(), onClick = { onOpenItem(recent.first()) }) }
        if (continueItems.isNotEmpty()) item { PosterRail("Continue watching", "Recently opened, watching, and watch-later titles", continueItems, onOpenItem) }
        if (trailerItems.isNotEmpty()) item { TrailerRail("Trailers", "Preview the latest available title trailers", trailerItems, onOpenTrailer = { trailerPreview = it }) }
        if (upcomingItems.isNotEmpty()) item { PosterRail("Upcoming", "Announced and upcoming Cinemaverse releases", upcomingItems, onOpenItem) }
        if (becauseYouWatched.isNotEmpty()) item { PosterRail("Because you watched ${recent.first().title}", recent.first().universe ?: recent.first().franchise ?: "Personal picks", becauseYouWatched, onOpenItem) }
        item { PosterRail("MCU", "Marvel Studios films, shows, specials, One-Shots, and Defenders", marvel, onOpenItem) }
        item { PosterRail("DC", "DCU, DCEU, Elseworlds, and connected TV", dc, onOpenItem) }
        item { ListRail("Managed collections", "Essentials, timelines, chapters, and character journeys", lists, onOpenList) }
    }
    trailerPreview?.let { item -> TrailerPlayerDialog(item = item, onOpenDetails = { trailerPreview = null; onOpenItem(item) }, onDismiss = { trailerPreview = null }) }
}

@Composable

internal fun CinemaverseHeader(title: String = "Marvel Spectrum", subtitle: String = "Marvel • DC • Release orders • Trailers", onOpenSearch: (() -> Unit)? = null, onOpenSettings: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SpectrumGlassSurface(shape = RoundedCornerShape(30.dp)) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Image(painter = painterResource(R.drawable.ic_cinemaverse), contentDescription = "Marvel Spectrum", modifier = Modifier.size(52.dp).padding(11.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Explore your spectrum", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Search, filter, and tune your view", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (onOpenSearch != null) SpectrumIconButton(onClick = onOpenSearch) { Icon(RhythmIcons.Search, contentDescription = "Search Marvel Spectrum") }
                SettingsIconAction(onOpenSettings)
            }
        }
    }
}

@Composable
internal fun CollectionAlbumHero(list: ViewingList, onPlayFirst: () -> Unit, onShuffle: () -> Unit) {
    val watchedCount = list.items.count { ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it) }
    var expanded by remember { mutableStateOf(false) }
    val poster = ViewingArtworkUtils.resolveCollectionBackdrop(list, ViewingMetadataStore.useLocalPosters.value)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(34.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth()) {
            ArtworkImage(poster, "${list.title} background", Modifier.matchParentSize(), ContentScale.Crop)
            Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = .82f)))
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    CollectionArtwork(list, Modifier.size(132.dp).clip(RoundedCornerShape(38.dp)), ContentScale.Crop)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(list.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(list.category ?: list.universe ?: "Cinemaverse collection", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$watchedCount of ${list.items.size} watched", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CollectionPill("${list.items.size} titles")
                            CollectionPill(list.phase ?: list.saga ?: "Curated")
                        }
                    }
                    Box {
                        FilledIconButton(
                            onClick = { expanded = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) { Icon(RhythmIcons.More, contentDescription = "More actions for ${list.title}") }
                        SpectrumPopupMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            SpectrumPopupMenuItem("Save list", leading = { Icon(MaterialSymbolIcon("bookmark", filled = true), null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.BOOKMARKED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.BOOKMARKED) } }
                            SpectrumPopupMenuItem("Mark all watched", leading = { Icon(RhythmIcons.Check, null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } }
                            SpectrumPopupMenuItem("Clear watched", leading = { Icon(RhythmIcons.Remove, null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } }
                        }
                    }
                }
                Text(list.description.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onPlayFirst, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) {
                        Icon(RhythmIcons.Play, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start")
                    }
                    OutlinedButton(onClick = onShuffle, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) {
                        Icon(RhythmIcons.Shuffle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                }
            }
        }
    }
}

@Composable
internal fun CollectionPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
internal fun CollectionTrackHeader(count: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Titles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("$count total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(count.toString(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
internal fun CollectionTitleRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    var expanded by remember { mutableStateOf(false) }
    var showTrailer by rememberSaveable(displayItem.id) { mutableStateOf(false) }
    val hasTrailer = displayItem.hasAnyTrailer()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(order.toString().padStart(2, '0'), modifier = Modifier.width(32.dp).semantics { contentDescription = "Number $order in this collection" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PosterBackdrop(displayItem, Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayItem.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(listOfNotNull(displayItem.runtime, displayItem.year, displayItem.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box {
                FilledIconButton(
                    onClick = { expanded = true },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Icon(RhythmIcons.More, contentDescription = "More actions for ${displayItem.title}") }
                SpectrumPopupMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SpectrumPopupMenuItem("Open details", leading = { Icon(RhythmIcons.Info, null) }) { expanded = false; onClick() }
                    if (hasTrailer) SpectrumPopupMenuItem("Play trailer", leading = { Icon(RhythmIcons.Play, null) }) { expanded = false; showTrailer = true }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    SpectrumPopupMenuItem("Add to watchlist", leading = { Icon(RhythmIcons.Add, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHLIST) }
                    SpectrumPopupMenuItem("Mark watched", leading = { Icon(RhythmIcons.Check, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHED) }
                    SpectrumPopupMenuItem("Favorite", leading = { Icon(RhythmIcons.Favorite, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.FAVORITE) }
                    SpectrumPopupMenuItem("Hide", leading = { Icon(RhythmIcons.VisibilityOff, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.HIDDEN) }
                }
            }
        }
    }
    if (showTrailer) TrailerPlayerDialog(item = displayItem, onOpenDetails = { showTrailer = false; onClick() }, onDismiss = { showTrailer = false })
}

@Composable
internal fun FeaturedTitleCarousel(
    items: List<ViewingItem>,
    featuredList: ViewingList,
    onOpenItem: (ViewingItem) -> Unit,
    onOpenLibrary: () -> Unit
) {
    val carouselItems = if (items.isEmpty()) featuredList.items.take(6) else items
    val listState = rememberLazyListState()
    var selectedIndex by rememberSaveable(carouselItems.map { it.id }.joinToString()) { mutableStateOf(0) }
    var interactionPaused by rememberSaveable { mutableStateOf(false) }
    val visibleItem = carouselItems.getOrNull(selectedIndex) ?: return

    LaunchedEffect(listState.firstVisibleItemIndex, listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            interactionPaused = true
            selectedIndex = listState.firstVisibleItemIndex.coerceIn(0, carouselItems.lastIndex)
        } else if (interactionPaused) {
            delay(2_400)
            interactionPaused = false
        }
    }
    LaunchedEffect(carouselItems, selectedIndex, interactionPaused) {
        if (carouselItems.size <= 1 || interactionPaused) return@LaunchedEffect
        delay(6_000)
        selectedIndex = (selectedIndex + 1) % carouselItems.size
    }
    LaunchedEffect(selectedIndex) {
        if (carouselItems.isNotEmpty() && !listState.isScrollInProgress && listState.firstVisibleItemIndex != selectedIndex) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpectrumSectionHeader("Featured titles", subtitle = "Swipe the cinematic spectrum • tap a scene for details")
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap),
            modifier = Modifier.semantics { contentDescription = "Gesture-first featured title carousel" }
        ) {
            itemsIndexed(carouselItems, key = { index, item -> "featured-${item.id}-$index" }) { index, item ->
                FeaturedTitleCarouselCard(
                    item = item,
                    list = featuredList,
                    selected = index == selectedIndex,
                    position = index + 1,
                    total = carouselItems.size,
                    onClick = {
                        interactionPaused = true
                        selectedIndex = index
                        onOpenItem(item)
                    },
                    modifier = Modifier.fillParentMaxWidth(0.90f)
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.carouselIndicatorGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            carouselItems.forEachIndexed { index, _ ->
                val width by animateDpAsState(
                    if (index == selectedIndex) SpectrumSpacing.carouselIndicatorActiveWidth else SpectrumSpacing.carouselIndicatorInactiveWidth,
                    label = "featuredIndicatorWidth"
                )
                Surface(
                    modifier = Modifier.size(width = width, height = SpectrumSpacing.carouselIndicatorHeight),
                    shape = RoundedCornerShape(50),
                    color = if (index == selectedIndex) spectrumUniverseAccent(visibleItem.universe).primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                ) {}
            }
        }
        Text(
            "Featured ${selectedIndex + 1} of ${carouselItems.size}: ${visibleItem.title}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun FeaturedTitleCarouselCard(
    item: ViewingItem,
    list: ViewingList,
    selected: Boolean,
    position: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayItem = rememberEnrichedItem(item)
    val accent = spectrumUniverseAccent(displayItem.universe)
    Card(
        onClick = onClick,
        shape = SpectrumShapes.mediaFrame,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.semantics {
            contentDescription = "Featured title $position of $total: ${displayItem.title}. Open details for ${displayItem.title}"
        }
    ) {
        Box(Modifier.fillMaxWidth().height(350.dp).clip(SpectrumShapes.mediaFrame)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
            UniverseGlow(displayItem.universe, Modifier.matchParentSize())
            Box(Modifier.matchParentSize().background(readabilityGradient(strong = true)))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpectrumPulseIndicator(active = selected, universe = displayItem.universe)
                    SpectrumArtworkPill(displayItem.universe ?: "Spectrum premiere")
                }
                Text(displayItem.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.phase ?: displayItem.saga, list.title).joinToString(" • "), color = Color.White.copy(alpha = .84f), maxLines = 2)
                SpectrumRhythmDivider(universe = displayItem.universe, bars = 16)
            }
        }
    }
}


internal fun McuAssetDataSource.ViewingAssetData.homeFeaturedTitles(): List<ViewingItem> {
    val essentials = allLists.firstOrNull { it.id == "mcu-release-order" }?.items.orEmpty().take(5)
    val dcHighlights = allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") && it.status == ViewingStatus.RELEASED }.take(4)
    val trailerReady = allItems.filter { it.status == ViewingStatus.RELEASED && (it.hasAnyTrailer()) && ViewingArtworkUtils.resolveCardPoster(it) != null }.take(6)
    val upcoming = allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(3)
    return (listOf(featuredItem) + trailerReady + essentials + dcHighlights + upcoming).distinctBy { it.id }.take(10)
}

internal fun Int.floorMod(size: Int): Int = ((this % size) + size) % size


@Composable
internal fun PosterRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
            items(items, key = { it.id }) { PosterCard(it) { onOpenItem(it) } }
        }
    }
}


@Composable
internal fun ListRail(title: String, subtitle: String, lists: List<ViewingList>, onOpenList: (ViewingList) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) { items(lists, key = { it.id }) { ViewingListCard(it) { onOpenList(it) } } }
    }
}


@Composable
internal fun CinemaActivityMiniSurface(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(104.dp)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f), MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f), Color.Transparent))))
            Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PosterBackdrop(displayItem, Modifier.size(50.dp, 72.dp), ContentScale.Crop, RoundedCornerShape(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recently viewed", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(displayItem.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Text(ViewingMetadataStore.statusesFor(displayItem).firstOrNull()?.activeLabel ?: "Open details", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
                FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                    Icon(RhythmIcons.Play, contentDescription = "Resume ${displayItem.title}")
                }
            }
        }
    }
}
