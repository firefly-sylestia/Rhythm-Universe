package com.cinemaverse.mcu.shared.presentation.components.viewing

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cinemaverse.mcu.shared.data.viewing.extractYouTubeVideoId

/**
 * Trusted YouTube IFrame WebView. Google documents the IFrame Player API for embedded playback
 * (https://developers.google.com/youtube/iframe_api_reference), and Android's media troubleshooting
 * guidance points Android YouTube embeds at the IFrame/WebView path rather than deprecated native APIs.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeTrailerWebPlayer(
    youtubeVideoId: String?,
    trailerUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoId = remember(youtubeVideoId, trailerUrl) { youtubeVideoId ?: extractYouTubeVideoId(trailerUrl) }
    if (videoId.isNullOrBlank()) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Trailer unavailable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text("Use metadata refresh later to fetch trailers from TMDb or YouTube search.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                if (!trailerUrl.isNullOrBlank()) {
                    FilledTonalButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))) } }) {
                        Text("Open on YouTube")
                    }
                }
            }
        }
        return
    }

    val html = remember(videoId) { trailerHtml(videoId) }
    var webView: WebView? = remember<WebView?> { null }
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(28.dp)),
        factory = { ctx ->
            WebView(ctx).apply {
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
                webView = this
            }
        },
        update = { it.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null) }
    )
    DisposableEffect(videoId) {
        onDispose {
            webView?.evaluateJavascript("if(window.player){player.pauseVideo();}", null)
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }
}

private fun trailerHtml(videoId: String): String = """
<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1.0"><style>
html,body,#player{margin:0;width:100%;height:100%;background:#000;overflow:hidden;}
</style></head><body><div id="player"></div><script src="https://www.youtube.com/iframe_api"></script><script>
var player;function onYouTubeIframeAPIReady(){player=new YT.Player('player',{videoId:'$videoId',playerVars:{playsinline:1,rel:0,modestbranding:1,autoplay:0},events:{onReady:function(e){}}});}
</script></body></html>
""".trimIndent()
