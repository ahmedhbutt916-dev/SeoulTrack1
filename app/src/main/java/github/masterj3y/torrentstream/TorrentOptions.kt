package github.masterj3y.torrentstream

import java.io.File

class TorrentOptions private constructor(
    val saveLocation: String,
    val removeFilesAfterStop: Boolean
) {
    class Builder {
        private var saveLocation: String = "/tmp/torrents"
        private var removeFilesAfterStop: Boolean = true

        fun saveLocation(location: String) = apply { this.saveLocation = location }
        fun removeFilesAfterStop(remove: Boolean) = apply { this.removeFilesAfterStop = remove }
        fun build() = TorrentOptions(saveLocation, removeFilesAfterStop)
    }
}
