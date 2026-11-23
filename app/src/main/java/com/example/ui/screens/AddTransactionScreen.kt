package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.theme.SereneTheme
import com.example.util.CategoryIcons
import com.example.util.DateFormatters
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

private val paymentMethodOptions = listOf(
    "Debit Card" to Icons.Outlined.CreditCard,
    "Cash" to Icons.Outlined.Payments,
    "Bank Transfer" to Icons.Outlined.AccountBalance,
    "LANKAQR" to Icons.Outlined.QrCodeScanner,
    "Digital Wallet" to Icons.Outlined.AccountBalanceWallet
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    existingTransaction: TransactionEntity?,
    initialType: TransactionType,
    bankName: String,
    categories: List<CategoryEntity>,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = existingTransaction != null
    val txType = existingTransaction?.type ?: initialType
    val isIncome = txType == TransactionType.INCOME
    var amountText by remember(existingTransaction) {
        mutableStateOf(
            if (existingTransaction != null) {
                String.format(Locale.US, "%.2f", existingTransaction.amount)
            } else {
                ""
            }
        )
    }
    var titleText by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.title ?: "")
    }
    var selectedCategory by remember(existingTransaction, initialType) {
        mutableStateOf(
            existingTransaction?.categoryName
                ?: if (initialType == TransactionType.INCOME) "Salary" else "Food"
        )
    }
    var selectedPaymentMethod by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.paymentMethod ?: "Debit Card")
    }
    var selectedTimestamp by remember(existingTransaction) {
        mutableLongStateOf(existingTransaction?.timestamp ?: System.currentTimeMillis())
    }
    var receiptUri by remember(existingTransaction) {
        mutableStateOf(existingTransaction?.receiptImageUri)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isSavingReceipt by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val sessionCreatedUris = remember(existingTransaction?.id) { mutableListOf<String>() }

    LaunchedEffect(existingTransaction?.id, existingTransaction?.receiptImageUri) {
        val current = existingTransaction?.receiptImageUri
        if (!current.isNullOrBlank() && current.startsWith("content://")) {
            val migrated = ImageStorageHelper.persistPickedImage(
                context = context,
                sourceUri = Uri.parse(current),
                subdirectory = ImageStorageHelper.RECEIPT_IMAGES_DIR
            )
            if (migrated != null) {
                sessionCreatedUris.add(migrated)
                receiptUri = migrated
            }
        }
    }

    val cancelAndCleanup = {
        sessionCreatedUris.forEach { uriStr ->
            ImageStorageHelper.deleteOwnedImage(context, uriStr)
        }
        sessionCreatedUris.clear()
        onCancel()
    }

    BackHandler(onBack = cancelAndCleanup)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isSavingReceipt = true
            validationError = null
            scope.launch {
                val persistedUri = ImageStorageHelper.persistPickedImage(
                    context = context,
                    sourceUri = uri,
                    subdirectory = ImageStorageHelper.RECEIPT_IMAGES_DIR
                )
                isSavingReceipt = false
                if (persistedUri != null) {
                    sessionCreatedUris.add(persistedUri)
                    receiptUri = persistedUri
                } else {
                    validationError = "Couldn't save the selected receipt photo. Please try another image."
                }
            }
        }
    }

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
                .navigationBarsPadding()
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SereneTheme.colors.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Header Indicator & Type/ID Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isIncome) SereneTheme.colors.incomeContainer else SereneTheme.colors.tertiaryFixed
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isIncome) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isIncome) SereneTheme.colors.onIncomeContainer else SereneTheme.colors.onTertiaryFixed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = when {
                                    isEdit && isIncome -> "Edit Income"
                                    isEdit -> "Edit Expense"
                                    isIncome -> "New Income"
                                    else -> "New Expense"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = if (existingTransaction != null) {
                                SereneTheme.colors.surfaceContainerHigh
                            } else if (isIncome) {
                                SereneTheme.colors.incomeContainer
                            } else {
                                SereneTheme.colors.tertiaryFixed
                            },
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (existingTransaction != null) {
                                    "ID #LK-${existingTransaction.id + 8400}"
                                } else if (isIncome) {
                                    "INCOME"
                                } else {
                                    "EXPENSE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (existingTransaction != null) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else if (isIncome) {
                                    SereneTheme.colors.onIncomeContainer
                                } else {
                                    SereneTheme.colors.onTertiaryFixed
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Prominent Numeric Amount Input
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SereneTheme.colors.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (txType == TransactionType.INCOME) "AMOUNT RECEIVED" else "AMOUNT SPENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "LKR",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.widthIn(min = 90.dp, max = 230.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            style = MaterialTheme.typography.displayMedium,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    BasicTextField(
                                        value = amountText,
                                        onValueChange = { input ->
                                            val cleaned = input.filter { it.isDigit() || it == '.' }
                                            if (cleaned.count { it == '.' } <= 1) {
                                                amountText = cleaned
                                                validationError = null
                                            }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        textStyle = MaterialTheme.typography.displayMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.testTag("tx_amount_input")
                                    )
                                }
                            }
                            val isCashSelected = selectedPaymentMethod.equals("Cash", ignoreCase = true)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCashSelected) Icons.Outlined.Payments else Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isCashSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isCashSelected) {
                                        "Cash • Recorded only, won't affect $bankName balance"
                                    } else {
                                        "$bankName ($selectedPaymentMethod)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCashSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Description Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SereneTheme.colors.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (titleText.isEmpty()) {
                                        Text(
                                            text = "What was this for?",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                    BasicTextField(
                                        value = titleText,
                                        onValueChange = {
                                            titleText = it
                                            validationError = null
                                        },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("tx_description_input")
                                    )
                                }
                                if (titleText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { titleText = "" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Cancel,
                                            contentDescription = "Clear description",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${categories.size} Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(categories, key = { it.id }) { cat ->
                                val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
                                Surface(
                                    onClick = { selectedCategory = cat.name },
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else SereneTheme.colors.surfaceContainer,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("tx_cat_chip_${cat.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Outlined.Check else CategoryIcons.iconFor(cat.iconKey),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${paymentMethodOptions.size} Available",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(paymentMethodOptions) { (methodName, methodIcon) ->
                                val isSelected = selectedPaymentMethod == methodName
                                Surface(
                                    onClick = { selectedPaymentMethod = methodName },
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else SereneTheme.colors.surfaceContainer,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .testTag("tx_payment_chip_$methodName")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Outlined.Check else methodIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = methodName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedPaymentMethod.equals("Cash", ignoreCase = true)) {
                            Text(
                                text = "Cash payments are documented in your activity & insights without changing your bank balance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // Date Picker Row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SereneTheme.colors.surfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(SereneTheme.colors.surfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Date",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = DateFormatters.formatPickerDate(selectedTimestamp),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { showDatePicker = true },
                                    shape = CircleShape,
                                    color = SereneTheme.colors.surfaceContainerHigh,
                                    modifier = Modifier.testTag("tx_change_date_button")
                                ) {
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Logged at ${DateFormatters.formatTime(selectedTimestamp)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (receiptUri != null) "Receipt Attached" else "Attach Receipt",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Receipt Preview Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SereneTheme.colors.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        if (isSavingReceipt) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text(
                                    text = "Saving receipt photo...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (receiptUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = receiptUri,
                                    contentDescription = "Attached receipt photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    error = {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SereneTheme.colors.surfaceContainerHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Receipt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = SereneTheme.colors.income,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Receipt Photo Attached",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SereneTheme.colors.income
                                        )
                                    }
                                    Text(
                                        text = titleText.ifBlank { "Transaction Receipt" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Tap to replace photo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { receiptUri = null }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove receipt",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap to attach a receipt photo from Gallery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (validationError != null) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            enabled = !isSavingReceipt,
                            onClick = {
                                val parsedAmount = amountText.replace(",", "").trim().toDoubleOrNull()
                                if (parsedAmount == null || parsedAmount <= 0.0) {
                                    validationError = "Please enter a valid amount greater than 0."
                                    return@Button
                                }
                                val finalTitle = titleText.trim().ifBlank { selectedCategory }
                                sessionCreatedUris
                                    .filter { it != receiptUri }
                                    .forEach { ImageStorageHelper.deleteOwnedImage(context, it) }
                                sessionCreatedUris.clear()
                                val previousUri = existingTransaction?.receiptImageUri
                                if (previousUri != null && previousUri != receiptUri) {
                                    ImageStorageHelper.deleteOwnedImage(context, previousUri)
                                }
                                onSave(
                                    TransactionEntity(
                                        id = existingTransaction?.id ?: 0L,
                                        title = finalTitle,
                                        amount = parsedAmount,
                                        type = txType,
                                        categoryName = selectedCategory,
                                        paymentMethod = selectedPaymentMethod,
                                        timestamp = selectedTimestamp,
                                        receiptImageUri = receiptUri,
                                        notes = existingTransaction?.notes ?: ""
                                    )
                                )
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("save_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isEdit -> "Save Entry"
                                    isIncome -> "Save Income"
                                    else -> "Save Expense"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        if (existingTransaction != null) {
                            Button(
                                onClick = { showDeleteConfirm = true },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("delete_transaction_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Delete Entry", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            TextButton(
                                onClick = cancelAndCleanup,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            val pickedCal = Calendar.getInstance().apply { timeInMillis = pickedUtcMillis }
                            val currentCal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                            currentCal.set(Calendar.YEAR, pickedCal.get(Calendar.YEAR))
                            currentCal.set(Calendar.MONTH, pickedCal.get(Calendar.MONTH))
                            currentCal.set(Calendar.DAY_OF_MONTH, pickedCal.get(Calendar.DAY_OF_MONTH))
                            selectedTimestamp = currentCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && existingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction?") },
            text = { Text("Remove \"${existingTransaction.title}\" and revert its effect on your bank balance?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(existingTransaction)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
