package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.SereneTheme
import com.example.util.LkrFormatter

@Composable
fun GoalsScreen(
    goals: List<SavingsGoalEntity>,
    bankBalance: Double,
    onEditGoal: (Long) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onAllocateGoal: (SavingsGoalEntity, Double) -> Unit,
    onMarkBought: (SavingsGoalEntity) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeGoals = remember(goals) { goals.filter { !it.isPurchased } }
    val purchasedGoals = remember(goals) { goals.filter { it.isPurchased } }

    val totalWishlistValue = remember(activeGoals) { activeGoals.sumOf { it.targetAmount } }
    val readyCount = remember(activeGoals, bankBalance) {
        activeGoals.count { (bankBalance + it.allocatedAmount) >= it.targetAmount }
    }

    var goalToAllocate by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalToBuy by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

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
            // Summary Banner
            item {
                val glowColor = SereneTheme.colors.primaryFixed.copy(alpha = 0.25f)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(glowColor, Color.Transparent),
                                    center = Offset(size.width * 0.9f, size.height * 0.85f),
                                    radius = size.width * 0.4f
                                ),
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.9f, size.height * 0.85f)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SereneTheme.colors.surfaceContainerHigh,
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.BookmarkBorder,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Wishlist Tracker",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = "${activeGoals.size} Planned Purchase${if (activeGoals.size == 1) "" else "s"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column {
                                Text(
                                    text = "Things I want to buy",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Track items and know when your bank balance can afford them",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL WISHLIST VALUE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = LkrFormatter.format(totalWishlistValue),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = SereneTheme.colors.income,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$readyCount Ready to Buy",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SereneTheme.colors.income
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Wishlist Cards
            items(activeGoals, key = { it.id }) { goal ->
                GoalWishlistCard(
                    goal = goal,
                    bankBalance = bankBalance,
                    onEdit = { onEditGoal(goal.id) },
                    onDelete = { goalToDelete = goal },
                    onOpenLink = {
                        val rawUrl = goal.storeUrl.trim()
                        if (rawUrl.isEmpty()) {
                            onShowMessage("No store link saved for this goal")
                        } else {
                            val formattedUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                                rawUrl
                            } else {
                                "https://$rawUrl"
                            }
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)))
                            }.onFailure {
                                onShowMessage("Could not open link")
                            }
                        }
                    },
                    onBoughtClick = { goalToBuy = goal },
                    onAllocateClick = { goalToAllocate = goal }
                )
            }

            if (purchasedGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "PURCHASED (${purchasedGoals.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
                items(purchasedGoals, key = { "purchased_${it.id}" }) { goal ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SereneTheme.colors.surfaceContainerLowest,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = SereneTheme.colors.income
                                )
                                Column {
                                    Text(
                                        text = goal.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Purchased • ${LkrFormatter.format(goal.targetAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            TextButton(onClick = { goalToDelete = goal }) {
                                Text("Remove", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Bottom Helper Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SereneTheme.colors.surfaceContainerLow,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SereneTheme.colors.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Have something else in mind?",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap + to add any product link or price target to your wishlist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (goalToAllocate != null) {
        AllocateGoalDialog(
            goal = goalToAllocate!!,
            onDismiss = { goalToAllocate = null },
            onConfirm = { amount ->
                onAllocateGoal(goalToAllocate!!, amount)
                onShowMessage("Reserved ${LkrFormatter.format(amount)} toward ${goalToAllocate!!.name}")
                goalToAllocate = null
            }
        )
    }

    if (goalToBuy != null) {
        val g = goalToBuy!!
        AlertDialog(
            onDismissRequest = { goalToBuy = null },
            title = { Text("Mark as Purchased?") },
            text = {
                Text(
                    "Record an expense of ${LkrFormatter.format(g.targetAmount)} for \"${g.name}\" and deduct it from your bank balance?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkBought(g)
                        onShowMessage("Purchased ${g.name}!")
                        goalToBuy = null
                    }
                ) {
                    Text("Confirm Purchase")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToBuy = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (goalToDelete != null) {
        val g = goalToDelete!!
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal?") },
            text = { Text("Remove \"${g.name}\" from your wishlist?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGoal(g)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GoalWishlistCard(
    goal: SavingsGoalEntity,
    bankBalance: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenLink: () -> Unit,
    onBoughtClick: () -> Unit,
    onAllocateClick: () -> Unit
) {
    val effectiveAmount = (bankBalance + goal.allocatedAmount).coerceAtMost(goal.targetAmount)
    val progress = if (goal.targetAmount > 0) (effectiveAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val pctInt = (progress * 100).toInt()
    val isReady = progress >= 1f
    val remaining = (goal.targetAmount - effectiveAmount).coerceAtLeast(0.0)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goal.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cover image + badges + Edit/Delete buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.79f)
                    .background(SereneTheme.colors.surfaceContainer)
            ) {
                if (!goal.imageUri.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = goal.imageUri,
                        contentDescription = goal.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = {
                            GoalImagePlaceholder(goalName = goal.name)
                        }
                    )
                } else {
                    GoalImagePlaceholder(goalName = goal.name)
                }

                // Top-left status badge
                Surface(
                    color = if (isReady) {
                        SereneTheme.colors.incomeContainer
                    } else {
                        SereneTheme.colors.surfaceContainerLowest.copy(alpha = 0.92f)
                    },
                    shape = CircleShape,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isReady) Icons.Outlined.Verified else Icons.Outlined.HourglassTop,
                            contentDescription = null,
                            tint = if (isReady) SereneTheme.colors.onIncomeContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isReady) "Ready to Buy" else "$pctInt% Funded",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isReady) SereneTheme.colors.onIncomeContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Top-right edit & delete actions
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = onEdit,
                        shape = CircleShape,
                        color = SereneTheme.colors.surfaceContainerLowest.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .testTag("edit_goal_${goal.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit goal",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = SereneTheme.colors.surfaceContainerLowest.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .testTag("delete_goal_${goal.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete goal",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Content body
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = goal.storeLabel.ifBlank { "Savings Goal" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LkrFormatter.format(goal.targetAmount),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Progress Track & Labels
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (isReady) SereneTheme.colors.income else MaterialTheme.colorScheme.primaryContainer,
                        trackColor = SereneTheme.colors.surfaceContainerHigh,
                        strokeCap = StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isReady) Icons.Outlined.CheckCircle else Icons.Outlined.Savings,
                                contentDescription = null,
                                tint = if (isReady) SereneTheme.colors.income else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isReady) {
                                    "You can afford this now!"
                                } else {
                                    "Need ${LkrFormatter.format(remaining)} more"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isReady) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = if (isReady) SereneTheme.colors.income else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (isReady) {
                                "100%"
                            } else {
                                "${LkrFormatter.formatCompactK(effectiveAmount)} / ${LkrFormatter.formatCompactK(goal.targetAmount)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenLink,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReady) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                SereneTheme.colors.surfaceContainerHigh
                            },
                            contentColor = if (isReady) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Open link", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (isReady) {
                        Button(
                            onClick = onBoughtClick,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SereneTheme.colors.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("goal_bought_${goal.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bought", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = onAllocateClick,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SereneTheme.colors.primaryFixed,
                                contentColor = SereneTheme.colors.onPrimaryFixed
                            ),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("goal_allocate_${goal.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Allocate", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalImagePlaceholder(goalName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SereneTheme.colors.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = goalName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun AllocateGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("10000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allocate Funds to Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Reserve savings specifically for \"${goal.name}\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (LKR)") },
                    prefix = { Text("LKR ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5000, 10000, 25000).forEach { preset ->
                        Surface(
                            onClick = { amountText = preset.toString() },
                            shape = CircleShape,
                            color = SereneTheme.colors.surfaceContainerHigh
                        ) {
                            Text(
                                text = "+${preset / 1000}k",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amountText.toDoubleOrNull() ?: 0.0
                    if (value > 0.0) onConfirm(value)
                }
            ) {
                Text("Allocate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
