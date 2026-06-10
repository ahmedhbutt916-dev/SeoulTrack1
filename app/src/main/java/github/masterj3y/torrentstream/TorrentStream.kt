package github.masterj3y.torrentstream

import github.masterj3y.torrentstream.listeners.TorrentListener
import github.masterj3y.torrentstream.model.Torrent

class TorrentStream private constructor(private val options: TorrentOptions) {

    private val listeners = mutableListOf<TorrentListener>()

    companion object {
        fun init(options: TorrentOptions): TorrentStream = TorrentStream(options)
    }

    fun addListener(listener: TorrentListener) {
        listeners.add(listener)
    }

    fun startStream(magnetUri: String) {
        // Stub: In production, this would connect to torrent swarm
    }

    fun stopStream() {
        // Stub: In production, this would stop the torrent stream
    }
}
