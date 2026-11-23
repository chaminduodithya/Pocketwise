package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SereneTheme
import com.example.ui.viewmodel.CategoryBreakdownItem
import com.example.ui.viewmodel.InsightsUiState
import com.example.ui.viewmodel.MonthYearKey
import com.example.ui.viewmodel.MonthlyComparisonBar
import com.example.util.LkrFormatter
import java.io.OutputStream
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun InsightsScreen(
    state: InsightsUiState,
    onStepMonth: (Int) -> Unit,
    onSelectMonth: (MonthYearKey) -> Unit,
    onWritePdfReport: (OutputStream) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryName by remember(state.selectedMonth) {
        mutableStateOf<String?>(null)
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    onWritePdfReport(stream)
                }
            }.onSuccess {
                onShowMessage("Saved ${state.selectedMonth.shortMonth()} PDF report")
            }.onFailure {
                onShowMessage("Could not save PDF report")
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector & High-level Spend Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onStepMonth(-1) },
                                modifier = Modifier.testTag("insights_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingDown.let { Icons.Outlined.ChevronLeft },
                                    contentDescription = "Previous month",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = SereneTheme.colors.surfaceContainerLowest,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = state.selectedMonth.label(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onStepMonth(1) },
                                modifier = Modifier.testTag("insights_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = "Next month",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "TOTAL MONTHLY SPEND",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "LKR",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = LkrFormatter.format(state.totalSpend, showPrefix = false),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (state.spendDeltaPercent != null) {
                            val delta = state.spendDeltaPercent
                            val isLess = delta <= 0f
                            val absDelta = kotlin.math.abs(delta)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isLess) {
                                    SereneTheme.colors.incomeContainer.copy(alpha = 0.55f)
                                } else {
                                    SereneTheme.colors.expenseContainer.copy(alpha = 0.7f)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLess) {
                                            Icons.AutoMirrored.Outlined.TrendingDown
                                        } else {
                                            Icons.AutoMirrored.Outlined.TrendingUp
                                        },
                                        contentDescription = null,
                                        tint = if (isLess) SereneTheme.colors.income else SereneTheme.colors.expense,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format(
                                            Locale.US,
                                            "%.1f%% %s than %s",
                                            absDelta,
                                            if (isLess) "less" else "more",
                                            state.prevMonthShortLabel
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLess) SereneTheme.colors.income else SereneTheme.colors.expense
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Saved this month + Dual Split Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val savedPositive = state.savedThisMonth >= 0
                    val rateClamped = (state.savingsRatePercent / 100f).coerceIn(0f, 1f)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (savedPositive) {
                                                    SereneTheme.colors.incomeContainer
                                                } else {
                                                    SereneTheme.colors.expenseContainer
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Savings,
                                            contentDescription = null,
                                            tint = if (savedPositive) {
                                                SereneTheme.colors.onIncomeContainer
                                            } else {
                                                SereneTheme.colors.onExpenseContainer
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Saved this month",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = LkrFormatter.formatSigned(state.savedThisMonth, savedPositive),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (savedPositive) SereneTheme.colors.income else SereneTheme.colors.expense
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (savedPositive) {
                                        SereneTheme.colors.incomeContainer
                                    } else {
                                        SereneTheme.colors.expenseContainer
                                    }
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%%", state.savingsRatePercent),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (savedPositive) {
                                            SereneTheme.colors.onIncomeContainer
                                        } else {
                                            SereneTheme.colors.onExpenseContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { rateClamped },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (savedPositive) SereneTheme.colors.income else SereneTheme.colors.expense,
                                trackColor = SereneTheme.colors.surfaceContainerHigh,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SereneTheme.colors.primaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Restaurant,
                                        contentDescription = null,
                                        tint = SereneTheme.colors.onPrimaryFixed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Top Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = state.topCategoryName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = LkrFormatter.format(state.topCategoryAmount),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SereneTheme.colors.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Daily Average",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = LkrFormatter.format(state.dailyAverage),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "across ${state.daysInMonth} days",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Donut Chart & Category Breakdown
            item {
                val activeSlice = state.categoryBreakdown.firstOrNull {
                    it.categoryName == selectedCategoryName
                } ?: state.categoryBreakdown.firstOrNull()

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Expense Breakdown",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.categoryBreakdown.size} active spending categor${if (state.categoryBreakdown.size == 1) "y" else "ies"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { selectedCategoryName = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Reset slice selection",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (state.categoryBreakdown.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No expenses recorded in ${state.selectedMonth.label()}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                InteractiveDonutChart(
                                    items = state.categoryBreakdown,
                                    selectedCategory = activeSlice?.categoryName,
                                    trackColor = SereneTheme.colors.surfaceContainer,
                                    onSelectCategory = { name ->
                                        selectedCategoryName = if (selectedCategoryName == name) null else name
                                    }
                                )
                                if (activeSlice != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (selectedCategoryName == null) "Largest Share" else "Selected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${activeSlice.percentage.toInt()}%",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = activeSlice.categoryName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.categoryBreakdown.forEach { item ->
                                    val isHighlighted = item.categoryName == selectedCategoryName
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isHighlighted) {
                                                    SereneTheme.colors.surfaceContainerLow
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                selectedCategoryName =
                                                    if (selectedCategoryName == item.categoryName) null else item.categoryName
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(item.colorHex))
                                            )
                                            Column {
                                                Text(
                                                    text = item.categoryName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${item.percentage.toInt()}% of total spend",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = LkrFormatter.format(item.amount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6-Month Income vs Expense Bar Chart
            item {
                SixMonthComparisonCard(
                    selectedMonth = state.selectedMonth,
                    savedThisMonth = state.savedThisMonth,
                    bars = state.sixMonthSeries,
                    onSelectMonth = onSelectMonth
                )
            }

            // Export PDF Action
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val fileName = "Pocketwise_${state.selectedMonth.shortMonth()}_${state.selectedMonth.year}.pdf"
                            pdfExportLauncher.launch(fileName)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SereneTheme.colors.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download ${state.selectedMonth.shortMonth()} Tax & Spend PDF",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveDonutChart(
    items: List<CategoryBreakdownItem>,
    selectedCategory: String?,
    trackColor: Color,
    onSelectCategory: (String) -> Unit
) {
    val gapDegrees = if (items.size > 1) 4f else 0f
    val totalGap = gapDegrees * items.size
    val usableDegrees = (360f - totalGap).coerceAtLeast(1f)

    val sliceAngles = remember(items) {
        var currentStart = -90f
        items.map { item ->
            val sweep = (item.percentage / 100f) * usableDegrees
            val triple = Triple(item.categoryName, currentStart, sweep)
            currentStart += sweep + gapDegrees
            triple
        }
    }

    Canvas(
        modifier = Modifier
            .size(196.dp)
            .pointerInput(sliceAngles) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val dist = hypot(dx, dy)
                    val outerRadius = minOf(size.width, size.height) / 2f
                    val innerRadius = outerRadius * 0.45f
                    if (dist in innerRadius..outerRadius) {
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        if (angle < -90f) angle += 360f
                        val hit = sliceAngles.firstOrNull { (_, start, sweep) ->
                            angle >= start && angle <= start + sweep
                        }
                        if (hit != null) {
                            onSelectCategory(hit.first)
                        }
                    }
                }
            }
    ) {
        val strokeWidth = 24.dp.toPx()
        val inset = strokeWidth / 2f + 8.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth)
        )

        sliceAngles.forEachIndexed { index, (catName, startAngle, sweepAngle) ->
            val item = items[index]
            val isSelected = selectedCategory == null || selectedCategory == catName
            val alpha = if (isSelected) 1f else 0.35f
            val width = if (selectedCategory == catName) strokeWidth + 4.dp.toPx() else strokeWidth
            if (sweepAngle > 0.5f) {
                drawArc(
                    color = Color(item.colorHex).copy(alpha = alpha),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = width, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun SixMonthComparisonCard(
    selectedMonth: MonthYearKey,
    savedThisMonth: Double,
    bars: List<MonthlyComparisonBar>,
    onSelectMonth: (MonthYearKey) -> Unit
) {
    val maxVal = remember(bars) {
        bars.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }
    val selectedBar = remember(bars) {
        bars.firstOrNull { it.isSelected } ?: bars.lastOrNull()
    }

    val incomeColor = SereneTheme.colors.income
    val expenseColor = SereneTheme.colors.expense
    val surfaceColor = SereneTheme.colors.surfaceContainerLowest
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val selectedGuideColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    val selectedHighlightBg = SereneTheme.colors.surfaceContainerHigh.copy(alpha = 0.6f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Income vs. Expense",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Past 6 months trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(incomeColor)
                        )
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(expenseColor)
                        )
                        Text(
                            text = "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SereneTheme.colors.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedMonth.shortMonth()} Savings Surplus:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = LkrFormatter.formatSigned(savedThisMonth, savedThisMonth >= 0),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (savedThisMonth >= 0) incomeColor else expenseColor
                    )
                }
            }

            if (bars.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                            .testTag("insights_line_chart")
                            .pointerInput(bars) {
                                detectTapGestures { offset ->
                                    if (bars.isNotEmpty() && size.width > 0) {
                                        val stepX = size.width.toFloat() / bars.size
                                        val tappedIdx = (offset.x / stepX).toInt()
                                            .coerceIn(0, bars.lastIndex)
                                        onSelectMonth(bars[tappedIdx].monthKey)
                                    }
                                }
                            }
                    ) {
                        val topPad = 14.dp.toPx()
                        val bottomPad = 12.dp.toPx()
                        val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
                        val baselineY = topPad + chartHeight
                        val stepX = size.width / bars.size

                        // Subtle horizontal dashed gridlines (0%, 50%, 100%)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        listOf(0f, 0.5f, 1f).forEach { fraction ->
                            val y = topPad + chartHeight * fraction
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (fraction < 1f) dashEffect else null
                            )
                        }

                        val incomePoints = bars.mapIndexed { index, bar ->
                            val x = (index + 0.5f) * stepX
                            val ratio = (bar.income / maxVal).toFloat().coerceIn(0f, 1f)
                            val y = baselineY - ratio * chartHeight
                            Offset(x, y)
                        }

                        val expensePoints = bars.mapIndexed { index, bar ->
                            val x = (index + 0.5f) * stepX
                            val ratio = (bar.expense / maxVal).toFloat().coerceIn(0f, 1f)
                            val y = baselineY - ratio * chartHeight
                            Offset(x, y)
                        }

                        val selectedIndex = bars.indexOfFirst { it.isSelected }
                        if (selectedIndex >= 0) {
                            val selX = (selectedIndex + 0.5f) * stepX
                            drawLine(
                                color = selectedGuideColor,
                                start = Offset(selX, topPad),
                                end = Offset(selX, baselineY),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }

                        fun buildSmoothLinePath(points: List<Offset>): Path {
                            return Path().apply {
                                if (points.isEmpty()) return@apply
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }
                        }

                        fun buildAreaPath(linePath: Path, points: List<Offset>): Path {
                            return Path().apply {
                                addPath(linePath)
                                if (points.isNotEmpty()) {
                                    lineTo(points.last().x, baselineY)
                                    lineTo(points.first().x, baselineY)
                                    close()
                                }
                            }
                        }

                        val incomePath = buildSmoothLinePath(incomePoints)
                        val expensePath = buildSmoothLinePath(expensePoints)

                        // Soft area gradients under lines
                        drawPath(
                            path = buildAreaPath(incomePath, incomePoints),
                            brush = Brush.verticalGradient(
                                colors = listOf(incomeColor.copy(alpha = 0.16f), Color.Transparent),
                                startY = topPad,
                                endY = baselineY
                            )
                        )
                        drawPath(
                            path = buildAreaPath(expensePath, expensePoints),
                            brush = Brush.verticalGradient(
                                colors = listOf(expenseColor.copy(alpha = 0.12f), Color.Transparent),
                                startY = topPad,
                                endY = baselineY
                            )
                        )

                        // Income & Expense trend lines
                        drawPath(
                            path = incomePath,
                            color = incomeColor,
                            style = Stroke(
                                width = 2.75.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        drawPath(
                            path = expensePath,
                            color = expenseColor,
                            style = Stroke(
                                width = 2.75.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Data point markers
                        bars.forEachIndexed { index, bar ->
                            val incPt = incomePoints[index]
                            val expPt = expensePoints[index]
                            if (bar.isSelected) {
                                drawCircle(
                                    color = incomeColor.copy(alpha = 0.22f),
                                    radius = 9.dp.toPx(),
                                    center = incPt
                                )
                                drawCircle(
                                    color = incomeColor,
                                    radius = 5.dp.toPx(),
                                    center = incPt
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 2.dp.toPx(),
                                    center = incPt
                                )

                                drawCircle(
                                    color = expenseColor.copy(alpha = 0.22f),
                                    radius = 9.dp.toPx(),
                                    center = expPt
                                )
                                drawCircle(
                                    color = expenseColor,
                                    radius = 5.dp.toPx(),
                                    center = expPt
                                )
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 2.dp.toPx(),
                                    center = expPt
                                )
                            } else {
                                drawCircle(
                                    color = surfaceColor,
                                    radius = 3.5.dp.toPx(),
                                    center = incPt
                                )
                                drawCircle(
                                    color = incomeColor,
                                    radius = 3.5.dp.toPx(),
                                    center = incPt,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                drawCircle(
                                    color = surfaceColor,
                                    radius = 3.5.dp.toPx(),
                                    center = expPt
                                )
                                drawCircle(
                                    color = expenseColor,
                                    radius = 3.5.dp.toPx(),
                                    center = expPt,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                    }

                    // Aligned X-axis month selector labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bars.forEach { bar ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (bar.isSelected) selectedHighlightBg else Color.Transparent)
                                    .clickable { onSelectMonth(bar.monthKey) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bar.monthKey.shortMonth(),
                                    style = if (bar.isSelected) {
                                        MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    } else {
                                        MaterialTheme.typography.labelSmall
                                    },
                                    color = if (bar.isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    if (selectedBar != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${selectedBar.monthKey.shortMonth()} Income: ${LkrFormatter.formatCompactK(selectedBar.income)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = incomeColor
                            )
                            Text(
                                text = "${selectedBar.monthKey.shortMonth()} Expense: ${LkrFormatter.formatCompactK(selectedBar.expense)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = expenseColor
                            )
                        }
                    }
                }
            }
        }
    }
}
