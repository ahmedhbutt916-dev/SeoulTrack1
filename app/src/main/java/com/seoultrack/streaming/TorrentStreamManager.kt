package com.seoultrack.streaming

import android.content.Context
import android.util.Log
import github.masterj3y.torrentstream.TorrentStream
import github.masterj3y.torrentstream.TorrentOptions
import github.masterj3y.torrentstream.listeners.TorrentListener
import github.masterj3y.torrentstream.model.Torrent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "TorrentStreamManager"

sealed class StreamState {
    object Idle : StreamState()
    data class Preparing(val progress: Int) : StreamState()       // 0–100
    data class Ready(val streamUrl: String) : StreamState()        // hand to ExoPlayer
    data class Error(val message: String) : StreamState()
}

/**
 * Wraps TorrentStream-Android to resolve magnet links → local HTTP stream URL.
 *
 * TorrentStream-Android uses libtorrent under the hood, so it connects to
 * the FULL torrent swarm (TCP + UDP DHT) — no WebRTC peer limitation.
 *
 * Usage:
 *   torrentStreamManager.stream(magnetUri).collect { state ->
 *     when (state) {
 *       is StreamState.Ready -> exoPlayer.setMediaItem(MediaItem.fromUri(state.streamUrl))
 *       is StreamState.Preparing -> showProgress(state.progress)
 *       is StreamState.Error -> showError(state.message)
 *     }
 *   }
 */
class TorrentStreamManager(context: Context) {

    private val torrentStream: TorrentStream

    init {
        val options = TorrentOptions.Builder()
            .saveLocation(context.filesDir.absolutePath + "/torrents")
            .removeFilesAfterStop(true)
            .build()

        torrentStream = TorrentStream.init(options)
    }

    fun stream(magnetUri: String): Flow<StreamState> = callbackFlow {
        trySend(StreamState.Preparing(0))

        torrentStream.addListener(object : TorrentListener {
            override fun onStreamReady(torrent: Torrent) {
                Log.d(TAG, "Stream ready: ${torrent.videoFile?.absolutePath}")
                // TorrentStream serves a local HTTP server; get the URL
                val url = torrent.videoFile?.absolutePath?.let { "file://$it" }
                    ?: run {
                        trySend(StreamState.Error("No video file found in torrent"))
                        return
                    }
                trySend(StreamState.Ready(url))
            }

            override fun onStreamProgress(torrent: Torrent, status: github.masterj3y.torrentstream.model.TorrentStatus) {
                val pct = (status.progress * 100).toInt()
                Log.d(TAG, "Progress: $pct% | Seeds: ${status.seeds} | DL: ${status.downloadSpeed} B/s")
                trySend(StreamState.Preparing(pct))
            }

            override fun onStreamError(torrent: Torrent, e: Exception) {
                Log.e(TAG, "Stream error: ${e.message}")
                trySend(StreamState.Error(e.message ?: "Unknown torrent error"))
            }

            override fun onStreamStarted(torrent: Torrent) {
                Log.d(TAG, "Stream started: ${torrent.magnetUri}")
            }

            override fun onStreamStopped() {
                Log.d(TAG, "Stream stopped")
            }
        })

        torrentStream.startStream(magnetUri)

        awaitClose {
            torrentStream.stopStream()
        }
    }

    fun stop() {
        torrentStream.stopStream()
    }
}
