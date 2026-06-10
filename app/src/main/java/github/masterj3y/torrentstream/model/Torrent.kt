package github.masterj3y.torrentstream.model

import java.io.File

data class Torrent(
    val magnetUri: String = "",
    val videoFile: File? = null
)
