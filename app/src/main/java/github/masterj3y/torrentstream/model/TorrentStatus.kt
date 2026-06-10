package github.masterj3y.torrentstream.model

data class TorrentStatus(
    val progress: Float = 0f,
    val seeds: Int = 0,
    val downloadSpeed: Long = 0L
)
