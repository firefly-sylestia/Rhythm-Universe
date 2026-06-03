package chromahub.rhythm.app.shared.data.viewing

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.ZoneOffset

/**
 * Loads bundled Marvel Spectrum metadata and poster references from app assets.
 *
 * Poster files intentionally live under the source poster folder and are exposed as
 * assets by Gradle so their JSON filenames can be used without Android resource renaming.
 */
class McuAssetDataSource(context: Context) {
    private val assets = context.applicationContext.assets
    private val gson = Gson()

    fun loadCatalog(
        curatedItems: List<ViewingItem> = ViewingLists.allItems,
        curatedLists: List<ViewingList> = ViewingLists.allLists
    ): ViewingCatalog {
        val jsonTitles = readTitles()
        val posterMap = readPosterMap()
        val curatedByTitle = curatedItems.associateBy { it.title.normalizedTitleKey() }
        val mergedByTitle = linkedMapOf<String, ViewingItem>()

        curatedItems.forEach { curated ->
            mergedByTitle[curated.title.normalizedTitleKey()] = curated.withoutInvalidLocalArtwork()
        }

        jsonTitles.forEach { json ->
            val key = json.title.normalizedTitleKey()
            val curated = curatedByTitle[key]
            val posterFile = json.posterPath
                ?: posterMap.byId[json.id]
                ?: posterMap.byTitle[json.title]
            val localPoster = posterFile?.toAssetPosterUrlOrNull(json.title)
            val jsonItem = json.toViewingItem(localPoster)
            mergedByTitle[key] = curated?.mergeWithAssetItem(jsonItem) ?: jsonItem
        }

        val mergedItems = mergedByTitle.values.toList().sortedBy { it.order ?: it.releaseOrder ?: Int.MAX_VALUE }
        val itemByTitle = mergedItems.associateBy { it.title.normalizedTitleKey() }
        val mergedLists = curatedLists.map { list ->
            list.copy(
                items = list.items.map { item -> itemByTitle[item.title.normalizedTitleKey()] ?: item.withoutInvalidLocalArtwork() },
                localPoster = list.localPoster.validLocalOverride(),
                localBackdrop = list.localBackdrop.validLocalOverride()
            )
        }

        return ViewingCatalog(
            allItems = mergedItems,
            allLists = mergedLists,
            featuredList = mergedLists.firstOrNull() ?: ViewingLists.featuredList,
            featuredItem = itemByTitle["avengersendgame"] ?: mergedItems.firstOrNull() ?: ViewingLists.featuredItem
        )
    }

    private fun readTitles(): List<McuTitleJson> = runCatching {
        assets.open(MCU_TITLES_PATH).bufferedReader().use { reader ->
            gson.fromJson(reader, Array<McuTitleJson>::class.java).toList()
        }
    }.onFailure { Log.w(TAG, "Unable to read $MCU_TITLES_PATH; curated viewing data will be used.", it) }
        .getOrDefault(emptyList())

    private fun readPosterMap(): PosterMap = runCatching {
        assets.open(POSTERS_PATH).bufferedReader().use { reader ->
            val root = gson.fromJson(reader, JsonObject::class.java)
            PosterMap(
                byId = root.getAsJsonObject("byId")?.entrySet()?.associate { it.key to it.value.asString } ?: emptyMap(),
                byTitle = root.getAsJsonObject("byTitle")?.entrySet()?.associate { it.key to it.value.asString } ?: emptyMap()
            )
        }
    }.onFailure { Log.w(TAG, "Unable to read $POSTERS_PATH; poster lookup will use title records only.", it) }
        .getOrDefault(PosterMap())

    private fun String.toAssetPosterUrlOrNull(title: String): String? {
        val assetPath = "$POSTER_ASSET_DIR/$this"
        return if (assets.exists(assetPath)) {
            "file:///android_asset/$assetPath"
        } else {
            Log.w(TAG, "Missing local poster asset for $title: $assetPath")
            null
        }
    }

    private fun AssetManager.exists(path: String): Boolean = runCatching {
        open(path).close()
    }.isSuccess

    private fun McuTitleJson.toViewingItem(localPoster: String?) = ViewingItem(
        id = id,
        title = title,
        year = releaseDate?.toUtcYear(),
        releaseDate = releaseDate?.toUtcDate(),
        type = type.toViewingType(),
        saga = saga.removeLeadingThe(),
        franchise = series,
        order = viewingOrder,
        releaseOrder = viewingOrder,
        chronologicalOrder = viewingOrder,
        localPoster = localPoster,
        trailerUrl = "https://www.youtube.com/results?search_query=${title.replace(" ", "+")}+trailer",
        trailerSource = TrailerSource.MANUAL
    )

    private fun ViewingItem.mergeWithAssetItem(assetItem: ViewingItem): ViewingItem = copy(
        id = id,
        title = title,
        year = year ?: assetItem.year,
        releaseDate = releaseDate ?: assetItem.releaseDate,
        type = type,
        saga = saga ?: assetItem.saga,
        franchise = franchise ?: assetItem.franchise,
        order = order ?: assetItem.order,
        releaseOrder = releaseOrder ?: assetItem.releaseOrder,
        chronologicalOrder = chronologicalOrder ?: assetItem.chronologicalOrder,
        localPoster = assetItem.localPoster ?: localPoster.validLocalOverride(),
        localBackdrop = localBackdrop.validLocalOverride(),
        trailerUrl = trailerUrl ?: assetItem.trailerUrl,
        trailerSource = trailerSource ?: assetItem.trailerSource
    )

    private fun ViewingItem.withoutInvalidLocalArtwork(): ViewingItem = copy(
        localPoster = localPoster.validLocalOverride(),
        localBackdrop = localBackdrop.validLocalOverride()
    )

    private fun String?.validLocalOverride(): String? = takeIf { value ->
        !value.isNullOrBlank() && !value.contains(PLACEHOLDER_POSTER_PATH) && value != "N/A"
    }

    private fun String?.toViewingType(): ViewingType = when (this?.lowercase()) {
        "series", "tv", "show" -> ViewingType.SERIES
        "episode" -> ViewingType.EPISODE
        "special" -> ViewingType.SPECIAL
        "short" -> ViewingType.SHORT
        else -> ViewingType.MOVIE
    }

    private fun Long.toUtcDate(): String = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toString()
    private fun Long.toUtcYear(): String = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).year.toString()
    private fun String?.removeLeadingThe(): String? = this?.removePrefix("The ")

    private data class McuTitleJson(
        val id: String,
        val title: String,
        val type: String? = "movie",
        val series: String? = null,
        val saga: String? = null,
        val viewingOrder: Int? = null,
        val releaseDate: Long? = null,
        @SerializedName("posterPath") val posterPath: String? = null
    )

    private data class PosterMap(
        val byId: Map<String, String> = emptyMap(),
        val byTitle: Map<String, String> = emptyMap()
    )

    private companion object {
        const val TAG = "McuAssetDataSource"
        const val MCU_TITLES_PATH = "mcu_data/mcu_titles.json"
        const val POSTERS_PATH = "mcu_data/posters.json"
        const val POSTER_ASSET_DIR = "mcu_posters"
        const val PLACEHOLDER_POSTER_PATH = "[I WILL PROVIDE POSTER FOLDER PATH LATER]"
    }
}

data class ViewingCatalog(
    val allItems: List<ViewingItem>,
    val allLists: List<ViewingList>,
    val featuredList: ViewingList,
    val featuredItem: ViewingItem
) {
    fun findItem(id: String): ViewingItem? = allItems.firstOrNull { it.id == id || it.imdbId == id || it.tmdbId?.toString() == id }
    fun findList(id: String): ViewingList? = allLists.firstOrNull { it.id == id }

    fun search(query: String): Pair<List<ViewingItem>, List<ViewingList>> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return allItems.take(8) to allLists.take(6)
        return allItems.filter { item ->
            listOfNotNull(item.title, item.year, item.phase, item.saga, item.franchise, item.director)
                .any { it.lowercase().contains(normalized) } ||
                item.genres.any { it.lowercase().contains(normalized) } ||
                item.actors.any { it.lowercase().contains(normalized) }
        } to allLists.filter { list ->
            listOfNotNull(list.title, list.description, list.phase, list.saga, list.franchise)
                .any { it.lowercase().contains(normalized) } ||
                list.items.any { it.title.lowercase().contains(normalized) }
        }
    }
}

private fun String.normalizedTitleKey(): String = lowercase().filter { it.isLetterOrDigit() }
