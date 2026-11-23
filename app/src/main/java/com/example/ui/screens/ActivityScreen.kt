package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.theme.SereneTheme
import com.example.ui.viewmodel.ActivityFilterState
import com.example.ui.viewmodel.ActivityTypeFilter
import com.example.ui.viewmodel.MonthYearKey
import com.example.util.CategoryIcons
import com.example.util.DateFormatters
import com.example.util.LkrFormatter
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    allTransactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    filterState: ActivityFilterState,
    availableMonths: List<MonthYearKey>,
    onSearchQueryChange: (String) -> Unit,
    onMonthSelect: (MonthYearKey?) -> Unit,
    onTypeFilterSelect: (ActivityTypeFilter) -> Unit,
    onCategoryFilterSelect: (String?) -> Unit,
    onResetFilters: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSwipeHint by remember { mutableStateOf(true) }

    val categoryIconMap = remember(categories) {
        categories.associate { it.name.lowercase() to it.iconKey }
    }

    val monthFiltered = remember(allTransactions, filterState.selectedMonth) {
        val targetMonth = filterState.selectedMonth
        if (targetMonth == null) {
            allTransactions
        } else {
            allTransactions.filter { tx ->
                val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                c.get(Calendar.YEAR) == targetMonth.year &&
                    c.get(Calendar.MONTH) == targetMonth.monthZeroBased
            }
        }
    }

    val monthIncomeTotal = remember(monthFiltered) {
        monthFiltered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val monthExpenseTotal = remember(monthFiltered) {
        monthFiltered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val filteredTransactions = remember(monthFiltered, filterState) {
        monthFiltered.filter { tx ->
            val matchesType = when (filterState.typeFilter) {
                ActivityTypeFilter.ALL -> true
                ActivityTypeFilter.INCOME -> tx.type == TransactionType.INCOME
                ActivityTypeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
            }
            val matchesCat = filterState.categoryFilter == null ||
                tx.categoryName.equals(filterState.categoryFilter, ignoreCase = true)
            val query = filterState.searchQuery.trim().lowercase(Locale.US)
            val matchesQuery = query.isEmpty() ||
                tx.title.lowercase(Locale.US).contains(query) ||
                tx.categoryName.lowercase(Locale.US).contains(query) ||
                tx.paymentMethod.lowercase(Locale.US).contains(query) ||
                tx.notes.lowercase(Locale.US).contains(query)
            matchesType && matchesCat && matchesQuery
        }
    }

    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            DateFormatters.formatDayGroupHeader(tx.timestamp)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Month Selector Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SereneTheme.colors.surfaceContainer,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (filterState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search transactions...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = filterState.searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("activity_search_input")
                                )
                            }
                            if (filterState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box {
                        Surface(
                            onClick = { showMonthMenu = true },
                            shape = CircleShape,
                            color = SereneTheme.colors.surfaceContainer,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("activity_month_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = filterState.selectedMonth?.shortLabel() ?: "All",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMonthMenu,
                            onDismissRequest = { showMonthMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Months") },
                                onClick = {
                                    onMonthSelect(null)
                                    showMonthMenu = false
                                }
                            )
                            availableMonths.forEach { monthKey ->
                                DropdownMenuItem(
                                    text = { Text(monthKey.shortLabel()) },
                                    onClick = {
                                        onMonthSelect(monthKey)
                                        showMonthMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips Carousel
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        val isSelected = filterState.typeFilter == ActivityTypeFilter.ALL &&
                            filterState.categoryFilter == null
                        Surface(
                            onClick = {
                                onTypeFilterSelect(ActivityTypeFilter.ALL)
                                onCategoryFilterSelect(null)
                            },
                            shape = CircleShape,
                            color = if (isSelected) SereneTheme.colors.primaryFixed else SereneTheme.colors.surfaceContainer,
                            shadowElevation = if (isSelected) 1.dp else 0.dp,
                            modifier = Modifier.testTag("filter_chip_all")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = SereneTheme.colors.onPrimaryFixed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "All",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) SereneTheme.colors.onPrimaryFixed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${monthFiltered.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        val isSelected = filterState.typeFilter == ActivityTypeFilter.INCOME
                        Surface(
                            onClick = {
                                onTypeFilterSelect(
                                    if (isSelected) ActivityTypeFilter.ALL else ActivityTypeFilter.INCOME
                                )
                            },
                            shape = CircleShape,
                            color = if (isSelected) SereneTheme.colors.incomeContainer else SereneTheme.colors.surfaceContainer,
                            modifier = Modifier.testTag("filter_chip_income")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDownward,
                                    contentDescription = null,
                                    tint = SereneTheme.colors.income,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Income",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) SereneTheme.colors.onIncomeContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+ LKR ${LkrFormatter.formatWhole(monthIncomeTotal)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = SereneTheme.colors.income
                                )
                            }
                        }
                    }

                    item {
                        val isSelected = filterState.typeFilter == ActivityTypeFilter.EXPENSE
                        Surface(
                            onClick = {
                                onTypeFilterSelect(
                                    if (isSelected) ActivityTypeFilter.ALL else ActivityTypeFilter.EXPENSE
                                )
                            },
                            shape = CircleShape,
                            color = if (isSelected) SereneTheme.colors.expenseContainer else SereneTheme.colors.surfaceContainer,
                            modifier = Modifier.testTag("filter_chip_expense")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowUpward,
                                    contentDescription = null,
                                    tint = SereneTheme.colors.expense,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Expenses",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) SereneTheme.colors.onExpenseContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "– LKR ${LkrFormatter.formatWhole(monthExpenseTotal)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = SereneTheme.colors.expense
                                )
                            }
                        }
                    }

                    item {
                        val hasCat = filterState.categoryFilter != null
                        Box {
                            Surface(
                                onClick = { showCategoryMenu = true },
                                shape = CircleShape,
                                color = if (hasCat) SereneTheme.colors.primaryFixed else SereneTheme.colors.surfaceContainer,
                                modifier = Modifier.testTag("filter_chip_categories")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Category,
                                        contentDescription = null,
                                        tint = if (hasCat) SereneTheme.colors.onPrimaryFixed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = filterState.categoryFilter ?: "Categories",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (hasCat) SereneTheme.colors.onPrimaryFixed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Categories") },
                                    onClick = {
                                        onCategoryFilterSelect(null)
                                        showCategoryMenu = false
                                    }
                                )
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            onCategoryFilterSelect(cat.name)
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Swipe / Tap Delight Hint
            item {
                AnimatedVisibility(visible = showSwipeHint && filteredTransactions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SereneTheme.colors.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSwipeHint = false }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TouchApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Tip: Swipe left to delete, tap any item to edit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.SwipeLeft,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Grouped Transactions OR Empty State
            if (filteredTransactions.isEmpty()) {
                item {
                    ActivityEmptyStateCard(onResetFilters = onResetFilters)
                }
            } else {
                groupedByDay.forEach { (dayHeader, dayTransactions) ->
                    val dayNet = dayTransactions.sumOf {
                        if (it.type == TransactionType.INCOME) it.amount else -it.amount
                    }
                    item(key = "header_$dayHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayHeader.uppercase(Locale.US),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = LkrFormatter.formatSigned(dayNet, dayNet >= 0),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (dayNet > 0) SereneTheme.colors.income else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    items(
                        items = dayTransactions,
                        key = { it.id }
                    ) { tx ->
                        val iconKey = categoryIconMap[tx.categoryName.lowercase()] ?: tx.categoryName
                        SwipeableTransactionRow(
                            transaction = tx,
                            iconKey = iconKey,
                            onClick = { onTransactionClick(tx.id) },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionRow(
    transaction: TransactionEntity,
    iconKey: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete transaction",
                        tint = MaterialTheme.colorScheme.onError
                    )
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    ) {
        val isIncome = transaction.type == TransactionType.INCOME
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag("tx_row_${transaction.id}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncome) {
                                    SereneTheme.colors.incomeContainer.copy(alpha = 0.7f)
                                } else {
                                    SereneTheme.colors.surfaceContainerHigh
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.iconFor(iconKey),
                            contentDescription = null,
                            tint = if (isIncome) {
                                SereneTheme.colors.onIncomeContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${transaction.categoryName} • ${DateFormatters.formatTime(transaction.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = LkrFormatter.formatSigned(transaction.amount, isIncome),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isIncome) SereneTheme.colors.income else SereneTheme.colors.expense,
                        maxLines = 1
                    )
                    Text(
                        text = transaction.paymentMethod,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isIncome) SereneTheme.colors.income else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityEmptyStateCard(onResetFilters: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SereneTheme.colors.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SereneTheme.colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Nothing yet. Add your first entry.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Filtered records or searched items will display here. Tap the teal plus button anytime to record income or expenses.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onResetFilters,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SereneTheme.colors.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("reset_filters_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Filters", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
