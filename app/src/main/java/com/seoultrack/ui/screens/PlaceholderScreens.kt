package com.seoultrack.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seoultrack.data.*
import com.seoultrack.ui.components.NavTab
import com.seoultrack.ui.theme.*
import kotlinx.coroutines.launch

// ── Sample Data (fallback when no TMDB key) ──────────────────────────────────

data class KDrama(
    val title: String,
    val year: Int,
    val rating: Float,
    val genre: String,
    val status: String,
    val episodes: Int,
    val currentEp: Int = 0,
    val color: Color,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val overview: String? = null,
)

val sampleDramas = listOf(
    KDrama("Crash Landing on You", 2019, 9.2f, "Romance · Drama", "Watched", 16, 16, Color(0xFFE85D75)),
    KDrama("Goblin", 2016, 9.0f, "Fantasy · Romance", "Watched", 16, 16, Color(0xFF6C5CE7)),
    KDrama("Vincenzo", 2021, 8.8f, "Action · Comedy", "Watching", 20, 14, Color(0xFF00B894)),
    KDrama("Squid Game", 2021, 8.4f, "Thriller · Drama", "Watched", 9, 9, Color(0xFFE17055)),
    KDrama("Hotel del Luna", 2019, 8.6f, "Fantasy · Horror", "Plan to Watch", 16, 0, Color(0xFFA29BFE)),
    KDrama("Itaewon Class", 2020, 8.3f, "Drama · Business", "On Hold", 16, 8, Color(0xFFFD79A8)),
    KDrama("My Love from the Star", 2013, 8.9f, "Romance · Sci-Fi", "Watched", 21, 21, Color(0xFF0984E3)),
    KDrama("Descendants of the Sun", 2016, 8.7f, "Romance · Military", "Watched", 16, 16, Color(0xFF6C5CE7)),
    KDrama("Kingdom", 2019, 8.5f, "Horror · Historical", "Watching", 6, 4, Color(0xFFD63031)),
    KDrama("Start-Up", 2020, 8.2f, "Romance · Business", "Plan to Watch", 16, 0, Color(0xFF00CEC9)),
    KDrama("The King: Eternal Monarch", 2020, 8.1f, "Fantasy · Romance", "Dropped", 16, 6, Color(0xFF636E72)),
    KDrama("Penthouse", 2020, 8.6f, "Thriller · Mystery", "Watching", 21, 17, Color(0xFFE84393)),
)

val trendingDramas = listOf(
    KDrama("Queen of Tears", 2024, 9.1f, "Romance · Drama", "Watching", 16, 10, Color(0xFFFF6B6B)),
    KDrama("Lovely Runner", 2024, 9.0f, "Fantasy · Romance", "Watching", 16, 6, Color(0xFF4ECDC4)),
    KDrama("Marry My Husband", 2024, 8.7f, "Romance · Revenge", "Plan to Watch", 16, 0, Color(0xFF45B7D1)),
    KDrama("My Demon", 2023, 8.5f, "Fantasy · Romance", "Watching", 16, 12, Color(0xFF96CEB4)),
    KDrama("Gyeongseong Creature", 2023, 8.3f, "Horror · Historical", "Plan to Watch", 10, 0, Color(0xFFD4A574)),
    KDrama("A Shop for Killers", 2024, 8.4f, "Action · Thriller", "Watching", 8, 5, Color(0xFFDDA0DD)),
)

// ── Reusable Components ──────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x1AFFFFFF),
                        0.50f to Color(0x0FFFFFFF),
                        1.00f to Color(0x14FFFFFF),
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x2EFFFFFF), Color(0x0FFFFFFF), Color(0x2EFFFFFF))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun SectionHeader(title: String, actionLabel: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color(0x40000000),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f,
                )
            )
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
            )
        }
    }
}

@Composable
fun DramaPosterCard(
    drama: KDrama,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
) {
    Column(
        modifier = modifier.width(130.dp),
    ) {
        // Poster — loads TMDB image if available, else gradient placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (drama.posterUrl != null) Modifier
                    else Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                drama.color.copy(alpha = 0.8f),
                                drama.color.copy(alpha = 0.4f),
                                Color(0xFF0A0E1A).copy(alpha = 0.9f),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            if (drama.posterUrl != null) {
                AsyncImage(
                    model = drama.posterUrl,
                    contentDescription = drama.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // Rating badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "★ ${drama.rating}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusPlan,
                )
            }

            // Play icon overlay for currently watching
            if (drama.status == "Watching") {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Accent.copy(alpha = 0.7f))
                        .border(1.dp, Color(0x40FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("\u25B6", fontSize = 14.sp, color = Color.White)
                }
            }

            // Progress bar
            if (showProgress && drama.episodes > 0) {
                val progress = drama.currentEp.toFloat() / drama.episodes.toFloat()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0x33000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(Accent)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = drama.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMain,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
        )

        Text(
            text = "${drama.year} · ${drama.episodes} eps",
            fontSize = 11.sp,
            color = TextMuted,
        )
    }
}

@Composable
fun TmdbShowPosterCard(
    show: TmdbShow,
    modifier: Modifier = Modifier,
) {
    val posterUrl = TmdbClient.posterUrl(show.poster_path)
    val rating = show.vote_average?.let { String.format("%.1f", it) } ?: "--"
    val year = show.displayYear()
    val title = show.displayTitle()

    Column(
        modifier = modifier.width(130.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (posterUrl != null) Modifier
                    else Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(Accent.copy(alpha = 0.5f), BgBase),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                // Fallback text poster
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            // Rating badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "★ $rating",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusPlan,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMain,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
        )

        Text(
            text = if (year.isNotEmpty()) "$year" else "",
            fontSize = 11.sp,
            color = TextMuted,
        )
    }
}

@Composable
fun WideDramaCard(drama: KDrama, modifier: Modifier = Modifier, showProgress: Boolean = false) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        drama.color.copy(alpha = 0.15f),
                        Color(0x0AFFFFFF),
                    )
                )
            )
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mini poster — loads TMDB image if available
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 78.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (drama.posterUrl != null) Modifier
                    else Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(drama.color.copy(alpha = 0.7f), drama.color.copy(alpha = 0.3f))
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (drama.posterUrl != null) {
                AsyncImage(
                    model = drama.posterUrl,
                    contentDescription = drama.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text("${drama.rating}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = drama.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = drama.genre,
                fontSize = 12.sp,
                color = TextMuted,
            )
            Spacer(Modifier.height(4.dp))

            // Status chip
            val chipColor = when (drama.status) {
                "Watching" -> StatusWatching
                "Watched" -> StatusWatched
                "Plan to Watch" -> StatusPlan
                "On Hold" -> Color(0xFFA29BFE)
                "Dropped" -> Color(0xFF636E72)
                else -> TextMuted
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(chipColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = drama.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = chipColor,
                )
            }
        }

        // Episode progress
        if (drama.status == "Watching" && drama.episodes > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${drama.currentEp}/${drama.episodes}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain,
                )
                Text("eps", fontSize = 9.sp, color = TextMuted)
            }
        }
    }
}

// ── Discover Screen ──────────────────────────────────────────────────────────

@Composable
fun DiscoverScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiKey = AppPreferences.getTmdbApiKey(context)

    // TMDB data states
    var trendingShows by remember { mutableStateOf<List<TmdbShow>>(emptyList()) }
    var popularShows by remember { mutableStateOf<List<TmdbShow>>(emptyList()) }
    var koreanDramas by remember { mutableStateOf<List<TmdbShow>>(emptyList()) }
    var onTheAirShows by remember { mutableStateOf<List<TmdbShow>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Fetch TMDB data when API key is available
    LaunchedEffect(apiKey) {
        if (apiKey.isNotBlank()) {
            isLoading = true
            loadError = null
            try {
                val trendingResp = TmdbClient.api.getTrendingTvDaily(apiKey)
                trendingShows = trendingResp.results.filter { it.isKorean() || it.vote_average != null }
                    .take(10)

                val popularResp = TmdbClient.api.getPopularTv(apiKey)
                popularShows = popularResp.results.take(10)

                val kdramaResp = TmdbClient.api.discoverKoreanDramas(apiKey)
                koreanDramas = kdramaResp.results.take(10)

                val onAirResp = TmdbClient.api.getOnTheAir(apiKey)
                onTheAirShows = onAirResp.results.filter { it.isKorean() }.take(6)
            } catch (e: Exception) {
                loadError = e.message
            }
            isLoading = false
        }
    }

    // Featured show (first trending Korean drama or first trending)
    val featuredShow = trendingShows.firstOrNull { it.isKorean() } ?: trendingShows.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 100.dp),
    ) {
        // Logo / Greeting header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "SeoulTrack",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Accent,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = AccentGlow,
                        offset = Offset(0f, 0f),
                        blurRadius = 20f,
                    )
                )
            )
            Text(
                text = if (apiKey.isNotBlank()) "Powered by TMDB" else "What will you watch tonight?",
                fontSize = 14.sp,
                color = TextMuted,
            )
        }

        Spacer(Modifier.height(20.dp))

        if (apiKey.isNotBlank() && !isLoading) {
            // ── TMDB-powered content ──────────────────────────────────────────

            // Featured Banner (from TMDB)
            if (featuredShow != null) {
                val backdropUrl = TmdbClient.backdropUrl(featuredShow.backdrop_path)
                val featuredTitle = featuredShow.displayTitle()
                val featuredYear = featuredShow.displayYear()
                val featuredRating = featuredShow.vote_average?.let { String.format("%.1f", it) } ?: "--"
                val featuredGenre = featuredShow.genreNames()

                val featuredPulse = rememberInfiniteTransition(label = "featured")
                val featuredAlpha by featuredPulse.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
                    label = "featuredAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (backdropUrl != null) Modifier
                            else Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Accent.copy(alpha = 0.5f * featuredAlpha),
                                        OrbPurple.copy(alpha = 0.3f),
                                        BgBase,
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                                )
                            )
                        )
                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(20.dp)),
                ) {
                    if (backdropUrl != null) {
                        AsyncImage(
                            model = backdropUrl,
                            contentDescription = featuredTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        // Gradient overlay for text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xB30A0E1A),
                                            Color(0xF00A0E1A),
                                        )
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Trending Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = featuredTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMain,
                        )
                        Text(
                            text = "$featuredGenre · $featuredYear · ★ $featuredRating",
                            fontSize = 12.sp,
                            color = TextMuted,
                        )
                    }

                    // Play button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Accent)
                            .border(1.dp, Color(0x40FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("\u25B6", fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(24.dp)
                )
            }

            // Trending Today
            if (trendingShows.isNotEmpty()) {
                SectionHeader("Trending Today", "See All")
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(trendingShows) { show ->
                        TmdbShowPosterCard(show)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Korean Dramas (TMDB discover)
            if (koreanDramas.isNotEmpty()) {
                SectionHeader("K-Dramas", "See All")
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(koreanDramas) { show ->
                        TmdbShowPosterCard(show)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Popular TV
            if (popularShows.isNotEmpty()) {
                SectionHeader("Popular TV", "See All")
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(popularShows) { show ->
                        TmdbShowPosterCard(show)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Currently Airing K-Dramas
            if (onTheAirShows.isNotEmpty()) {
                SectionHeader("Currently Airing", "See All")
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                ) {
                    items(onTheAirShows) { show ->
                        TmdbShowPosterCard(show)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Error state
            if (loadError != null) {
                GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Could not load TMDB data: $loadError",
                        fontSize = 13.sp,
                        color = Accent,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Check your API key in Settings → TMDB Configuration",
                        fontSize = 12.sp,
                        color = TextMuted,
                    )
                }
            }

        } else if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Loading from TMDB...", fontSize = 14.sp, color = TextMuted)
                }
            }
        } else {
            // ── Fallback: sample data when no TMDB key ──────────────────────

            // Featured Banner
            val featuredPulse = rememberInfiniteTransition(label = "featured")
            val featuredAlpha by featuredPulse.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "featuredAlpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Accent.copy(alpha = 0.5f * featuredAlpha),
                                OrbPurple.copy(alpha = 0.3f),
                                BgBase,
                            ),
                            start = Offset.Zero,
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    )
                    .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = "Add Your TMDB Key",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "Unlock Real Show Data",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextMain,
                    )
                    Text(
                        text = "Go to Settings → TMDB Configuration to enter your API key",
                        fontSize = 12.sp,
                        color = TextMuted,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Trending Now row (sample)
            SectionHeader("Trending Now", "See All")
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                items(trendingDramas) { drama ->
                    DramaPosterCard(drama)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Popular K-Dramas grid (sample)
            SectionHeader("Popular K-Dramas", "See All")
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                items(sampleDramas.take(6)) { drama ->
                    DramaPosterCard(drama)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recently Added (sample)
            SectionHeader("Recently Added")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                sampleDramas.drop(6).take(4).forEach { drama ->
                    WideDramaCard(drama)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

// ── Library Screen ───────────────────────────────────────────────────────────

@Composable
fun LibraryScreen() {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Watching", "Watched", "Plan to Watch", "On Hold", "Dropped")

    val filteredDramas = if (selectedFilter == "All") {
        sampleDramas
    } else {
        sampleDramas.filter { it.status == selectedFilter }
    }

    // Stats
    val watchingCount = sampleDramas.count { it.status == "Watching" }
    val watchedCount = sampleDramas.count { it.status == "Watched" }
    val planCount = sampleDramas.count { it.status == "Plan to Watch" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 100.dp),
    ) {
        // Header
        Text(
            text = "My Library",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = TextMain,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard("Watching", watchingCount.toString(), StatusWatching, Modifier.weight(1f))
            StatCard("Watched", watchedCount.toString(), StatusWatched, Modifier.weight(1f))
            StatCard("Plan", planCount.toString(), StatusPlan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Accent.copy(alpha = 0.2f) else Color(0x0AFFFFFF)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Accent.copy(alpha = 0.4f) else Color(0x14FFFFFF),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Accent else TextMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Drama list
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (filteredDramas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No dramas here yet", fontSize = 14.sp, color = TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Text("Add shows from the Discover tab", fontSize = 12.sp, color = TextMuted)
                    }
                }
            } else {
                filteredDramas.forEach { drama ->
                    WideDramaCard(drama, showProgress = true)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = color,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted,
        )
    }
}

// ── Profile Screen ───────────────────────────────────────────────────────────

@Composable
fun ProfileScreen() {
    val totalEps = sampleDramas.sumOf { it.currentEp }
    val totalDramas = sampleDramas.size
    val completedDramas = sampleDramas.count { it.status == "Watched" }
    val avgRating = if (sampleDramas.isNotEmpty()) {
        String.format("%.1f", sampleDramas.map { it.rating }.average())
    } else "0.0"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 100.dp),
    ) {
        // Profile header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Accent.copy(alpha = 0.6f), OrbPurple.copy(alpha = 0.4f))
                        )
                    )
                    .border(2.dp, Accent.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("\uD83C\uDDF0\uD83C\uDDF7", fontSize = 32.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "DramaFan",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
            )
            Text(
                text = "K-Drama enthusiast since 2020",
                fontSize = 13.sp,
                color = TextMuted,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Stats grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileStatItem("Dramas", totalDramas.toString(), Accent, Modifier.weight(1f))
            ProfileStatItem("Episodes", totalEps.toString(), StatusWatching, Modifier.weight(1f))
            ProfileStatItem("Completed", completedDramas.toString(), StatusWatched, Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ProfileStatItem("Avg Rating", avgRating, StatusPlan, Modifier.weight(1f))
            ProfileStatItem("Watchlist", sampleDramas.count { it.status == "Plan to Watch" }.toString(), OrbPurple, Modifier.weight(1f))
            ProfileStatItem("On Hold", sampleDramas.count { it.status == "On Hold" }.toString(), Color(0xFFA29BFE), Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        // Favorite Genres
        SectionHeader("Favorite Genres")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Romance", "Fantasy", "Thriller", "Drama", "Action").forEach { genre ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x0FFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = genre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMain,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Recently Watched
        SectionHeader("Recently Watched", "See All")
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(sampleDramas.filter { it.currentEp > 0 }.take(5)) { drama ->
                DramaPosterCard(drama, showProgress = true)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Watch Time Breakdown
        SectionHeader("Watch Activity")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val activity = listOf(2, 4, 1, 3, 5, 8, 6)
            val maxActivity = activity.maxOrNull() ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                days.forEachIndexed { index, day ->
                    val height = (activity[index].toFloat() / maxActivity.toFloat()) * 80f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Accent.copy(alpha = 0.8f),
                                            Accent.copy(alpha = 0.3f),
                                        )
                                    )
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = day,
                            fontSize = 9.sp,
                            color = TextMuted,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = color,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted,
        )
    }
}

// ── Settings Screen ──────────────────────────────────────────────────────────

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(AppPreferences.isDarkMode(context)) }
    var oledMode by remember { mutableStateOf(AppPreferences.isOledMode(context)) }
    var notificationsEnabled by remember { mutableStateOf(AppPreferences.isNotificationsEnabled(context)) }
    var autoPlayNext by remember { mutableStateOf(AppPreferences.isAutoPlay(context)) }
    var subtitleEnabled by remember { mutableStateOf(AppPreferences.isSubtitlesEnabled(context)) }
    var selectedQuality by remember { mutableStateOf(AppPreferences.getStreamingQuality(context)) }
    var selectedSubtitleLang by remember { mutableStateOf(AppPreferences.getSubtitleLanguage(context)) }

    // TMDB API key state
    var tmdbApiKey by remember { mutableStateOf(TextFieldValue(AppPreferences.getTmdbApiKey(context))) }
    var tmdbKeySaved by remember { mutableStateOf(AppPreferences.hasTmdbApiKey(context)) }
    var tmdbKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 100.dp),
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = TextMain,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(20.dp))

        // ── TMDB Configuration Section ──────────────────────────────────────
        SectionHeader("TMDB Configuration")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            // API Key input
            Text(
                text = "TMDB API Key",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Enter your TMDB API key (v3 auth) to load real show data. Get one free at themoviedb.org/settings/api",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(10.dp))

            // API Key input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x0AFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicTextField(
                    value = tmdbApiKey,
                    onValueChange = { tmdbApiKey = it },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextMain,
                        fontSize = 14.sp,
                    ),
                    singleLine = true,
                    visualTransformation = if (tmdbKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None
                    else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    decorationBox = { inner ->
                        if (tmdbApiKey.text.isEmpty()) {
                            Text(
                                text = "Paste your API key here...",
                                color = TextMuted,
                                fontSize = 14.sp,
                            )
                        }
                        inner()
                    }
                )

                // Show/hide toggle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x0FFFFFFF))
                        .clickable { tmdbKeyVisible = !tmdbKeyVisible },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (tmdbKeyVisible) "\uD83D\uDC41" else "\uD83D\uDC41\u200D\uD83D\uDD8C",
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Accent.copy(alpha = 0.15f))
                        .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable {
                            AppPreferences.setTmdbApiKey(context, tmdbApiKey.text)
                            tmdbKeySaved = tmdbApiKey.text.isNotBlank()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Save Key",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(8.dp))
                        .clickable {
                            AppPreferences.setTmdbApiKey(context, "")
                            tmdbApiKey = TextFieldValue("")
                            tmdbKeySaved = false
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Clear Key",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                    )
                }
            }

            // Status indicator
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (tmdbKeySaved) StatusWatched else Color(0xFF636E72))
                )
                Text(
                    text = if (tmdbKeySaved) "API key saved — Discover tab will load real data" else "No API key — showing sample data",
                    fontSize = 11.sp,
                    color = if (tmdbKeySaved) StatusWatched else TextMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Appearance Section
        SectionHeader("Appearance")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsToggleItem("Dark Mode", "Use dark theme throughout the app", darkModeEnabled) {
                darkModeEnabled = it
                AppPreferences.setDarkMode(context, it)
            }
            SettingsDivider()
            SettingsToggleItem("OLED Black", "True black background for OLED displays", oledMode) {
                oledMode = it
                AppPreferences.setOledMode(context, it)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Playback Section
        SectionHeader("Playback")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsToggleItem("Auto-Play Next Episode", "Automatically play the next episode", autoPlayNext) {
                autoPlayNext = it
                AppPreferences.setAutoPlay(context, it)
            }
            SettingsDivider()
            SettingsToggleItem("Subtitles", "Show subtitles by default", subtitleEnabled) {
                subtitleEnabled = it
                AppPreferences.setSubtitlesEnabled(context, it)
            }
            SettingsDivider()
            SettingsSelectItem("Streaming Quality", selectedQuality, listOf("Auto", "1080p", "720p", "480p")) {
                selectedQuality = it
                AppPreferences.setStreamingQuality(context, it)
            }
            SettingsDivider()
            SettingsSelectItem("Subtitle Language", selectedSubtitleLang, listOf("Korean", "English", "Chinese", "Japanese", "Off")) {
                selectedSubtitleLang = it
                AppPreferences.setSubtitleLanguage(context, it)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Notifications Section
        SectionHeader("Notifications")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsToggleItem("Push Notifications", "Get notified about new episodes", notificationsEnabled) {
                notificationsEnabled = it
                AppPreferences.setNotificationsEnabled(context, it)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Data & Storage Section
        SectionHeader("Data & Storage")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsActionItem("Clear Cache", "Free up 128 MB of cached data")
            SettingsDivider()
            SettingsActionItem("Export Library", "Export your watchlist as JSON")
            SettingsDivider()
            SettingsActionItem("Import Library", "Import from MyDramaList or AniList")
        }

        Spacer(Modifier.height(20.dp))

        // About Section
        SectionHeader("About")
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            SettingsInfoItem("Version", "1.1.0")
            SettingsDivider()
            SettingsInfoItem("Build", "debug")
            SettingsDivider()
            SettingsInfoItem("Data Source", if (tmdbKeySaved) "TMDB API" else "Sample Data")
            SettingsDivider()
            SettingsActionItem("Open Source Licenses", "View third-party licenses")
            SettingsDivider()
            SettingsActionItem("Report a Bug", "Help us improve SeoulTrack")
        }

        Spacer(Modifier.height(24.dp))

        // App signature
        Text(
            text = "SeoulTrack v1.1.0 · Made with \u2764\uFE0F for K-Drama fans",
            fontSize = 11.sp,
            color = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
            )
        }

        // Custom toggle switch
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (checked) Accent else Color(0x1AFFFFFF))
                .border(1.dp, if (checked) Accent.copy(alpha = 0.3f) else Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                .padding(2.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun SettingsSelectItem(
    title: String,
    currentValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentValue,
                    fontSize = 13.sp,
                    color = Accent,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = if (expanded) " \u25B2" else " \u25BC",
                    fontSize = 10.sp,
                    color = TextMuted,
                )
            }
        }

        if (expanded) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                options.forEach { option ->
                    val isSelected = option == currentValue
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Accent.copy(alpha = 0.15f) else Color(0x0AFFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) Accent.copy(alpha = 0.3f) else Color(0x0FFFFFFF),
                                RoundedCornerShape(8.dp),
                            )
                            .clickable {
                                onSelected(option)
                                expanded = false
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Accent else TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
            )
        }
        Text("\u203A", fontSize = 18.sp, color = TextMuted)
    }
}

@Composable
private fun SettingsInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMain,
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextMuted,
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(1.dp)
            .background(Color(0x0FFFFFFF))
    )
}
