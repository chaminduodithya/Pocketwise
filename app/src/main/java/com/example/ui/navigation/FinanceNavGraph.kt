package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.TransactionType
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.FinanceBottomBar
import com.example.ui.components.FinanceFab
import com.example.ui.components.FinanceStackTopBar
import com.example.ui.components.FinanceTabTopBar
import com.example.ui.components.FinanceTransactionSpeedDialFab
import com.example.ui.components.SetBalanceDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.CreateGoalScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.ManageCategoriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FinanceTrackerTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val ACTIVITY = "activity"
    const val GOALS = "goals"
    const val INSIGHTS = "insights"
    const val ADD_TRANSACTION = "add_transaction"
    const val CREATE_GOAL = "create_goal"
    const val SETTINGS = "settings"
    const val MANAGE_CATEGORIES = "manage_categories"
}

@Composable
fun FinanceTrackerApp(viewModel: FinanceViewModel) {
    val userPrefs by viewModel.userPrefs.collectAsStateWithLifecycle()
    val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val activityFilter by viewModel.activityFilter.collectAsStateWithLifecycle()
    val insightsState by viewModel.insightsUiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSetBalanceDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }

    val tabRoutes = setOf(Routes.HOME, Routes.ACTIVITY, Routes.GOALS, Routes.INSIGHTS)
    val isTabRoute = currentRoute in tabRoutes

    LaunchedEffect(currentRoute) {
        fabExpanded = false
    }

    BackHandler(enabled = fabExpanded) {
        fabExpanded = false
    }

    fun showToast(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    FinanceTrackerTheme(themeMode = userPrefs.themeMode) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                when {
                    isTabRoute -> {
                        val subtitle = when (currentRoute) {
                            Routes.HOME -> "Home"
                            Routes.ACTIVITY -> "Activity"
                            Routes.GOALS -> "Goals"
                            Routes.INSIGHTS -> "Insights"
                            else -> "Home"
                        }
                        FinanceTabTopBar(
                            subtitle = subtitle,
                            userName = userPrefs.userName,
                            onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }
                    currentRoute.startsWith(Routes.ADD_TRANSACTION) -> {
                        val txId = navBackStackEntry?.arguments?.getLong("txId") ?: -1L
                        val typeArg = navBackStackEntry?.arguments?.getString("defaultType") ?: TransactionType.EXPENSE.name
                        val existingTx = transactions.firstOrNull { it.id == txId }
                        val activeType = existingTx?.type ?: runCatching {
                            TransactionType.valueOf(typeArg)
                        }.getOrDefault(TransactionType.EXPENSE)
                        val barTitle = when {
                            existingTx != null && activeType == TransactionType.INCOME -> "Edit Income"
                            existingTx != null -> "Edit Expense"
                            activeType == TransactionType.INCOME -> "Add Income"
                            else -> "Add Expense"
                        }
                        FinanceStackTopBar(
                            title = barTitle,
                            userName = userPrefs.userName,
                            onBackClick = { navController.popBackStack() },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }
                    currentRoute.startsWith(Routes.CREATE_GOAL) -> {
                        val goalId = navBackStackEntry?.arguments?.getLong("goalId") ?: -1L
                        FinanceStackTopBar(
                            title = if (goalId > 0) "Edit Goal" else "Create Goal",
                            userName = userPrefs.userName,
                            onBackClick = { navController.popBackStack() },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }
                    currentRoute == Routes.SETTINGS -> {
                        FinanceStackTopBar(
                            title = "Settings",
                            userName = userPrefs.userName,
                            onBackClick = { navController.popBackStack() },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }
                    currentRoute == Routes.MANAGE_CATEGORIES -> {
                        FinanceStackTopBar(
                            title = "Manage Categories",
                            userName = userPrefs.userName,
                            onBackClick = { navController.popBackStack() },
                            onProfileClick = { showProfileDialog = true }
                        )
                    }
                }
            },
            bottomBar = {
                if (isTabRoute) {
                    FinanceBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> navigateToTab(route) }
                    )
                }
            },
            floatingActionButton = {
                if (isTabRoute) {
                    if (currentRoute == Routes.GOALS) {
                        FinanceFab(
                            contentDescription = "Create goal",
                            onClick = {
                                navController.navigate("${Routes.CREATE_GOAL}?goalId=-1")
                            }
                        )
                    } else {
                        FinanceTransactionSpeedDialFab(
                            expanded = fabExpanded,
                            onToggle = { fabExpanded = !fabExpanded },
                            onAddIncome = {
                                fabExpanded = false
                                navController.navigate("${Routes.ADD_TRANSACTION}?txId=-1&defaultType=INCOME")
                            },
                            onAddExpense = {
                                fabExpanded = false
                                navController.navigate("${Routes.ADD_TRANSACTION}?txId=-1&defaultType=EXPENSE")
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            state = homeState,
                            categories = categories,
                            onSetBalanceClick = { showSetBalanceDialog = true },
                            onViewAllGoalsClick = { navigateToTab(Routes.GOALS) },
                            onSeeAllActivityClick = { navigateToTab(Routes.ACTIVITY) },
                            onTransactionClick = { txId ->
                                navController.navigate("${Routes.ADD_TRANSACTION}?txId=$txId&defaultType=EXPENSE")
                            },
                            onInsightClick = { navigateToTab(Routes.INSIGHTS) }
                        )
                    }

                    composable(Routes.ACTIVITY) {
                        val availableMonths = remember(transactions) {
                            viewModel.availableMonthKeys(transactions)
                        }
                        ActivityScreen(
                            allTransactions = transactions,
                            categories = categories,
                            filterState = activityFilter,
                            availableMonths = availableMonths,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onMonthSelect = viewModel::updateActivityMonth,
                            onTypeFilterSelect = viewModel::updateActivityTypeFilter,
                            onCategoryFilterSelect = viewModel::updateActivityCategoryFilter,
                            onResetFilters = viewModel::resetActivityFilters,
                            onTransactionClick = { txId ->
                                navController.navigate("${Routes.ADD_TRANSACTION}?txId=$txId&defaultType=EXPENSE")
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val res = snackbarHostState.showSnackbar(
                                        message = "Deleted \"${tx.title}\"",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreDeletedTransaction(tx)
                                    }
                                }
                            }
                        )
                    }

                    composable(Routes.GOALS) {
                        GoalsScreen(
                            goals = goals,
                            bankBalance = userPrefs.bankBalance,
                            onEditGoal = { goalId ->
                                navController.navigate("${Routes.CREATE_GOAL}?goalId=$goalId")
                            },
                            onDeleteGoal = { goal ->
                                viewModel.deleteGoal(goal.id)
                                showToast("Removed \"${goal.name}\"")
                            },
                            onAllocateGoal = viewModel::allocateToGoal,
                            onMarkBought = viewModel::markGoalBought,
                            onShowMessage = ::showToast
                        )
                    }

                    composable(Routes.INSIGHTS) {
                        InsightsScreen(
                            state = insightsState,
                            onStepMonth = viewModel::stepInsightsMonth,
                            onSelectMonth = viewModel::selectInsightsMonth,
                            onWritePdfReport = viewModel::writeInsightsPdf,
                            onShowMessage = ::showToast
                        )
                    }

                    composable(
                        route = "${Routes.ADD_TRANSACTION}?txId={txId}&defaultType={defaultType}",
                        arguments = listOf(
                            navArgument("txId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            },
                            navArgument("defaultType") {
                                type = NavType.StringType
                                defaultValue = TransactionType.EXPENSE.name
                            }
                        )
                    ) { entry ->
                        val txId = entry.arguments?.getLong("txId") ?: -1L
                        val typeArg = entry.arguments?.getString("defaultType") ?: TransactionType.EXPENSE.name
                        val defaultType = runCatching { TransactionType.valueOf(typeArg) }
                            .getOrDefault(TransactionType.EXPENSE)
                        val existing = transactions.firstOrNull { it.id == txId }

                        AddTransactionScreen(
                            existingTransaction = existing,
                            initialType = defaultType,
                            bankName = userPrefs.bankName,
                            categories = categories,
                            onSave = { entity ->
                                viewModel.saveTransaction(entity) {
                                    navController.popBackStack()
                                    showToast(if (existing != null) "Transaction updated" else "Transaction saved")
                                }
                            },
                            onDelete = { entity ->
                                viewModel.deleteTransaction(entity)
                                navController.popBackStack()
                                showToast("Transaction deleted")
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "${Routes.CREATE_GOAL}?goalId={goalId}",
                        arguments = listOf(
                            navArgument("goalId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) { entry ->
                        val goalId = entry.arguments?.getLong("goalId") ?: -1L
                        val existing = goals.firstOrNull { it.id == goalId }

                        CreateGoalScreen(
                            existingGoal = existing,
                            bankBalance = userPrefs.bankBalance,
                            onSave = { entity ->
                                viewModel.saveGoal(entity) {
                                    navController.popBackStack()
                                    showToast(if (existing != null) "Goal updated!" else "Goal locked in!")
                                }
                            },
                            onDiscard = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            userPrefs = userPrefs,
                            categories = categories,
                            onEditBalanceClick = { showSetBalanceDialog = true },
                            onManageCategoriesClick = { navController.navigate(Routes.MANAGE_CATEGORIES) },
                            onThemeSelect = { mode ->
                                viewModel.setThemeMode(mode)
                                showToast("Theme set to ${mode.name.lowercase().replaceFirstChar { it.uppercase() }}")
                            },
                            onGenerateCsv = viewModel::generateCsvContent,
                            onGenerateBackupJson = viewModel::generateBackupJson,
                            onRestoreBackupJson = viewModel::restoreFromBackupJson,
                            onShowMessage = ::showToast
                        )
                    }

                    composable(Routes.MANAGE_CATEGORIES) {
                        ManageCategoriesScreen(
                            categories = categories,
                            onAddCategory = { name, iconKey, colorHex ->
                                viewModel.addCategory(name, iconKey, colorHex)
                                showToast("Added category \"$name\"")
                            },
                            onDeleteCategory = { id ->
                                viewModel.deleteCategory(id)
                                showToast("Category removed")
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = fabExpanded && isTabRoute && currentRoute != Routes.GOALS,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.28f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { fabExpanded = false }
                    )
                }
            }
        }

        if (showSetBalanceDialog) {
            SetBalanceDialog(
                currentBalance = userPrefs.bankBalance,
                currentBankName = userPrefs.bankName,
                onDismiss = { showSetBalanceDialog = false },
                onSave = { newBalance, newBank ->
                    viewModel.setBankBalance(newBalance, newBank)
                    showSetBalanceDialog = false
                    showToast("Bank balance updated")
                }
            )
        }

        if (showProfileDialog) {
            EditProfileDialog(
                currentName = userPrefs.userName,
                currentBank = userPrefs.bankName,
                onDismiss = { showProfileDialog = false },
                onSave = { newName, newBank ->
                    viewModel.setUserProfile(newName, newBank)
                    showProfileDialog = false
                    showToast("Profile updated")
                }
            )
        }
    }
}
