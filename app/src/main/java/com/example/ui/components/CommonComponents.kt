package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PrimaryContainerTeal
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SereneTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem("activity", "Activity", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    BottomNavItem("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag),
    BottomNavItem("insights", "Insights", Icons.Filled.PieChart, Icons.Outlined.PieChart)
)

@Composable
fun BrandEmblem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryTeal, PrimaryContainerTeal)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AccountBalanceWallet,
            contentDescription = null,
            tint = PrimaryFixed,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
fun ProfileAvatar(
    userName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("profile_avatar_button"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_profile_avatar_minimal_1795417639564),
            contentDescription = "$userName profile avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                .scale(1.22f)
        )
    }
}

@Composable
fun FinanceTabTopBar(
    subtitle: String,
    userName: String,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BrandEmblem()
                Column {
                    Text(
                        text = "Pocketwise",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ProfileAvatar(userName = userName, onClick = onProfileClick)
            }
        }
    }
}

@Composable
fun FinanceStackTopBar(
    title: String,
    userName: String,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                BrandEmblem()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            ProfileAvatar(userName = userName, onClick = onProfileClick)
        }
    }
}

@Composable
fun FinanceBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(76.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${item.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(if (selected) 60.dp else 36.dp)
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) SereneTheme.colors.primaryFixed else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (selected) {
                                SereneTheme.colors.onPrimaryFixed
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        style = if (selected) {
                            MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        } else {
                            MaterialTheme.typography.labelSmall
                        },
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        ),
        modifier = modifier
            .size(56.dp)
            .testTag("main_fab")
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun FinanceTransactionSpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(stiffness = 450f, dampingRatio = 0.75f),
        label = "fab_rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = spring(stiffness = 500f)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = 500f, dampingRatio = 0.78f)
                ) +
                scaleIn(initialScale = 0.85f),
            exit = fadeOut(animationSpec = spring(stiffness = 550f)) +
                slideOutVertically(targetOffsetY = { it / 2 }) +
                scaleOut(targetScale = 0.85f)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onAddIncome,
                        shape = CircleShape,
                        color = SereneTheme.colors.surfaceContainerLowest,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = SereneTheme.colors.income,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddIncome,
                        shape = CircleShape,
                        containerColor = SereneTheme.colors.incomeContainer,
                        contentColor = SereneTheme.colors.onIncomeContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 6.dp
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("fab_add_income")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Income",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onAddExpense,
                        shape = CircleShape,
                        color = SereneTheme.colors.surfaceContainerLowest,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Expense",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = onAddExpense,
                        shape = CircleShape,
                        containerColor = SereneTheme.colors.tertiaryFixed,
                        contentColor = SereneTheme.colors.onTertiaryFixed,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 6.dp
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("fab_add_expense")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Add Expense",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggle,
            shape = CircleShape,
            containerColor = if (expanded) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (expanded) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            ),
            modifier = Modifier
                .size(56.dp)
                .testTag("main_fab")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (expanded) "Close transaction options" else "Add transaction",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun SetBalanceDialog(
    currentBalance: Double,
    currentBankName: String,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit
) {
    var balanceText by remember {
        mutableStateOf(String.format(java.util.Locale.US, "%.2f", currentBalance))
    }
    var bankText by remember { mutableStateOf(currentBankName) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Bank Balance", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Update your current on-hand bank balance in LKR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = {
                        balanceText = it
                        errorMsg = null
                    },
                    label = { Text("Balance (LKR)") },
                    prefix = { Text("LKR ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMsg != null,
                    supportingText = errorMsg?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("balance_input")
                )
                OutlinedTextField(
                    value = bankText,
                    onValueChange = { bankText = it },
                    label = { Text("Bank / Account Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = balanceText.replace(",", "").trim().toDoubleOrNull()
                    if (parsed == null || parsed < 0.0) {
                        errorMsg = "Please enter a valid non-negative amount"
                    } else {
                        onSave(parsed, bankText.ifBlank { currentBankName })
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("save_balance_confirm")
            ) {
                Text("Save Balance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentBank: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }
    var bankText by remember { mutableStateOf(currentBank) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Personalize Profile", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_profile_avatar_minimal_1795417639564),
                    contentDescription = "Profile illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .scale(1.22f)
                )
                Text(
                    text = "All profile details stay strictly on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bankText,
                    onValueChange = { bankText = it },
                    label = { Text("Primary Bank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        nameText.ifBlank { currentName },
                        bankText.ifBlank { currentBank }
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
