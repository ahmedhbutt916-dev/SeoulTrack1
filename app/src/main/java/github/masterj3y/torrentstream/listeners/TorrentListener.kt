package github.masterj3y.torrentstream.listeners

import github.masterj3y.torrentstream.model.Torrent
import github.masterj3y.torrentstream.model.TorrentStatus

interface TorrentListener {
    fun onStreamReady(torrent: Torrent) {}
    fun onStreamProgress(torrent: Torrent, status: TorrentStatus) {}
    fun onStreamError(torrent: Torrent, e: Exception) {}
    fun onStreamStarted(torrent: Torrent) {}
    fun onStreamStopped() {}
}
