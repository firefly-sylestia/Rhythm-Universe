package com.cinemaverse.mcu.shared.data.viewing

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository that loads and enriches MCU viewing items from JSON assets and local data.
 * Merges bundled JSON data with curated metadata.
 */
class McuViewingRepository(private val context: Context) {
    companion object {
        private const val TAG = "McuViewingRepository"
    }

    private val dataSource = McuAssetDataSource(context)

    /**
     * Loads all MCU titles from JSON and enriches them with curated data.
     */
    suspend fun loadMcuTitles(): List<ViewingItem> = withContext(Dispatchers.Default) {
        try {
            val jsonItems = dataSource.loadMcuTitles()
            if (jsonItems.isEmpty()) {
                Log.w(TAG, "No items loaded from JSON; falling back to curated lists")
                return@withContext ViewingLists.allItems
            }

            val items = jsonItems.map { json ->
                val jsonItem = dataSource.toViewingItem(json)
                // Try to find matching curated item for enrichment
                val curatedItem = ViewingLists.allItems.find { it.id == jsonItem.id }
                enrichWithCurated(jsonItem, curatedItem)
            }
            Log.i(TAG, "Loaded ${items.size} MCU titles from JSON")
            items
        } catch (e: Exception) {
            Log.e(TAG, "Error loading MCU titles: ${e.message}")
            ViewingLists.allItems
        }
    }

    /**
     * Enriches JSON-loaded item with curated metadata, preferring JSON for artwork.
     */
    private fun enrichWithCurated(
        jsonItem: ViewingItem,
        curatedItem: ViewingItem?
    ): ViewingItem {
        if (curatedItem == null) return jsonItem

        return jsonItem.copy(
            // Prefer curated metadata for these fields
            phase = jsonItem.phase ?: curatedItem.phase,
            order = jsonItem.order ?: curatedItem.order,
            releaseOrder = jsonItem.releaseOrder ?: curatedItem.releaseOrder,
            chronologicalOrder = jsonItem.chronologicalOrder ?: curatedItem.chronologicalOrder,
            phaseOrder = jsonItem.phaseOrder ?: curatedItem.phaseOrder,
            runtime = jsonItem.runtime ?: curatedItem.runtime,
            genres = if (jsonItem.genres.isEmpty()) curatedItem.genres else jsonItem.genres,
            plot = jsonItem.plot ?: curatedItem.plot,
            overview = jsonItem.overview ?: curatedItem.overview,
            director = jsonItem.director ?: curatedItem.director,
            writer = jsonItem.writer ?: curatedItem.writer,
            actors = if (jsonItem.actors.isEmpty()) curatedItem.actors else jsonItem.actors,
            trailerUrl = jsonItem.trailerUrl ?: curatedItem.trailerUrl,
            trailerSource = jsonItem.trailerSource ?: curatedItem.trailerSource,
            // Prefer JSON for poster paths (local assets)
            localPoster = jsonItem.localPoster ?: curatedItem.localPoster,
            localBackdrop = jsonItem.localBackdrop ?: curatedItem.localBackdrop,
            // Preserve curated state but allow JSON override
            tmdbId = jsonItem.tmdbId ?: curatedItem.tmdbId,
            imdbId = jsonItem.imdbId ?: curatedItem.imdbId,
            awards = jsonItem.awards ?: curatedItem.awards,
            language = jsonItem.language ?: curatedItem.language,
            country = jsonItem.country ?: curatedItem.country
        )
    }

    /**
     * Gets poster path priority: local asset → curated local → TMDB → fallback
     */
    fun resolveBestPosterPath(item: ViewingItem): String? {
        // Highest priority: local asset poster
        if (!item.localPoster.isNullOrBlank()) return item.localPoster
        // Fallback: direct poster field or TMDB
        if (!item.poster.isNullOrBlank()) return item.poster
        if (!item.tmdbPoster.isNullOrBlank()) return item.tmdbPoster
        if (!item.omdbPoster.isNullOrBlank()) return item.omdbPoster
        return null
    }

    /**
     * Gets backdrop path priority: local asset → TMDB → fallback
     */
    fun resolveBestBackdropPath(item: ViewingItem): String? {
        if (!item.localBackdrop.isNullOrBlank()) return item.localBackdrop
        if (!item.backdrop.isNullOrBlank()) return item.backdrop
        if (!item.tmdbBackdrop.isNullOrBlank()) return item.tmdbBackdrop
        return null
    }
}
