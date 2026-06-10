package com.seoultrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.seoultrack.ui.theme.*

// Nav tab identifiers
enum class NavTab { DISCOVER, LIBRARY, PROFILE, SETTINGS }

data class NavItemData(
    val tab: NavTab,
    val label: String,
    val icon: @Composable () -> Unit,
)

/**
 * The main SeoulTrack bottom navigation component.
 *
 * Matches the HTML exactly:
 *  - Liquid glass pill bar (left side, width = 100% - 100px, max 420dp)
 *  - Iridescent rainbow border via ::after mask trick → replicated with drawIridescentBorder()
 *  - Top specular reflection lines → drawTopReflection()
 *  - Sliding red pill indicator that animates between tabs with spring
 *  - Pill is draggable to switch tabs (replicates initPillDrag())
 *  - Collapses to a 60dp circle when search is open
 *  - Search bubble FAB (right side, 60dp circle, same glass treatment)
 *  - Search bubble expands into a full search bar (width = 100% - 100px, max 480dp)
 */
@Composable
fun SeoulTrackNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // Pill position animation — spring matches CSS cubic-bezier(0.34, 1.56, 0.64, 1)
    val pillIndex = NavTab.values().indexOf(currentTab)
    val pillOffsetFraction by animateFloatAsState(
        targetValue = pillIndex / NavTab.values().size.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.6f,   // underdamped = slight overshoot like CSS spring
            stiffness    = 300f,
        ),
        label = "pillSlide"
    )

    // Drag state for the pill
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetFraction by remember { mutableStateOf(0f) }

    // Nav collapse animation (when search opens)
    val navCollapsed = searchExpanded
    val navWidth by animateDpAsState(
        targetValue = if (navCollapsed) 60.dp else Dp.Unspecified,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f),
        label = "navWidth"
    )
    val navBorderRadius by animateDpAsState(
        targetValue = if (navCollapsed) 30.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f),
        label = "navRadius"
    )

    // Iridescent shimmer animation — CSS navShimmer 8s
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = -1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label         = "shimmerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomStart,
    ) {

        // ── LEFT: Navigation pill bar ───────────────────────────────────────────
        val navShape = if (navCollapsed) CircleShape else RoundedCornerShape(30.dp)

        // ── Main glass nav bar (floating with frosted backdrop blur) ──────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .then(
                    if (navCollapsed) Modifier.size(60.dp)
                    else Modifier
                        .fillMaxWidth()
                        .padding(end = 76.dp)  // leave room for search bubble
                )
                .heightIn(min = 60.dp, max = 60.dp)
                .drawBehind {
                    // Soft drop shadow
                    val cr = if (navCollapsed) size.minDimension / 2f else 30.dp.toPx()
                    for (i in 3 downTo 1) {
                        val alpha = 0.06f * i
                        val offset = (i * 2).dp.toPx()
                        drawRoundRect(
                            color = Color.Black.copy(alpha = alpha),
                            topLeft = Offset(0f, offset),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(cr, cr),
                        )
                    }
                }
                .clip(navShape)
                // Frosted glass tint — lighter since backdrop blur handles the obscuring
                // On API 31+ the MainScreen backdrop blur provides the real blur effect,
                // so this just needs a subtle tint. On older APIs, this is the only frosted layer.
                .background(
                    FrostedTint,  // ~55% opacity — blurred colors bleed through
                    shape = navShape,
                )
                // Glass gradient overlay
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0x38FFFFFF),
                            0.25f to Color(0x1AFFFFFF),
                            0.50f to Color(0x29FFFFFF),
                            0.75f to Color(0x0FFFFFFF),
                            1.00f to Color(0x21FFFFFF),
                        )
                    ),
                    shape = navShape,
                )
                .drawWithContent {
                    drawContent()
                    // Iridescent border
                    val cr = if (navCollapsed) size.minDimension / 2f else 30.dp.toPx()
                    drawIridescentBorder(cr, 3f)
                    // Specular top reflection (hidden when collapsed)
                    if (!navCollapsed) drawTopReflection(cr)
                }
                .then(
                    if (navCollapsed)
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { searchExpanded = false }
                    else Modifier
                ),
        ) {
            if (navCollapsed) {
                // Show dots icon when collapsed
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DotsIcon()
                }
            } else {
                // Full nav bar content
                NavBarContent(
                    currentTab       = currentTab,
                    pillOffsetFraction = if (isDragging) dragOffsetFraction else pillOffsetFraction,
                    shimmerOffset    = shimmerOffset,
                    onTabSelected    = onTabSelected,
                    onDragStart      = { isDragging = true },
                    onDragEnd        = { fraction ->
                        isDragging = false
                        // Snap to nearest tab
                        val idx = (fraction * NavTab.values().size).coerceIn(0f, (NavTab.values().size - 1).toFloat())
                        onTabSelected(NavTab.values()[idx.toInt().coerceIn(0, NavTab.values().size - 1)])
                    },
                    onDragFraction   = { f -> dragOffsetFraction = f.coerceIn(0f, 1f - 1f/NavTab.values().size) },
                )
            }
        }

        // ── RIGHT: Search bubble FAB ────────────────────────────────────────────
        SearchBubbleFab(
            modifier       = Modifier.align(Alignment.BottomEnd),
            expanded       = searchExpanded,
            searchQuery    = searchQuery,
            onQueryChange  = { searchQuery = it },
            onExpand       = {
                searchExpanded = true
            },
            onCollapse     = {
                searchExpanded = false
                searchQuery    = TextFieldValue("")
            },
        )
    }
}

// ── Nav bar inner content with pill + items ─────────────────────────────────

@Composable
private fun NavBarContent(
    currentTab: NavTab,
    pillOffsetFraction: Float,
    shimmerOffset: Float,
    onTabSelected: (NavTab) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: (Float) -> Unit,
    onDragFraction: (Float) -> Unit,
) {
    val tabs = NavTab.values()
    val tabCount = tabs.size

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val totalWidth = maxWidth
        val tabWidth   = totalWidth / tabCount
        val pillWidth  = tabWidth
        val pillLeft   = tabWidth * (pillOffsetFraction * tabCount)

        // Sliding pill indicator
        Box(
            modifier = Modifier
                .offset(x = pillLeft)
                .width(pillWidth)
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PillBg)
                .drawWithContent {
                    drawContent()
                    // Pill border
                    drawRoundRect(
                        color        = PillBorder,
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style        = Stroke(width = 1.5f),
                    )
                    // Pill inner glow
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x26FFFFFF), Color.Transparent),
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                    )
                }
                .zIndex(1f),
        )

        // Iridescent shimmer overlay on the bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val shimmerBrush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.15f to Color(0x0FFFB4B4),
                            0.30f to Color(0x14B4DCFF),
                            0.45f to Color(0x0FDCB4FF),
                            0.50f to Color.Transparent,
                            0.65f to Color(0x12B4FFDC),
                            0.80f to Color(0x0FFFECB4),
                            1.00f to Color.Transparent,
                        ),
                        start = Offset(size.width * (shimmerOffset + 1f) * 0.5f, 0f),
                        end   = Offset(size.width * (shimmerOffset + 1f) * 0.5f + size.width, size.height),
                    )
                    drawRect(brush = shimmerBrush)
                }
                .zIndex(2f),
        )

        // Nav items row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd   = { onDragEnd(0f) },    // fraction passed in onDrag
                        onDragCancel= { onDragEnd(0f) },
                        onDrag      = { change, dragAmount ->
                            change.consume()
                            // Convert pixel drag to fraction
                            val fraction = dragAmount.x / (size.width.toFloat())
                            onDragFraction(fraction)
                        }
                    )
                },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                NavItemButton(
                    tab       = tab,
                    isActive  = tab == currentTab,
                    modifier  = Modifier.weight(1f),
                    onClick   = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun NavItemButton(
    tab: NavTab,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "navScale"
    )
    val iconColor = if (isActive) Accent else Color(0xB3FFFFFF)  // rgba(255,255,255,0.7)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon with drop-shadow glow when active
        Box(
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (isActive) Modifier.drawBehind {
                        drawCircle(
                            color  = Accent.copy(alpha = 0.3f),
                            radius = 18.dp.toPx(),
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            NavIcon(tab = tab, tint = iconColor)
        }

        Spacer(Modifier.height(3.dp))

        Text(
            text     = tab.name,
            fontSize = 8.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color    = iconColor,
            style    = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color  = Color(0x99000000),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f,
                )
            )
        )
    }
}

// ── Search Bubble FAB ───────────────────────────────────────────────────────

@Composable
private fun SearchBubbleFab(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    searchQuery: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
) {
    val fabWidth by animateDpAsState(
        targetValue = if (expanded) 280.dp else 60.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f),
        label = "fabWidth"
    )
    val fabRadius by animateDpAsState(
        targetValue = if (expanded) 28.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f),
        label = "fabRadius"
    )
    val fabShape = RoundedCornerShape(fabRadius)

    // ── Main glass search bubble (floating with frosted backdrop blur) ───
    Box(
        modifier = modifier
            .width(fabWidth)
            .height(60.dp)
            .drawBehind {
                // Soft layered drop shadow
                for (i in 3 downTo 1) {
                    val alpha = 0.06f * i
                    val offset = (i * 2).dp.toPx()
                    drawRoundRect(
                        color = Color.Black.copy(alpha = alpha),
                        topLeft = Offset(0f, offset),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(fabRadius.toPx(), fabRadius.toPx()),
                    )
                }
            }
            .clip(fabShape)
            // Frosted glass tint — lighter for backdrop blur
            .background(
                FrostedTint,  // ~55% opacity — blurred colors bleed through
                shape = fabShape,
            )
            // Glass gradient overlay
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0x38FFFFFF),
                        0.25f to Color(0x1AFFFFFF),
                        0.50f to Color(0x29FFFFFF),
                        0.75f to Color(0x0FFFFFFF),
                        1.00f to Color(0x21FFFFFF),
                    )
                )
            )
            .drawWithContent {
                drawContent()
                val cr = fabRadius.toPx()
                drawIridescentBorder(cr, 3f)
                drawTopReflection(cr)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
            ) { if (!expanded) onExpand() },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (!expanded) {
            // Collapsed: show search icon centered
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SearchIcon(tint = Color(0xB3FFFFFF))
            }
        } else {
            // Expanded: search input + close button
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SearchIcon(tint = Color(0x80FFFFFF), modifier = Modifier.size(20.dp))

                BasicTextField(
                    value          = searchQuery,
                    onValueChange  = onQueryChange,
                    modifier       = Modifier.weight(1f),
                    textStyle      = androidx.compose.ui.text.TextStyle(
                        color      = TextMain,
                        fontSize   = 15.sp,
                    ),
                    singleLine     = true,
                    decorationBox  = { inner ->
                        if (searchQuery.text.isEmpty()) {
                            Text(
                                text  = "Search K-Dramas...",
                                color = TextMuted,
                                fontSize = 15.sp,
                            )
                        }
                        inner()
                    }
                )

                // Close button (the ✕ circle from the screenshot)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onCollapse,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "✕",
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

// ── Polished Vector Icons ────────────────────────────────────────────────────

@Composable
fun NavIcon(tab: NavTab, tint: Color, modifier: Modifier = Modifier.size(22.dp)) {
    when (tab) {
        NavTab.DISCOVER  -> DiscoverIcon(tint, modifier)
        NavTab.LIBRARY   -> LibraryIcon(tint, modifier)
        NavTab.PROFILE   -> ProfileIcon(tint, modifier)
        NavTab.SETTINGS  -> SettingsIcon(tint, modifier)
    }
}

/**
 * Discover — A compass rose with directional diamond and tick marks.
 * Professional look with a north diamond, cardinal ticks, and fine details.
 */
@Composable
private fun DiscoverIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f - 2f

        // Outer ring
        drawCircle(color = tint, radius = r, style = Stroke(width = 1.8f))

        // Inner ring
        drawCircle(color = tint.copy(alpha = 0.3f), radius = r * 0.7f, style = Stroke(width = 0.8f))

        // Cardinal tick marks (N, E, S, W)
        for (i in 0 until 4) {
            val angle = Math.toRadians(i * 90.0 - 90.0) // start from top
            val x1 = cx + (r * 0.72f) * Math.cos(angle).toFloat()
            val y1 = cy + (r * 0.72f) * Math.sin(angle).toFloat()
            val x2 = cx + (r * 0.92f) * Math.cos(angle).toFloat()
            val y2 = cy + (r * 0.92f) * Math.sin(angle).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), 1.8f)
        }

        // Intercardinal tick marks (NE, SE, SW, NW)
        for (i in 0 until 4) {
            val angle = Math.toRadians(i * 90.0 - 45.0)
            val x1 = cx + (r * 0.78f) * Math.cos(angle).toFloat()
            val y1 = cy + (r * 0.78f) * Math.sin(angle).toFloat()
            val x2 = cx + (r * 0.88f) * Math.cos(angle).toFloat()
            val y2 = cy + (r * 0.88f) * Math.sin(angle).toFloat()
            drawLine(tint.copy(alpha = 0.5f), Offset(x1, y1), Offset(x2, y2), 1f)
        }

        // North diamond pointer — a sleek arrow pointing up
        val diamond = Path().apply {
            moveTo(cx, cy - r * 0.6f)       // top point
            lineTo(cx + r * 0.12f, cy - r * 0.35f)
            lineTo(cx, cy)                    // center
            lineTo(cx - r * 0.12f, cy - r * 0.35f)
            close()
        }
        drawPath(diamond, color = tint)

        // South pointer — lighter, thinner
        val south = Path().apply {
            moveTo(cx, cy + r * 0.6f)       // bottom point
            lineTo(cx + r * 0.08f, cy + r * 0.35f)
            lineTo(cx, cy)                    // center
            lineTo(cx - r * 0.08f, cy + r * 0.35f)
            close()
        }
        drawPath(south, color = tint.copy(alpha = 0.4f))

        // Center dot
        drawCircle(color = tint, radius = 2f, center = Offset(cx, cy))
    }
}

/**
 * Library — Two overlapping bookmark shapes with a play indicator.
 * Polished with rounded inner details and page lines.
 */
@Composable
private fun LibraryIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Back book (slightly offset)
        val backBook = Path().apply {
            moveTo(w * 0.32f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.92f)
            lineTo(w * 0.57f, h * 0.74f)
            lineTo(w * 0.32f, h * 0.92f)
            close()
        }
        drawPath(backBook, color = tint.copy(alpha = 0.25f), style = Stroke(width = 1.5f))

        // Front book (bookmark shape with notch)
        val frontBook = Path().apply {
            moveTo(w * 0.18f, h * 0.08f)
            lineTo(w * 0.68f, h * 0.08f)
            lineTo(w * 0.68f, h * 0.88f)
            lineTo(w * 0.43f, h * 0.70f)
            lineTo(w * 0.18f, h * 0.88f)
            close()
        }
        drawPath(frontBook, color = tint, style = Stroke(width = 1.8f))

        // Page lines on front book
        drawLine(
            tint.copy(alpha = 0.4f),
            Offset(w * 0.28f, h * 0.24f),
            Offset(w * 0.58f, h * 0.24f),
            1f
        )
        drawLine(
            tint.copy(alpha = 0.3f),
            Offset(w * 0.28f, h * 0.34f),
            Offset(w * 0.52f, h * 0.34f),
            1f
        )
        drawLine(
            tint.copy(alpha = 0.2f),
            Offset(w * 0.28f, h * 0.44f),
            Offset(w * 0.48f, h * 0.44f),
            1f
        )
    }
}

/**
 * Profile — Sleek person silhouette with a subtle ring.
 * Modern design with a filled head circle and smooth body curve.
 */
@Composable
private fun ProfileIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val headRadius = size.minDimension * 0.2f
        val headCy = size.height * 0.32f

        // Head — filled circle with border
        drawCircle(
            color = tint.copy(alpha = 0.15f),
            radius = headRadius + 2f,
            center = Offset(cx, headCy),
        )
        drawCircle(
            color = tint,
            radius = headRadius,
            center = Offset(cx, headCy),
            style = Stroke(width = 1.8f),
        )

        // Body — smooth curved shoulder arc
        val bodyPath = Path().apply {
            moveTo(cx - size.width * 0.4f, size.height * 0.92f)
            cubicTo(
                cx - size.width * 0.4f, size.height * 0.58f,
                cx - size.width * 0.18f, size.height * 0.52f,
                cx, size.height * 0.54f,
            )
            cubicTo(
                cx + size.width * 0.18f, size.height * 0.52f,
                cx + size.width * 0.4f, size.height * 0.58f,
                cx + size.width * 0.4f, size.height * 0.92f,
            )
        }
        drawPath(
            bodyPath,
            color = tint,
            style = Stroke(width = 1.8f),
        )

        // Subtle status dot on shoulder
        drawCircle(
            color = tint.copy(alpha = 0.3f),
            radius = 2.5f,
            center = Offset(cx + size.width * 0.15f, size.height * 0.6f),
        )
    }
}

/**
 * Settings — Professional gear/cog with smooth teeth.
 * Clean design with proper tooth geometry and inner detail.
 */
@Composable
private fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.minDimension * 0.44f
        val innerR = size.minDimension * 0.30f
        val toothDepth = size.minDimension * 0.08f
        val teethCount = 8

        // Draw the gear body using path
        val gearPath = Path().apply {
            for (i in 0 until teethCount) {
                val baseAngle = (i.toFloat() / teethCount) * 2f * Math.PI.toFloat()
                val halfTooth = (1f / teethCount) * Math.PI.toFloat() * 0.35f

                // Tooth outer edge
                val a1 = baseAngle - halfTooth * 1.4f
                val a2 = baseAngle - halfTooth
                val a3 = baseAngle + halfTooth
                val a4 = baseAngle + halfTooth * 1.4f

                val outerIR = outerR - toothDepth

                if (i == 0) {
                    moveTo(
                        cx + outerIR * kotlin.math.cos(a1),
                        cy + outerIR * kotlin.math.sin(a1),
                    )
                }

                // Rise to tooth
                lineTo(
                    cx + outerIR * kotlin.math.cos(a2),
                    cy + outerIR * kotlin.math.sin(a2),
                )
                // Tooth top
                lineTo(
                    cx + outerR * kotlin.math.cos(a2 + halfTooth * 0.2f),
                    cy + outerR * kotlin.math.sin(a2 + halfTooth * 0.2f),
                )
                lineTo(
                    cx + outerR * kotlin.math.cos(a3 - halfTooth * 0.2f),
                    cy + outerR * kotlin.math.sin(a3 - halfTooth * 0.2f),
                )
                // Fall from tooth
                lineTo(
                    cx + outerIR * kotlin.math.cos(a3),
                    cy + outerIR * kotlin.math.sin(a3),
                )
                // Valley between teeth
                lineTo(
                    cx + outerIR * kotlin.math.cos(a4),
                    cy + outerIR * kotlin.math.sin(a4),
                )
            }
            close()
        }
        drawPath(gearPath, color = tint, style = Stroke(width = 1.8f))

        // Inner circle
        drawCircle(
            color = tint,
            radius = innerR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f),
        )

        // Center dot
        drawCircle(color = tint.copy(alpha = 0.4f), radius = 2.5f, center = Offset(cx, cy))

        // Decorative inner notches
        for (i in 0 until 4) {
            val angle = Math.toRadians(i * 90.0 + 45.0)
            val x1 = cx + (innerR * 0.5f) * Math.cos(angle).toFloat()
            val y1 = cy + (innerR * 0.5f) * Math.sin(angle).toFloat()
            val x2 = cx + (innerR * 0.85f) * Math.cos(angle).toFloat()
            val y2 = cy + (innerR * 0.85f) * Math.sin(angle).toFloat()
            drawLine(tint.copy(alpha = 0.3f), Offset(x1, y1), Offset(x2, y2), 1f)
        }
    }
}

/**
 * Search — Professional magnifying glass with lens shine.
 */
@Composable
private fun SearchIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        val cx = size.width * 0.40f
        val cy = size.height * 0.40f
        val r = size.minDimension * 0.30f

        // Lens circle — thicker stroke for prominence
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 2.2f),
        )

        // Lens shine — small arc highlight
        val shinePath = Path().apply {
            val shineR = r * 0.7f
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    center = Offset(cx - r * 0.2f, cy - r * 0.2f),
                    radius = shineR,
                ),
                startAngleDegrees = 200f,
                sweepAngleDegrees = 60f,
                forceMoveTo = true,
            )
        }
        drawPath(shinePath, color = tint.copy(alpha = 0.25f), style = Stroke(width = 1.5f))

        // Handle — tapered line from bottom-right of lens
        val handleStart = Offset(
            cx + r * 0.72f,
            cy + r * 0.72f,
        )
        val handleEnd = Offset(
            size.width * 0.90f,
            size.height * 0.90f,
        )
        drawLine(tint, handleStart, handleEnd, 2.5f)

        // Handle cap — small circle at end
        drawCircle(
            color = tint.copy(alpha = 0.3f),
            radius = 1.5f,
            center = handleEnd,
        )
    }
}

@Composable
private fun DotsIcon() {
    Canvas(Modifier.size(24.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val gap = 8f
        listOf(cx - gap, cx, cx + gap).forEach { x ->
            drawCircle(Color(0xCCFFFFFF), radius = 3f, center = Offset(x, cy))
        }
    }
}
