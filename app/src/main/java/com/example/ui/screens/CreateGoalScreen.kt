package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.SavingsGoalEntity
import com.example.ui.theme.SereneTheme
import com.example.util.HotlinkedImages
import com.example.util.LkrFormatter
import java.util.Locale

@Composable
fun CreateGoalScreen(
    existingGoal: SavingsGoalEntity?,
    bankBalance: Double,
    onSave: (SavingsGoalEntity) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = existingGoal != null
    var itemName by remember(existingGoal) { mutableStateOf(existingGoal?.name ?: "") }
    var targetPriceText by remember(existingGoal) {
        mutableStateOf(
            if (existingGoal != null) String.format(Locale.US, "%.2f", existingGoal.targetAmount) else ""
        )
    }
    var storeUrl by remember(existingGoal) { mutableStateOf(existingGoal?.storeUrl ?: "") }
    var storeLabel by remember(existingGoal) { mutableStateOf(existingGoal?.storeLabel ?: "") }
    var notes by remember(existingGoal) { mutableStateOf(existingGoal?.notes ?: "") }
    var imageUri by remember(existingGoal) { mutableStateOf(existingGoal?.imageUri) }
    var allocatedAmount by remember(existingGoal) {
        mutableDoubleStateOf(existingGoal?.allocatedAmount ?: 0.0)
    }
    var showWebUrlDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    val parsedPrice = targetPriceText.replace(",", "").trim().toDoubleOrNull() ?: 0.0
    val availableTotal = bankBalance + allocatedAmount
    val isAffordableNow = parsedPrice > 0.0 && availableTotal >= parsedPrice
    val deficit = (parsedPrice - availableTotal).coerceAtLeast(0.0)
    val progressFraction = if (parsedPrice > 0.0) {
        (availableTotal / parsedPrice).toFloat().coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Header & Synced Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SereneTheme.colors.surfaceContainer
                ) {
                    Text(
                        text = if (isEdit) "Edit Existing Goal" else "New Goal",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SereneTheme.colors.incomeContainer.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SereneTheme.colors.income)
                        )
                        Text(
                            text = "Linked to Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = SereneTheme.colors.onIncomeContainer
                        )
                    }
                }
            }

            // Image Picker Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Wishlist Item",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (imageUri != null) "Change Photo" else "Sample Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            if (imageUri != null) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } else {
                                imageUri = HotlinkedImages.GOAL_HEADPHONES_PREVIEW
                            }
                        }
                    )
                }

                if (imageUri == null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SereneTheme.colors.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SereneTheme.colors.primaryFixed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    tint = SereneTheme.colors.onPrimaryFixed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Upload product picture",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Supports device gallery photos or paste an image URL directly",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = CircleShape,
                                    color = SereneTheme.colors.surfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Choose Gallery",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { showWebUrlDialog = true },
                                    shape = CircleShape,
                                    color = SereneTheme.colors.surfaceContainerHighest
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Link,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "From Web",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SereneTheme.colors.surfaceContainerHighest)
                    ) {
                        SubcomposeAsyncImage(
                            model = imageUri,
                            contentDescription = "Selected goal image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Product photo attached",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                            Surface(
                                onClick = { imageUri = null },
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Item Name
            GoalInputContainer(label = "Item Name") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (itemName.isEmpty()) {
                            Text(
                                text = "e.g. Wireless Noise Cancelling Headphones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        BasicTextField(
                            value = itemName,
                            onValueChange = {
                                itemName = it
                                validationError = null
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_name_input")
                        )
                    }
                }
            }

            // Target Amount
            GoalInputContainer(label = "Target Amount") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LKR",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (targetPriceText.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        BasicTextField(
                            value = targetPriceText,
                            onValueChange = { raw ->
                                val cleaned = raw.filter { it.isDigit() || it == '.' }
                                if (cleaned.count { it == '.' } <= 1) {
                                    targetPriceText = cleaned
                                    validationError = null
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_amount_input")
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = SereneTheme.colors.income,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Store / Marketplace URL
            GoalInputContainer(label = "Store or Marketplace URL", optional = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (storeUrl.isEmpty()) {
                            Text(
                                text = "Paste URL from Daraz, Takas, etc.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        BasicTextField(
                            value = storeUrl,
                            onValueChange = { storeUrl = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_url_input")
                        )
                    }
                }
            }

            // Notes
            GoalInputContainer(label = "Notes & Commute Target", optional = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (notes.isEmpty()) {
                            Text(
                                text = "Why do you want this or when do you plan to buy?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        BasicTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            minLines = 2,
                            maxLines = 3,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Live Affordability Status Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isAffordableNow) {
                    SereneTheme.colors.incomeContainer
                } else {
                    SereneTheme.colors.surfaceContainerHigh
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAffordableNow) SereneTheme.colors.income else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAffordableNow) {
                                Icons.Outlined.SentimentVerySatisfied
                            } else {
                                Icons.Outlined.Schedule
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Affordability Status",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isAffordableNow) {
                                SereneTheme.colors.onIncomeContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                parsedPrice <= 0.0 -> "Enter a target amount to check affordability against your current balance of ${LkrFormatter.format(bankBalance)}."
                                isAffordableNow -> "Ready to buy now with current balance of ${LkrFormatter.format(bankBalance)}."
                                else -> "Requires ${LkrFormatter.format(deficit)} more to safely purchase without dipping below zero."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAffordableNow) {
                                SereneTheme.colors.onIncomeContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (isAffordableNow) SereneTheme.colors.income else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Quick Milestone Assist Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    QuickDeltaChip(label = "+ LKR 10,000") {
                        val next = parsedPrice + 10000.0
                        targetPriceText = String.format(Locale.US, "%.2f", next)
                    }
                }
                item {
                    QuickDeltaChip(label = "+ LKR 25,000") {
                        val next = parsedPrice + 25000.0
                        targetPriceText = String.format(Locale.US, "%.2f", next)
                    }
                }
                item {
                    Surface(
                        onClick = {
                            allocatedAmount = if (allocatedAmount == 0.0) 5000.0 else 0.0
                        },
                        shape = CircleShape,
                        color = if (allocatedAmount > 0.0) SereneTheme.colors.primaryFixed else SereneTheme.colors.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (allocatedAmount > 0.0) "Reserved ${LkrFormatter.formatCompactK(allocatedAmount)}" else "Fast Reserve",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Save & Discard Actions
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (itemName.isBlank()) {
                            validationError = "Please enter an item name."
                            return@Button
                        }
                        if (parsedPrice <= 0.0) {
                            validationError = "Please enter a target amount greater than 0."
                            return@Button
                        }
                        val derivedStoreLabel = storeLabel.ifBlank {
                            runCatching {
                                val host = Uri.parse(
                                    if (storeUrl.startsWith("http")) storeUrl else "https://$storeUrl"
                                ).host.orEmpty().removePrefix("www.")
                                host.replaceFirstChar { it.uppercase() }
                            }.getOrDefault("").ifBlank { "Savings Goal" }
                        }
                        onSave(
                            SavingsGoalEntity(
                                id = existingGoal?.id ?: 0L,
                                name = itemName.trim(),
                                targetAmount = parsedPrice,
                                allocatedAmount = allocatedAmount,
                                storeUrl = storeUrl.trim(),
                                storeLabel = derivedStoreLabel,
                                imageUri = imageUri,
                                notes = notes.trim(),
                                createdAt = existingGoal?.createdAt ?: System.currentTimeMillis(),
                                isPurchased = existingGoal?.isPurchased ?: false
                            )
                        )
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdded,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEdit) "Update Goal" else "Save Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = "Discard & Close",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showWebUrlDialog) {
        var urlInput by remember { mutableStateOf(imageUri ?: "") }
        AlertDialog(
            onDismissRequest = { showWebUrlDialog = false },
            title = { Text("Image URL") },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Paste product image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            imageUri = urlInput.trim()
                        }
                        showWebUrlDialog = false
                    }
                ) {
                    Text("Use Image")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWebUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GoalInputContainer(
    label: String,
    optional: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (optional) {
                Text(
                    text = "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SereneTheme.colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun QuickDeltaChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = SereneTheme.colors.surfaceContainer
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
