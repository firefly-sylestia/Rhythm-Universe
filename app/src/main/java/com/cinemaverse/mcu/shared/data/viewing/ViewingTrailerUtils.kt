package com.cinemaverse.mcu.shared.data.viewing

import android.net.Uri

fun extractYouTubeVideoId(urlOrId: String?): String? {
    val value = urlOrId?.trim()?.takeIf { it.isNotBlank() } ?: return null
    if (Regex("^[A-Za-z0-9_-]{11}$").matches(value)) return value
    val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return null
    val host = uri.host.orEmpty().lowercase()
    return when {
        host == "youtu.be" -> uri.pathSegments.firstOrNull()
        host.endsWith("youtube.com") && uri.pathSegments.firstOrNull() == "embed" -> uri.pathSegments.getOrNull(1)
        host.endsWith("youtube.com") -> uri.getQueryParameter("v")
        else -> null
    }?.takeIf { Regex("^[A-Za-z0-9_-]{11}$").matches(it) }
}
