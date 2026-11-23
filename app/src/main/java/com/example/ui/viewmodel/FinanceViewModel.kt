package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserPrefs
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.ThemeMode
import com.example.util.BackupAndExportManager
import com.example.util.CategoryIcons
import com.example.util.DateFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.Calendar

enum class ActivityTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}

data class MonthYearKey(val year: Int, val monthZeroBased: Int) {
    fun label(): String = DateFormatters.formatMonthYear(year, monthZeroBased)
    fun shortLabel(): String = DateFormatters.formatShortMonthYear(year, monthZeroBased)
    fun shortMonth(): String = DateFormatters.formatShortMonth(year, monthZeroBased)
}

data class ActivityFilterState(
    val searchQuery: String = "",
    val selectedMonth: MonthYearKey? = null, // null = Current Month by default or All? Set in init
    val typeFilter: ActivityTypeFilter = ActivityTypeFilter.ALL,
    val categoryFilter: String? = null
)

data class CategoryBreakdownItem(
    val categoryName: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: Long
)

data class MonthlyComparisonBar(
    val monthKey: MonthYearKey,
    val income: Double,
    val expense: Double,
    val isSelected: Boolean
)

data class HomeUiState(
    val userPrefs: UserPrefs = UserPrefs(),
    val currentMonthIncome: Double = 0.0,
    val currentMonthExpense: Double = 0.0,
    val topGoal: SavingsGoalEntity? = null,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val insightHeadline: String = "Keep tracking daily expenses!",
    val insightSubtitle: String = "Every LKR saved brings your goals closer.",
    val isLoaded: Boolean = false
)

data class InsightsUiState(
    val selectedMonth: MonthYearKey,
    val totalSpend: Double = 0.0,
    val totalIncome: Double = 0.0,
    val savedThisMonth: Double = 0.0,
    val savingsRatePercent: Float = 0f,
    val spendDeltaPercent: Float? = null,
    val prevMonthShortLabel: String = "",
    val topCategoryName: String = "None",
    val topCategoryAmount: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val daysInMonth: Int = 30,
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val sixMonthSeries: List<MonthlyComparisonBar> = emptyList(),
    val monthTransactions: List<TransactionEntity> = emptyList()
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val db = AppDatabase.getInstance(application)
        val prefs = UserPreferencesRepository(application)
        repository = FinanceRepository(db.financeDao(), prefs, application)
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private fun currentMonthKey(): MonthYearKey {
        val cal = Calendar.getInstance()
        return MonthYearKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    val userPrefs: StateFlow<UserPrefs> = repository.userPrefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefs())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<SavingsGoalEntity>> = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activityFilter = MutableStateFlow(
        ActivityFilterState(selectedMonth = currentMonthKey())
    )
    val activityFilter: StateFlow<ActivityFilterState> = _activityFilter.asStateFlow()

    private val _insightsMonth = MutableStateFlow(currentMonthKey())
    val insightsMonth: StateFlow<MonthYearKey> = _insightsMonth.asStateFlow()

    val homeUiState: StateFlow<HomeUiState> = combine(
        userPrefs,
        allTransactions,
        allGoals
    ) { prefs, txs, goals ->
        val curKey = currentMonthKey()
        val curTxs = txs.filter { txMonthKey(it.timestamp) == curKey }
        val curIncome = curTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val curExpense = curTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val prevCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val prevKey = MonthYearKey(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH))
        val prevBills = txs.filter {
            txMonthKey(it.timestamp) == prevKey &&
                it.type == TransactionType.EXPENSE &&
                it.categoryName.equals("Bills", ignoreCase = true)
        }.sumOf { it.amount }
        val curBills = curTxs.filter {
            it.type == TransactionType.EXPENSE &&
                it.categoryName.equals("Bills", ignoreCase = true)
        }.sumOf { it.amount }

        val (headline, subtitle) = when {
            prevBills > 0 && curBills < prevBills -> {
                val pct = ((prevBills - curBills) / prevBills * 100).toInt().coerceIn(1, 99)
                "$pct% less spent on utilities this month!" to "Keep it up to reach your savings goals faster."
            }
            curIncome > curExpense && curIncome > 0 -> {
                val pct = ((curIncome - curExpense) / curIncome * 100).toInt().coerceIn(1, 100)
                "You're saving $pct% of your monthly income!" to "Your mindful spending keeps your balance healthy."
            }
            else -> {
                "Every LKR tracked builds financial clarity" to "Log expenses daily to keep insights accurate."
            }
        }

        val activeGoals = goals.filter { !it.isPurchased }
        val topGoal = activeGoals.firstOrNull { (prefs.bankBalance + it.allocatedAmount) >= it.targetAmount }
            ?: activeGoals.firstOrNull()

        HomeUiState(
            userPrefs = prefs,
            currentMonthIncome = curIncome,
            currentMonthExpense = curExpense,
            topGoal = topGoal,
            recentTransactions = txs.take(5),
            insightHeadline = headline,
            insightSubtitle = subtitle,
            isLoaded = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val insightsUiState: StateFlow<InsightsUiState> = combine(
        _insightsMonth,
        allTransactions,
        allCategories
    ) { monthKey, txs, categories ->
        val monthTxs = txs.filter { txMonthKey(it.timestamp) == monthKey }
        val totalIncome = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalSpend = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val saved = totalIncome - totalSpend
        val rate = if (totalIncome > 0) ((saved / totalIncome) * 100f).toFloat().coerceIn(-100f, 100f) else 0f

        val prevCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, monthKey.year)
            set(Calendar.MONTH, monthKey.monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, -1)
        }
        val prevKey = MonthYearKey(prevCal.get(Calendar.YEAR), prevCal.get(Calendar.MONTH))
        val prevSpend = txs.filter {
            txMonthKey(it.timestamp) == prevKey && it.type == TransactionType.EXPENSE
        }.sumOf { it.amount }
        val deltaPct = if (prevSpend > 0) {
            (((totalSpend - prevSpend) / prevSpend) * 100.0).toFloat()
        } else null

        val catColorMap = categories.associate { it.name.lowercase() to it.colorHex }
        val expenseByCat = monthTxs
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }

        val breakdown = expenseByCat.map { (catName, amount) ->
            val pct = if (totalSpend > 0) (amount / totalSpend * 100.0).toFloat() else 0f
            val hex = catColorMap[catName.lowercase()]
                ?: CategoryIcons.colorForCategory(catName).value.toLong()
            CategoryBreakdownItem(
                categoryName = catName,
                amount = amount,
                percentage = pct,
                colorHex = hex
            )
        }

        val topCat = breakdown.firstOrNull()
        val daysInMonth = DateFormatters.daysInMonth(monthKey.year, monthKey.monthZeroBased)
        val dailyAvg = if (daysInMonth > 0) totalSpend / daysInMonth else 0.0

        // 6 months series ending at selectedMonth
        val series = (5 downTo 0).map { offset ->
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, monthKey.year)
                set(Calendar.MONTH, monthKey.monthZeroBased)
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, -offset)
            }
            val key = MonthYearKey(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
            val mTxs = txs.filter { txMonthKey(it.timestamp) == key }
            MonthlyComparisonBar(
                monthKey = key,
                income = mTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = mTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                isSelected = (key == monthKey)
            )
        }

        InsightsUiState(
            selectedMonth = monthKey,
            totalSpend = totalSpend,
            totalIncome = totalIncome,
            savedThisMonth = saved,
            savingsRatePercent = rate,
            spendDeltaPercent = deltaPct,
            prevMonthShortLabel = prevKey.shortMonth(),
            topCategoryName = topCat?.categoryName ?: "No Expenses",
            topCategoryAmount = topCat?.amount ?: 0.0,
            dailyAverage = dailyAvg,
            daysInMonth = daysInMonth,
            categoryBreakdown = breakdown,
            sixMonthSeries = series,
            monthTransactions = monthTxs
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        InsightsUiState(selectedMonth = currentMonthKey())
    )

    fun txMonthKey(timestamp: Long): MonthYearKey {
        val c = Calendar.getInstance().apply { timeInMillis = timestamp }
        return MonthYearKey(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
    }

    fun availableMonthKeys(txs: List<TransactionEntity>): List<MonthYearKey> {
        val keys = txs.map { txMonthKey(it.timestamp) }.toMutableSet()
        keys.add(currentMonthKey())
        return keys.sortedWith(compareByDescending<MonthYearKey> { it.year }.thenByDescending { it.monthZeroBased })
    }

    fun updateSearchQuery(query: String) {
        _activityFilter.value = _activityFilter.value.copy(searchQuery = query)
    }

    fun updateActivityMonth(month: MonthYearKey?) {
        _activityFilter.value = _activityFilter.value.copy(selectedMonth = month)
    }

    fun updateActivityTypeFilter(type: ActivityTypeFilter) {
        _activityFilter.value = _activityFilter.value.copy(typeFilter = type)
    }

    fun updateActivityCategoryFilter(category: String?) {
        _activityFilter.value = _activityFilter.value.copy(categoryFilter = category)
    }

    fun resetActivityFilters() {
        _activityFilter.value = ActivityFilterState(
            searchQuery = "",
            selectedMonth = currentMonthKey(),
            typeFilter = ActivityTypeFilter.ALL,
            categoryFilter = null
        )
    }

    fun stepInsightsMonth(delta: Int) {
        val cur = _insightsMonth.value
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, cur.year)
            set(Calendar.MONTH, cur.monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        _insightsMonth.value = MonthYearKey(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
    }

    fun selectInsightsMonth(key: MonthYearKey) {
        _insightsMonth.value = key
    }

    fun setBankBalance(amount: Double, bankName: String? = null) {
        viewModelScope.launch {
            repository.setBankBalance(amount, bankName)
        }
    }

    fun setUserProfile(userName: String, bankName: String) {
        viewModelScope.launch {
            repository.setUserProfile(userName, bankName)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun saveTransaction(transaction: TransactionEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun restoreDeletedTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.restoreDeletedTransaction(transaction)
        }
    }

    fun saveGoal(goal: SavingsGoalEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveGoal(goal)
            onComplete()
        }
    }

    fun allocateToGoal(goal: SavingsGoalEntity, addedAmount: Double) {
        viewModelScope.launch {
            repository.allocateToGoal(goal, addedAmount)
        }
    }

    fun markGoalBought(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.markGoalBought(goal)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun addCategory(name: String, iconKey: String, colorHex: Long) {
        viewModelScope.launch {
            repository.addCategory(name, iconKey, colorHex)
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    suspend fun generateCsvContent(): String {
        val (txs, _, _) = repository.getSnapshotData()
        val prefs = userPrefs.value
        return BackupAndExportManager.buildCsv(txs, prefs.bankBalance, prefs.bankName)
    }

    suspend fun generateBackupJson(): String {
        val (txs, goals, cats) = repository.getSnapshotData()
        return BackupAndExportManager.buildBackupJson(userPrefs.value, txs, goals, cats)
    }

    suspend fun restoreFromBackupJson(rawJson: String): Result<Int> {
        val parsed = BackupAndExportManager.parseBackupJson(rawJson)
        return parsed.map { payload ->
            repository.restoreFromBackup(payload)
            payload.transactions.size
        }
    }

    fun writeInsightsPdf(outputStream: OutputStream) {
        val state = insightsUiState.value
        val prefs = userPrefs.value
        BackupAndExportManager.writeMonthlyPdfReport(
            outputStream = outputStream,
            monthLabel = state.selectedMonth.label(),
            bankName = prefs.bankName,
            bankBalance = prefs.bankBalance,
            totalIncome = state.totalIncome,
            totalExpense = state.totalSpend,
            categoryBreakdown = state.categoryBreakdown.map { it.categoryName to it.amount },
            transactions = state.monthTransactions
        )
    }
}
