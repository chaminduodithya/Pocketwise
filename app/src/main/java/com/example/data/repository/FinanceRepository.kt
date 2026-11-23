package com.example.data.repository

import com.example.data.local.CategoryEntity
import com.example.data.local.FinanceDao
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserPrefs
import com.example.ui.theme.ThemeMode
import com.example.util.BackupPayload
import com.example.util.HotlinkedImages
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(
    private val dao: FinanceDao,
    private val prefsRepo: UserPreferencesRepository
) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val goals: Flow<List<SavingsGoalEntity>> = dao.observeGoals()
    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val userPrefs: Flow<UserPrefs> = prefsRepo.userPrefs

    suspend fun ensureSeeded() {
        val prefs = prefsRepo.getPrefsOnce()
        if (prefs.isSeeded) return

        val existingCategories = dao.getAllCategoriesOnce()
        if (existingCategories.isEmpty()) {
            dao.insertCategories(defaultCategories())
        }

        val existingTransactions = dao.getAllTransactionsOnce()
        if (existingTransactions.isEmpty()) {
            dao.insertTransactions(initialSeedTransactions())
        }

        val existingGoals = dao.getAllGoalsOnce()
        if (existingGoals.isEmpty()) {
            dao.insertGoals(initialSeedGoals())
        }

        prefsRepo.setBankBalance(142500.0, "Commercial Bank")
        prefsRepo.markSeeded()
    }

    suspend fun setBankBalance(amount: Double, bankName: String? = null) {
        prefsRepo.setBankBalance(amount.coerceAtLeast(0.0), bankName)
    }

    suspend fun setUserProfile(userName: String, bankName: String) {
        prefsRepo.setUserProfile(userName, bankName)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        prefsRepo.setThemeMode(mode)
    }

    suspend fun saveTransaction(transaction: TransactionEntity) {
        if (transaction.id == 0L) {
            dao.insertTransaction(transaction)
            val delta = bankBalanceEffect(transaction)
            if (delta != 0.0) {
                prefsRepo.adjustBankBalance(delta)
            }
        } else {
            val previous = dao.getTransactionById(transaction.id)
            dao.updateTransaction(transaction)
            if (previous != null) {
                val netDelta = bankBalanceEffect(transaction) - bankBalanceEffect(previous)
                if (netDelta != 0.0) {
                    prefsRepo.adjustBankBalance(netDelta)
                }
            }
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransactionById(transaction.id)
        val revertDelta = -bankBalanceEffect(transaction)
        if (revertDelta != 0.0) {
            prefsRepo.adjustBankBalance(revertDelta)
        }
    }

    suspend fun restoreDeletedTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction.copy(id = 0L))
        val applyDelta = bankBalanceEffect(transaction)
        if (applyDelta != 0.0) {
            prefsRepo.adjustBankBalance(applyDelta)
        }
    }

    suspend fun saveGoal(goal: SavingsGoalEntity) {
        if (goal.id == 0L) {
            dao.insertGoal(goal)
        } else {
            dao.updateGoal(goal)
        }
    }

    suspend fun allocateToGoal(goal: SavingsGoalEntity, addedAmount: Double) {
        val updated = goal.copy(
            allocatedAmount = (goal.allocatedAmount + addedAmount).coerceAtLeast(0.0).coerceAtMost(goal.targetAmount)
        )
        dao.updateGoal(updated)
    }

    suspend fun markGoalBought(goal: SavingsGoalEntity) {
        dao.updateGoal(goal.copy(isPurchased = true, allocatedAmount = goal.targetAmount))
        val tx = TransactionEntity(
            title = "Purchased: ${goal.name}",
            amount = goal.targetAmount,
            type = TransactionType.EXPENSE,
            categoryName = "Shopping",
            paymentMethod = "Debit Card",
            timestamp = System.currentTimeMillis(),
            notes = goal.notes
        )
        dao.insertTransaction(tx)
        prefsRepo.adjustBankBalance(-goal.targetAmount)
    }

    suspend fun deleteGoal(goalId: Long) {
        dao.deleteGoalById(goalId)
    }

    suspend fun addCategory(name: String, iconKey: String, colorHex: Long) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        dao.insertCategory(
            CategoryEntity(
                name = trimmed,
                iconKey = iconKey,
                colorHex = colorHex,
                isDefault = false
            )
        )
    }

    suspend fun deleteCategory(categoryId: Long) {
        dao.deleteCustomCategoryById(categoryId)
    }

    suspend fun getSnapshotData(): Triple<List<TransactionEntity>, List<SavingsGoalEntity>, List<CategoryEntity>> {
        return Triple(
            dao.getAllTransactionsOnce(),
            dao.getAllGoalsOnce(),
            dao.getAllCategoriesOnce()
        )
    }

    suspend fun restoreFromBackup(payload: BackupPayload) {
        dao.clearTransactions()
        dao.clearGoals()
        dao.clearCategories()
        if (payload.categories.isNotEmpty()) {
            dao.insertCategories(payload.categories)
        } else {
            dao.insertCategories(defaultCategories())
        }
        dao.insertTransactions(payload.transactions)
        dao.insertGoals(payload.goals)
        prefsRepo.setBankBalance(payload.bankBalance, payload.bankName)
        prefsRepo.setUserProfile(payload.userName, payload.bankName)
        prefsRepo.markSeeded()
    }

    private fun defaultCategories(): List<CategoryEntity> = listOf(
        CategoryEntity(name = "Food", iconKey = "food", colorHex = 0xFF005048L, isDefault = true),
        CategoryEntity(name = "Salary", iconKey = "salary", colorHex = 0xFF076D2EL, isDefault = true),
        CategoryEntity(name = "Transport", iconKey = "transport", colorHex = 0xFF076D2EL, isDefault = true),
        CategoryEntity(name = "Bills", iconKey = "bills", colorHex = 0xFFB91919L, isDefault = true),
        CategoryEntity(name = "Shopping", iconKey = "shopping", colorHex = 0xFF016A60L, isDefault = true),
        CategoryEntity(name = "Health", iconKey = "health", colorHex = 0xFF147434L, isDefault = true),
        CategoryEntity(name = "Entertainment", iconKey = "entertainment", colorHex = 0xFF3E4947L, isDefault = true),
        CategoryEntity(name = "Other", iconKey = "other", colorHex = 0xFF6E7977L, isDefault = true)
    )

    private fun initialSeedTransactions(): List<TransactionEntity> {
        fun calDaysAgo(daysAgo: Int, hour: Int, minute: Int): Long {
            return Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }.timeInMillis
        }

        fun calMonthsAgo(monthsAgo: Int, dayOfMonth: Int, hour: Int): Long {
            return Calendar.getInstance().apply {
                add(Calendar.MONTH, -monthsAgo)
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 15)
                set(Calendar.SECOND, 0)
            }.timeInMillis
        }

        // Current month transactions (matching the HTML figures: Income +85,000, Spent 34,250)
        val currentMonthList = listOf(
            TransactionEntity(
                title = "Keells Super Groceries",
                amount = 8450.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calDaysAgo(0, 11, 42),
                receiptImageUri = HotlinkedImages.RECEIPT_SAMPLE,
                notes = "Weekly groceries & pantry essentials - Colombo 03"
            ),
            TransactionEntity(
                title = "Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calDaysAgo(1, 9, 15),
                notes = "Direct salary deposit"
            ),
            TransactionEntity(
                title = "Uber Ride - Colombo 03",
                amount = 1250.0,
                type = TransactionType.EXPENSE,
                categoryName = "Transport",
                paymentMethod = "Commercial Bank",
                timestamp = calDaysAgo(1, 19, 50)
            ),
            TransactionEntity(
                title = "CEB Electricity Bill",
                amount = 6200.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "Bank Transfer",
                timestamp = calDaysAgo(4, 15, 20)
            ),
            TransactionEntity(
                title = "Java Lounge Coffee & Snack",
                amount = 1890.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Digital Wallet",
                timestamp = calDaysAgo(4, 10, 4)
            ),
            TransactionEntity(
                title = "Bus Fare to Pettah & Reload",
                amount = 480.0,
                type = TransactionType.EXPENSE,
                categoryName = "Transport",
                paymentMethod = "Cash",
                timestamp = calDaysAgo(5, 8, 12)
            ),
            TransactionEntity(
                title = "Dialog Home Broadband",
                amount = 3490.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "LANKAQR",
                timestamp = calDaysAgo(7, 14, 10)
            ),
            TransactionEntity(
                title = "Cargills Food City & Produce",
                amount = 4060.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calDaysAgo(9, 18, 30)
            ),
            TransactionEntity(
                title = "Odel Essentials & Apparel",
                amount = 3450.0,
                type = TransactionType.EXPENSE,
                categoryName = "Shopping",
                paymentMethod = "Debit Card",
                timestamp = calDaysAgo(11, 16, 45)
            ),
            TransactionEntity(
                title = "PickMe Commute Pass",
                amount = 3070.0,
                type = TransactionType.EXPENSE,
                categoryName = "Transport",
                paymentMethod = "Digital Wallet",
                timestamp = calDaysAgo(13, 9, 5)
            ),
            TransactionEntity(
                title = "Scope Cinemas & Dinner",
                amount = 1910.0,
                type = TransactionType.EXPENSE,
                categoryName = "Entertainment",
                paymentMethod = "Debit Card",
                timestamp = calDaysAgo(15, 20, 15)
            )
        )

        // Ensure current-month total stays cleanly aligned even early in the month:
        // If calDaysAgo pushed any item to previous month, keep all current-month items in current month
        val nowCal = Calendar.getInstance()
        val curYear = nowCal.get(Calendar.YEAR)
        val curMonth = nowCal.get(Calendar.MONTH)
        val normalizedCurrent = currentMonthList.mapIndexed { idx, tx ->
            val c = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            if (c.get(Calendar.YEAR) != curYear || c.get(Calendar.MONTH) != curMonth) {
                val safeDay = (nowCal.get(Calendar.DAY_OF_MONTH) - idx).coerceAtLeast(1)
                c.set(Calendar.YEAR, curYear)
                c.set(Calendar.MONTH, curMonth)
                c.set(Calendar.DAY_OF_MONTH, safeDay)
                tx.copy(timestamp = c.timeInMillis)
            } else {
                tx
            }
        }

        // Past 5 months history so 6-Month Income vs Expense chart & Month comparison work out-of-the-box
        val historyList = listOf(
            // 1 month ago (Income 85,000, Expense 39,100)
            TransactionEntity(
                title = "Monthly Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(1, 25, 9)
            ),
            TransactionEntity(
                title = "Groceries & Household",
                amount = 16200.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calMonthsAgo(1, 14, 18)
            ),
            TransactionEntity(
                title = "Utilities & Fiber Internet",
                amount = 15600.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(1, 10, 11)
            ),
            TransactionEntity(
                title = "Commute & Fuel",
                amount = 7300.0,
                type = TransactionType.EXPENSE,
                categoryName = "Transport",
                paymentMethod = "LANKAQR",
                timestamp = calMonthsAgo(1, 5, 8)
            ),
            // 2 months ago (Income 85,000, Expense 39,000)
            TransactionEntity(
                title = "Monthly Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(2, 25, 9)
            ),
            TransactionEntity(
                title = "Supermarket & Dining",
                amount = 21000.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calMonthsAgo(2, 16, 17)
            ),
            TransactionEntity(
                title = "Utility Bills & Medical",
                amount = 18000.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(2, 9, 14)
            ),
            // 3 months ago (Income 85,000, Expense 45,000)
            TransactionEntity(
                title = "Monthly Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(3, 25, 9)
            ),
            TransactionEntity(
                title = "Family Groceries & Dining",
                amount = 24500.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calMonthsAgo(3, 18, 19)
            ),
            TransactionEntity(
                title = "Annual Insurance & Bills",
                amount = 20500.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(3, 11, 10)
            ),
            // 4 months ago (Income 85,000, Expense 38,000)
            TransactionEntity(
                title = "Monthly Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(4, 25, 9)
            ),
            TransactionEntity(
                title = "Monthly Living & Transport",
                amount = 38000.0,
                type = TransactionType.EXPENSE,
                categoryName = "Food",
                paymentMethod = "Debit Card",
                timestamp = calMonthsAgo(4, 15, 16)
            ),
            // 5 months ago (Income 85,000, Expense 42,000)
            TransactionEntity(
                title = "Monthly Salary - Virtusa SL",
                amount = 85000.0,
                type = TransactionType.INCOME,
                categoryName = "Salary",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(5, 25, 9)
            ),
            TransactionEntity(
                title = "Monthly Expenses & Bills",
                amount = 42000.0,
                type = TransactionType.EXPENSE,
                categoryName = "Bills",
                paymentMethod = "Bank Transfer",
                timestamp = calMonthsAgo(5, 12, 13)
            )
        )

        return normalizedCurrent + historyList
    }

    private fun initialSeedGoals(): List<SavingsGoalEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            SavingsGoalEntity(
                name = "Wireless ANC Headphones",
                targetAmount = 12900.0,
                allocatedAmount = 0.0,
                storeUrl = "https://www.daraz.lk/",
                storeLabel = "Daraz Online Store",
                imageUri = HotlinkedImages.GOAL_HEADPHONES_CARD,
                notes = "Buy for daily remote work and commuting on Route 138 (Maharagama - Pettah)",
                createdAt = now - 12L * 24 * 60 * 60 * 1000,
                isPurchased = false
            ),
            SavingsGoalEntity(
                name = "Modern Slim Workstation Laptop",
                targetAmount = 185000.0,
                allocatedAmount = 0.0,
                storeUrl = "https://www.takas.lk/",
                storeLabel = "Takas Tech Mart",
                imageUri = HotlinkedImages.GOAL_LAPTOP_CARD,
                notes = "Upgrade mobile dev workstation for client projects",
                createdAt = now - 25L * 24 * 60 * 60 * 1000,
                isPurchased = false
            )
        )
    }

    companion object {
        internal fun bankBalanceEffect(transaction: TransactionEntity): Double {
            if (transaction.paymentMethod.equals("Cash", ignoreCase = true)) {
                return 0.0
            }
            return if (transaction.type == TransactionType.INCOME) {
                transaction.amount
            } else {
                -transaction.amount
            }
        }
    }
}
