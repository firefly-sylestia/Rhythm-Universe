package com.cinemaverse.mcu.shared.data.viewing

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Data source for loading MCU viewing items from bundled JSON assets and poster files.
 * Reads mcu_titles.json and manages local poster asset paths.
 */
class McuAssetDataSource(private val context: Context) {
    companion object {
        private const val TAG = "McuAssetDataSource"
        private const val MCU_TITLES_JSON = "mcu_data/mcu_titles.json"
        private const val POSTERS_JSON = "mcu_data/posters.json"
        private const val POSTERS_DIR = "mcu_posters"
    }

    private val gson = Gson()
    private val assetManager = context.assets

    /**
     * Checks if an asset file exists without throwing exceptions.
     */
    private fun assetExists(path: String): Boolean =
        runCatching { assetManager.open(path).close() }.isSuccess

    /**
     * Loads raw JSON from assets.
     */
    private fun loadJsonString(assetPath: String): String? = runCatching {
        BufferedReader(InputStreamReader(assetManager.open(assetPath))).use { it.readText() }
    }.onFailure { e ->
        Log.w(TAG, "Failed to load $assetPath: ${e.message}")
    }.getOrNull()

    /**
     * Loads MCU titles from mcu_titles.json.
     */
    fun loadMcuTitles(): List<ViewingItemJson> {
        val json = loadJsonString(MCU_TITLES_JSON) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ViewingItemJson>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Invalid JSON in mcu_titles.json: ${e.message}")
            emptyList()
        }
    }

    /**
     * Converts a ViewingItemJson into a ViewingItem with resolved poster paths.
     */
    fun toViewingItem(json: ViewingItemJson): ViewingItem {
        val posterPath = resolvePosterPath(json.posterPath)
        val backdropPath = json.backdropPath?.let { resolvePosterPath(it) }

        return ViewingItem(
            id = json.id,
            title = json.title,
            originalTitle = json.originalTitle,
            type = determineViewingType(json.type),
            saga = json.saga,
            franchise = json.series,
            phase = json.phase,
            order = json.viewingOrder,
            releaseDate = json.releaseDate?.let { formatReleaseDate(it) },
            year = json.releaseDate?.let { extractYear(it) },
            imdbId = json.imdbId,
            tmdbId = json.tmdbId,
            localPoster = posterPath,
            localBackdrop = backdropPath,
            poster = posterPath,
            backdrop = backdropPath,
            plot = json.plot,
            overview = json.overview,
            director = json.director,
            writer = json.writer,
            actors = json.actors ?: emptyList(),
            runtime = json.runtime,
            genres = json.genres ?: emptyList(),
            trailerUrl = json.trailerUrl,
            watched = false,
            favorite = false,
            watchlisted = false
        )
    }

    /**
     * Resolves a poster filename to a file:// URI pointing to the asset.
     * Returns null if the asset doesn't exist.
     */
    private fun resolvePosterPath(filename: String?): String? {
        if (filename.isNullOrBlank()) return null
        val assetPath = "$POSTERS_DIR/$filename"
        return if (assetExists(assetPath)) {
            "file:///android_asset/$assetPath"
        } else {
            Log.w(TAG, "Poster asset not found: $assetPath")
            null
        }
    }

    /**
     * Determines ViewingType from a string like "movie" or "tv_series".
     */
    private fun determineViewingType(typeStr: String?): ViewingType = when (typeStr?.lowercase()) {
        "tv", "tv_series", "series" -> ViewingType.TV_SERIES
        else -> ViewingType.MOVIE
    }

    /**
     * Formats release date from epoch milliseconds to readable date string.
     */
    private fun formatReleaseDate(epochMillis: Long): String {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            dateFormat.format(java.util.Date(epochMillis))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to format date: ${e.message}")
            ""
        }
    }

    /**
     * Extracts year (YYYY) from release date string or epoch milliseconds.
     */
    private fun extractYear(dateOrEpoch: Any?): String? = when (dateOrEpoch) {
        is String -> if (dateOrEpoch.length >= 4) dateOrEpoch.substring(0, 4) else null
        is Long -> {
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = dateOrEpoch }
            calendar.get(java.util.Calendar.YEAR).toString()
        }
        else -> null
    }
}

/**
 * JSON model matching mcu_titles.json structure.
 */
data class ViewingItemJson(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val type: String? = "movie",
    val series: String? = null,
    val saga: String? = null,
    val phase: String? = null,
    val viewingOrder: Int? = null,
    val releaseOrder: Int? = null,
    val chronologicalOrder: Int? = null,
    val releaseDate: Long? = null,
    val imdbId: String? = null,
    val tmdbId: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val plot: String? = null,
    val overview: String? = null,
    val director: String? = null,
    val writer: String? = null,
    val actors: List<String>? = null,
    val runtime: String? = null,
    val genres: List<String>? = null,
    val trailerUrl: String? = null,
    val language: String? = null,
    val country: String? = null
)

enum class ViewingType {
    MOVIE, TV_SERIES
}
