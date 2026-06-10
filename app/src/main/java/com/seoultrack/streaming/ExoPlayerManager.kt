package com.seoultrack.streaming

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultDataSource

/**
 * Thin wrapper around ExoPlayer for SeoulTrack.
 *
 * Supports:
 *  - HLS streams (.m3u8) — used by ezvidapi and other streaming APIs
 *  - Direct file:// URLs — output from TorrentStreamManager
 *  - HTTP MP4/MKV/WebM direct streams
 *
 * The ExoPlayer instance is created once and reused across episodes.
 */
class ExoPlayerManager(context: Context) {

    val player: ExoPlayer = ExoPlayer.Builder(context).build()

    fun playUrl(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun playHls(hlsUrl: String, context: Context) {
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val hlsSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(hlsUrl))
        player.setMediaSource(hlsSource)
        player.prepare()
        player.play()
    }

    fun pause()  { player.pause() }
    fun resume() { player.play() }
    fun seekTo(posMs: Long) { player.seekTo(posMs) }

    val isPlaying get() = player.isPlaying
    val duration  get() = player.duration
    val position  get() = player.currentPosition

    fun release() { player.release() }
}
