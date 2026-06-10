package com.seoultrack.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// ── Data models ──────────────────────────────────────────────────────────────

data class StremioStream(
    val name: String?,
    val title: String?,
    val infoHash: String?,
    val fileIdx: Int?,
    val url: String?,       // direct URL streams
    val behaviorHints: BehaviorHints?,
)

data class BehaviorHints(
    val notWebReady: Boolean?,
)

data class StremioStreamsResponse(
    val streams: List<StremioStream>?,
)

// ── Stremio addon API client ─────────────────────────────────────────────────

interface StremioAddonApi {
    /**
     * Fetch streams for a series episode.
     * type = "series", id = "tt1234567:1:1" (IMDB:season:episode)
     */
    @GET("{type}/stream/{id}.json")
    suspend fun getStreams(
        @Path("type") type: String,
        @Path("id")   id: String,
    ): StremioStreamsResponse
}

object StremioClient {

    /**
     * Build an API client for any Stremio addon base URL.
     * Default: Torrentio (most popular K-drama source)
     */
    fun forAddon(baseUrl: String): StremioAddonApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StremioAddonApi::class.java)
    }

    // Torrentio addon — full torrent swarm via libtorrent (same source the HTML uses)
    val torrentio = forAddon("https://torrentio.strem.fun/")

    /**
     * Build a magnet URI from a Stremio stream's infoHash.
     */
    fun buildMagnet(stream: StremioStream): String? {
        val hash = stream.infoHash ?: return stream.url
        val fileIdx = stream.fileIdx?.let { "&so=$it" } ?: ""
        return "magnet:?xt=urn:btih:$hash$fileIdx&tr=udp://tracker.opentrackr.org:1337/announce"
    }
}
