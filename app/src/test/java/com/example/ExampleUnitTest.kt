package com.example

import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.util.DateFormatters
import com.example.util.LkrFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `lkr formatter formats positive and negative currency accurately`() {
        assertEquals("LKR 142,500.00", LkrFormatter.format(142500.0))
        assertEquals("142,500.00", LkrFormatter.format(142500.0, showPrefix = false))
        assertEquals("+ LKR 85,000.00", LkrFormatter.formatSigned(85000.0, isIncome = true))
        assertEquals("– LKR 8,450.00", LkrFormatter.formatSigned(8450.0, isIncome = false))
        assertEquals("LKR 142.5k", LkrFormatter.formatCompactK(142500.0))
        assertEquals("LKR 185k", LkrFormatter.formatCompactK(185000.0))
    }

    @Test
    fun `date formatters compute month labels and days in month`() {
        assertEquals("October 2024", DateFormatters.formatMonthYear(2024, 9))
        assertEquals("Oct 2024", DateFormatters.formatShortMonthYear(2024, 9))
        assertEquals("Oct", DateFormatters.formatShortMonth(2024, 9))
        assertEquals(31, DateFormatters.daysInMonth(2024, 9))
        assertEquals(29, DateFormatters.daysInMonth(2024, 1))
        assertTrue(DateFormatters.formatDayGroupHeader(System.currentTimeMillis()).startsWith("Today"))
    }

    @Test
    fun `cash payments do not deduct from or alter bank balance`() {
        val cashExpense = TransactionEntity(
            title = "Street Food",
            amount = 750.0,
            type = TransactionType.EXPENSE,
            categoryName = "Food",
            paymentMethod = "Cash",
            timestamp = 1_700_000_000_000L
        )
        val cashIncome = cashExpense.copy(
            title = "Cash Gift",
            amount = 5000.0,
            type = TransactionType.INCOME,
            paymentMethod = "cash"
        )
        val cardExpense = cashExpense.copy(paymentMethod = "Debit Card")
        val bankIncome = cashIncome.copy(paymentMethod = "Bank Transfer")

        assertEquals(0.0, FinanceRepository.bankBalanceEffect(cashExpense), 0.001)
        assertEquals(0.0, FinanceRepository.bankBalanceEffect(cashIncome), 0.001)
        assertEquals(-750.0, FinanceRepository.bankBalanceEffect(cardExpense), 0.001)
        assertEquals(5000.0, FinanceRepository.bankBalanceEffect(bankIncome), 0.001)

        // Switching an expense from Debit Card to Cash refunds the bank balance (+750.0)
        val switchCardToCashDelta =
            FinanceRepository.bankBalanceEffect(cashExpense) - FinanceRepository.bankBalanceEffect(cardExpense)
        assertEquals(750.0, switchCardToCashDelta, 0.001)

        // Switching an expense from Cash to Debit Card deducts from bank balance (-750.0)
        val switchCashToCardDelta =
            FinanceRepository.bankBalanceEffect(cardExpense) - FinanceRepository.bankBalanceEffect(cashExpense)
        assertEquals(-750.0, switchCashToCardDelta, 0.001)
    }
}
