package com.cinemaverse.mcu.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Offline-first viewing catalog loader.
 * The app intentionally stores only textual metadata and remote artwork identifiers/URLs here; no
 * poster JPG/PNG/WebP assets are added. TMDb image URLs are resolved at display/cache time, while
 * OMDb remains a text/ratings/poster fallback when a user supplies an optional API key.
 */
object McuAssetDataSource {
    private const val TAG = "ViewingCatalogDataSource"
    private const val MARVEL_CATALOG_PATH = "viewing_catalog_marvel.json"
    private const val DC_CATALOG_PATH = "viewing_catalog_dc.json"
    private const val LISTS_PATH = "viewing_lists.json"

    data class ViewingAssetData(
        val allItems: List<ViewingItem>,
        val allLists: List<ViewingList>,
        val featuredItem: ViewingItem,
        val featuredList: ViewingList
    ) {
        fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
            val normalized = query.trim().lowercase()
            if (normalized.isBlank()) return allItems.take(10) to allLists.take(8)
            return allItems.filter { item ->
                listOfNotNull(
                    item.title, item.originalTitle, item.year, item.releaseDate, item.phase, item.saga,
                    item.franchise, item.universe, item.category, item.director, item.writer, item.imdbId,
                    item.runtime, item.language, item.country
                ).any { it.lowercase().contains(normalized) } ||
                    item.genres.any { it.lowercase().contains(normalized) } ||
                    item.actors.any { it.lowercase().contains(normalized) } ||
                    item.cast.any { it.name.lowercase().contains(normalized) || it.character?.lowercase()?.contains(normalized) == true }
            } to allLists.filter { list ->
                listOfNotNull(list.title, list.description, list.phase, list.saga, list.franchise, list.universe, list.category)
                    .any { it.lowercase().contains(normalized) } ||
                    list.items.any { it.title.lowercase().contains(normalized) }
            }
        }

        fun itemsForList(listId: String): List<ViewingItem> = allLists.firstOrNull { it.id == listId }?.items.orEmpty()
        fun findList(id: String): ViewingList? = allLists.firstOrNull { it.id == id }
        fun findItem(id: String): ViewingItem? = allItems.firstOrNull { it.id == id || it.imdbId == id || it.tmdbId?.toString() == id }
    }

    fun load(context: Context): ViewingAssetData = load(context.assets)

    fun load(assetManager: AssetManager): ViewingAssetData {
        val assetItems = readCatalog(assetManager, MARVEL_CATALOG_PATH) + readCatalog(assetManager, DC_CATALOG_PATH)
        val allItems = (assetItems.ifEmpty { ViewingLists.allItems })
            .distinctBy { it.id }
            .sortedWith(compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE })
        val lists = readLists(assetManager, allItems).ifEmpty { ViewingLists.allLists }
        val featured = allItems.firstOrNull { it.id == "avengers-endgame" } ?: allItems.firstOrNull() ?: ViewingLists.featuredItem
        val featuredList = lists.firstOrNull { it.id == "mcu-release" } ?: lists.firstOrNull() ?: ViewingLists.featuredList
        return ViewingAssetData(allItems, lists, featured, featuredList)
    }

    private fun readCatalog(assetManager: AssetManager, path: String): List<ViewingItem> = runCatching {
        val array = JSONArray(assetManager.readText(path))
        (0 until array.length()).mapNotNull { index -> array.optJSONObject(index)?.toViewingItem() }
    }.getOrElse {
        Log.d(TAG, "Optional catalog asset $path unavailable; using Kotlin seed for that universe.")
        emptyList()
    }

    private fun readLists(assetManager: AssetManager, items: List<ViewingItem>): List<ViewingList> = runCatching {
        val byId = items.associateBy { it.id }
        val array = JSONArray(assetManager.readText(LISTS_PATH))
        (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let { json ->
                val ids = json.optStringList("itemIds")
                val resolved = ids.mapNotNull { byId[it] }
                ViewingList(
                    id = json.optString("id"),
                    title = json.optString("title"),
                    description = json.optString("description").takeUsable(),
                    phase = json.optString("phase").takeUsable(),
                    saga = json.optString("saga").takeUsable(),
                    franchise = json.optString("franchise").takeUsable(),
                    universe = json.optString("universe").takeUsable(),
                    category = json.optString("category").takeUsable(),
                    itemIds = ids,
                    items = resolved
                )
            }
        }.filter { it.items.isNotEmpty() }
    }.getOrElse { emptyList() }

    private fun JSONObject.toViewingItem(): ViewingItem = ViewingItem(
        id = optString("id"),
        title = optString("title"),
        originalTitle = optString("originalTitle").takeUsable(),
        year = optString("year").takeUsable() ?: optString("releaseDate").takeUsable()?.take(4),
        releaseDate = optString("releaseDate").takeUsable(),
        imdbId = optString("imdbId").takeUsable(),
        tmdbId = optInt("tmdbId").takeIf { it > 0 },
        type = optString("type").toViewingType(),
        phase = optString("phase").takeUsable(),
        saga = optString("saga").takeUsable(),
        franchise = optString("franchise").takeUsable(),
        studio = optString("studio").takeUsable(),
        universe = optString("universe").takeUsable(),
        category = optString("category").takeUsable(),
        releaseOrder = optInt("releaseOrder").takeIf { it > 0 },
        chronologicalOrder = optInt("chronologicalOrder").takeIf { it > 0 },
        phaseOrder = optInt("phaseOrder").takeIf { it > 0 },
        collectionOrder = optInt("collectionOrder").takeIf { it > 0 },
        runtime = optString("runtime").takeUsable(),
        genres = optStringList("genres"),
        plot = optString("plot").takeUsable(),
        overview = optString("overview").takeUsable(),
        poster = optString("poster").takeUsable(),
        tmdbPoster = optString("tmdbPoster").takeUsable(),
        omdbPoster = optString("omdbPoster").takeUsable(),
        backdrop = optString("backdrop").takeUsable(),
        tmdbBackdrop = optString("tmdbBackdrop").takeUsable(),
        trailerUrl = optString("trailerUrl").takeUsable(),
        youtubeVideoId = optString("youtubeVideoId").takeUsable(),
        trailerSource = optString("trailerSource").takeUsable()?.let { runCatching { TrailerSource.valueOf(it) }.getOrNull() },
        director = optString("director").takeUsable(),
        writer = optString("writer").takeUsable(),
        actors = optStringList("actors"),
        imdbRating = optString("imdbRating").takeUsable(),
        tmdbRating = optDouble("tmdbRating").takeIf { it > 0.0 },
        language = optString("language").takeUsable(),
        country = optString("country").takeUsable(),
        watchProviders = optStringList("watchProviders"),
        status = optString("status").takeUsable()?.let { runCatching { ViewingStatus.valueOf(it) }.getOrNull() } ?: ViewingStatus.RELEASED
    )

    private fun JSONObject.optStringList(key: String): List<String> {
        val array = optJSONArray(key) ?: return optString(key).takeUsable()?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
        return (0 until array.length()).mapNotNull { array.optString(it).takeUsable() }
    }

    private fun String.toViewingType(): ViewingType = when (lowercase()) {
        "series", "tv", "show" -> ViewingType.SERIES
        "episode" -> ViewingType.EPISODE
        "special" -> ViewingType.SPECIAL
        "short" -> ViewingType.SHORT
        "one_shot", "one-shot" -> ViewingType.ONE_SHOT
        else -> ViewingType.MOVIE
    }

    private fun AssetManager.readText(path: String): String = open(path).bufferedReader().use { it.readText() }
    private fun String.takeUsable(): String? = takeIf { it.isNotBlank() && it != "N/A" && it != "null" }
}
