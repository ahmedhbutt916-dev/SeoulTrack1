package com.seoultrack.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ── TMDB Data Models ──────────────────────────────────────────────────────────

data class TmdbPageResponse(
    val page: Int,
    val results: List<TmdbShow>,
    val total_pages: Int,
    val total_results: Int,
)

data class TmdbShow(
    val id: Int,
    val name: String?,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Float?,
    val vote_count: Int?,
    val genre_ids: List<Int>?,
    val first_air_date: String?,
    val release_date: String?,
    val origin_country: List<String>?,
    val original_language: String?,
    val popularity: Float?,
)

data class TmdbShowDetail(
    val id: Int,
    val name: String?,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Float?,
    val vote_count: Int?,
    val genres: List<TmdbGenre>?,
    val number_of_seasons: Int?,
    val number_of_episodes: Int?,
    val first_air_date: String?,
    val status: String?,
    val origin_country: List<String>?,
    val original_language: String?,
    val networks: List<TmdbNetwork>?,
)

data class TmdbGenre(
    val id: Int,
    val name: String,
)

data class TmdbNetwork(
    val id: Int,
    val name: String?,
    val logo_path: String?,
)

data class TmdbCreditResponse(
    val cast: List<TmdbCastMember>?,
)

data class TmdbCastMember(
    val id: Int,
    val name: String?,
    val character: String?,
    val profile_path: String?,
    val order: Int?,
)

// ── TMDB Genre Map ────────────────────────────────────────────────────────────

val TmdbGenreMap = mapOf(
    10759 to "Action & Adventure",
    16 to "Animation",
    35 to "Comedy",
    80 to "Crime",
    99 to "Documentary",
    18 to "Drama",
    10751 to "Family",
    10762 to "Kids",
    9648 to "Mystery",
    10763 to "News",
    10764 to "Reality",
    10765 to "Sci-Fi & Fantasy",
    10766 to "Soap",
    10767 to "Talk",
    10768 to "War & Politics",
    28 to "Action",
    12 to "Adventure",
    14 to "Fantasy",
    36 to "History",
    27 to "Horror",
    10402 to "Music",
    10749 to "Romance",
    878 to "Science Fiction",
    10770 to "TV Movie",
    53 to "Thriller",
    10752 to "War",
    37 to "Western",
)

fun TmdbShow.displayTitle(): String = name ?: title ?: "Unknown"

fun TmdbShow.displayYear(): String {
    val date = first_air_date ?: release_date ?: ""
    return if (date.length >= 4) date.substring(0, 4) else ""
}

fun TmdbShow.genreNames(): String {
    val ids = genre_ids ?: return ""
    return ids.mapNotNull { TmdbGenreMap[it] }.take(3).joinToString(" · ")
}

fun TmdbShow.isKorean(): Boolean {
    return origin_country?.contains("KR") == true || original_language == "ko"
}

fun TmdbShowDetail.displayTitle(): String = name ?: title ?: "Unknown"

fun TmdbShowDetail.displayYear(): String {
    val date = first_air_date ?: ""
    return if (date.length >= 4) date.substring(0, 4) else ""
}

fun TmdbShowDetail.genreNames(): String {
    return genres?.map { it.name }?.take(3)?.joinToString(" · ") ?: ""
}

// ── TMDB API Interface ────────────────────────────────────────────────────────

interface TmdbApiService {

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("trending/tv/day")
    suspend fun getTrendingTvDaily(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAir(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("discover/tv")
    suspend fun discoverKoreanDramas(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_original_language") lang: String = "ko",
        @Query("with_genres") genres: String? = null,
        @Query("page") page: Int = 1,
        @Query("vote_count.gte") minVotes: Int = 50,
    ): TmdbPageResponse

    @GET("search/tv")
    suspend fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse

    @GET("tv/{tv_id}")
    suspend fun getShowDetail(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): TmdbShowDetail

    @GET("tv/{tv_id}/credits")
    suspend fun getShowCredits(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
    ): TmdbCreditResponse

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbPageResponse
}

// ── TMDB Client ───────────────────────────────────────────────────────────────

object TmdbClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val IMAGE_BASE = "https://image.tmdb.org/t/p/"

    val api: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }

    fun posterUrl(path: String?, size: String = "w342"): String? {
        if (path == null) return null
        return "${IMAGE_BASE}${size}${path}"
    }

    fun backdropUrl(path: String?, size: String = "w780"): String? {
        if (path == null) return null
        return "${IMAGE_BASE}${size}${path}"
    }

    fun profileUrl(path: String?, size: String = "w185"): String? {
        if (path == null) return null
        return "${IMAGE_BASE}${size}${path}"
    }
}
