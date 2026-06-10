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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.seoultrack.R
import com.seoultrack.ui.theme.*
import kotlin.math.abs

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
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomStart,
    ) {

        // ── LEFT: Navigation pill bar ───────────────────────────────────────────
        val navShape = if (navCollapsed) CircleShape else RoundedCornerShape(30.dp)

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
                .clip(navShape)
                // Glass background — matches the CSS gradient exactly
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

    Box(
        modifier = modifier
            .width(fabWidth)
            .height(60.dp)
            .clip(fabShape)
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

// ── SVG Icons (inline vectors matching the HTML SVGs) ──────────────────────

@Composable
fun NavIcon(tab: NavTab, tint: Color, modifier: Modifier = Modifier.size(22.dp)) {
    when (tab) {
        NavTab.DISCOVER  -> DiscoverIcon(tint, modifier)
        NavTab.LIBRARY   -> LibraryIcon(tint, modifier)
        NavTab.PROFILE   -> ProfileIcon(tint, modifier)
        NavTab.SETTINGS  -> SettingsIcon(tint, modifier)
    }
}

@Composable
private fun DiscoverIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2; val cy = size.height / 2; val r = size.minDimension / 2 - 1f
        drawCircle(color = tint, radius = r, style = Stroke(width = 2f))
        // Compass arrow — simplified
        drawLine(tint, Offset(cx, cy - r*0.5f), Offset(cx + r*0.35f, cy + r*0.35f), 2f)
        drawLine(tint, Offset(cx, cy - r*0.5f), Offset(cx - r*0.2f, cy + r*0.15f), 2f)
    }
}

@Composable
private fun LibraryIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        // Bookmark shape
        val path = Path().apply {
            moveTo(w*0.25f, h*0.1f)
            lineTo(w*0.75f, h*0.1f)
            lineTo(w*0.75f, h*0.9f)
            lineTo(w*0.50f, h*0.7f)
            lineTo(w*0.25f, h*0.9f)
            close()
        }
        drawPath(path, color = Color.Transparent, style = androidx.compose.ui.graphics.drawscope.Fill)
        drawPath(path, color = tint, style = Stroke(width = 2f))
    }
}

@Composable
private fun ProfileIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2; val cy = size.height * 0.35f
        drawCircle(color = tint, radius = size.width * 0.2f, center = Offset(cx, cy), style = Stroke(width = 2f))
        val path = Path().apply {
            moveTo(cx - size.width*0.38f, size.height*0.9f)
            cubicTo(
                cx - size.width*0.38f, size.height*0.65f,
                cx + size.width*0.38f, size.height*0.65f,
                cx + size.width*0.38f, size.height*0.9f
            )
        }
        drawPath(path, color = tint, style = Stroke(width = 2f))
    }
}

@Composable
private fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2; val cy = size.height / 2; val r = size.minDimension * 0.18f; val R = size.minDimension * 0.4f
        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(width = 2f))
        drawCircle(color = tint, radius = R, center = Offset(cx, cy), style = Stroke(width = 2f))
        // Gear teeth (8 tick marks)
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val x1 = cx + (R - 3f) * Math.cos(angle).toFloat()
            val y1 = cy + (R - 3f) * Math.sin(angle).toFloat()
            val x2 = cx + (R + 3f) * Math.cos(angle).toFloat()
            val y2 = cy + (R + 3f) * Math.sin(angle).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), 2.5f)
        }
    }
}

@Composable
private fun SearchIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        val cx = size.width * 0.42f; val cy = size.height * 0.42f; val r = size.minDimension * 0.28f
        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(width = 2f))
        drawLine(tint, Offset(cx + r*0.7f, cy + r*0.7f), Offset(size.width*0.88f, size.height*0.88f), 2.5f)
    }
}

@Composable
private fun DotsIcon() {
    Canvas(Modifier.size(24.dp)) {
        val cx = size.width / 2; val cy = size.height / 2; val r = 3f; val gap = 10f
        listOf(cx - gap, cx, cx + gap).forEach { x ->
            drawCircle(Color(0xCCFFFFFF), radius = r, center = Offset(x, cy))
        }
    }
}
