package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object HotlinkedImages {
    const val PROFILE_AVATAR =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBaSno2O6eMie8HTvLAsPvo0BUJoorRn3zjDWLVPkvYLlEfgeaaPeh3m-X4srX44y8u8bu-JvPB5tSxLwKls7pP2vAhiDxnEgfUZS14EuR4hYpDmwoZmyZ_v2jekGyvd54UBMTR9q5hwKc8-35vxy8ugRrnyc8Vw3BYs0RVE0zRLwletE3dlEX5bl8GL5BxJRwTnkJ9wUoyX1rl7AEoDDv16Vz8VjQLkBxCh3nFtdQ"

    const val RECEIPT_SAMPLE =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDJiWOdCF2BrA5o1XhhlvwLh0sK-OwXId8Hd_Qfr9eLzlxwoCADJ8VtDWI6MLTtWIKy03a6k8aXqXNVoTsukEl1Y6wEfvVBUcxtLdGT0QwgxZOIexLrCxVeUA0H6lRmHGqj_9FINfw1ABJKcRdmoHVEycBguw7Shpa7CTg-v6shSHh9z3VZZDV1rdDqPFh2r6M6uKlMYX2tIHaXdly29M5Ch1M8JdcRRQvCSAS1OMQ"

    const val GOAL_HEADPHONES_PREVIEW =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCTSn2GTQQbB8VxG30N4vbuHn2ck__XrIGd-nPXbofOIaOLJ4lhkDLsjludDBzp3iUotbXO_0BjactzAppTM86h8FKTiUd4NgPZjL49FNVLEeK6dBqn09EX4IlUxBJTYH4XmJuBNfoq-Jeo6TR3BYkO0O9CwSeas7GGYy3X9jzwRbQsHjH8vdO5dL0oZfmPEQB9SwzcN3HaMfto5Zma2_O0vvN_xcYD7i0UFUCAp7Q"

    const val GOAL_HEADPHONES_CARD =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDUz_znBy794zIr2pegEFzWkXcoVXoK4icSvKx37Y0yndb-veUAgFKguMj_HAUVKdjwlzEev1-vHPtbMZ_G4VfTwd4V2NY8nbcZEEXGQzXruf-ExPmKMa0lAglXgKql9UXfFaykyERKy8hlJiX7JOGeZhFkwvCE5gsOr92Jfs7-aQsZ6RvVfVUWiRh4SxT1Mn9GflL0FNDY2pdGV3CcNyk0igSegDEei8aOKIE02r4"

    const val GOAL_LAPTOP_CARD =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuA1KmTJt3kzKSSNJbP9GpXt2XyWcBt9lzsEUxxJ1kIXce7rdutHc1vfgcsXjNN3ClN2_e1twdiCbB2L98R16wLZqml6TUG-wNcbQT0s47qQ_qUq8idjNdbt5vJjn1M5jIxoQVlLUCpMOedG4Bk6muULRHzacIow6P0pTh8EKt00_gYM6mUFAmTJkL7ZyldrHPAscreVVWsyA8vplNOivOPOjRznqkc0ikjG3_RhyHQ"

    const val HOME_INSIGHT_DESK =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuDMrsYsas2Rv0U90taayDULhsvpgu69gmaVjIZlqof-QKZ7tkRw_ayXNc22v1luDAlvkX7PAhaKDZet7m-9FYD5jxWkDEUIG7Yx36-VHyQTCE5u6e1iUNlqCAPbcb701Gz675jsPFAXt4b6yqHIaNOqhLsnYm83mNpMaPPS0AMr1XkKGaQ9gjeEaMa0bn2COsQOFc-oJOZ4Y2Hoahdd7vzyKrlfxm71y0IptE9kaCs"
}

object LkrFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val fullFormat = DecimalFormat("#,##0.00", symbols)
    private val wholeFormat = DecimalFormat("#,##0", symbols)

    fun format(amount: Double, showPrefix: Boolean = true): String {
        val formatted = fullFormat.format(kotlin.math.abs(amount))
        return if (showPrefix) "LKR $formatted" else formatted
    }

    fun formatWhole(amount: Double): String {
        return wholeFormat.format(kotlin.math.abs(amount))
    }

    fun formatCompactK(amount: Double): String {
        return if (amount >= 1000) {
            val k = amount / 1000.0
            if (k % 1.0 == 0.0) "LKR ${k.toInt()}k" else String.format(Locale.US, "LKR %.1fk", k)
        } else {
            format(amount)
        }
    }

    fun formatSigned(amount: Double, isIncome: Boolean): String {
        val prefix = if (isIncome) "+ LKR " else "– LKR "
        return prefix + fullFormat.format(kotlin.math.abs(amount))
    }
}

object DateFormatters {
    private val monthYearFmt = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val shortMonthYearFmt = SimpleDateFormat("MMM yyyy", Locale.US)
    private val shortMonthFmt = SimpleDateFormat("MMM", Locale.US)
    private val dayMonthFmt = SimpleDateFormat("dd MMM", Locale.US)
    private val fullDateFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val longDateFmt = SimpleDateFormat("dd MMMM yyyy", Locale.US)
    private val timeFmt = SimpleDateFormat("hh:mm a", Locale.US)

    fun formatMonthYear(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFmt.format(cal.time)
    }

    fun formatShortMonthYear(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return shortMonthYearFmt.format(cal.time)
    }

    fun formatShortMonth(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return shortMonthFmt.format(cal.time)
    }

    fun formatDayGroupHeader(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when {
            isSameDay(now, target) -> "Today, ${dayMonthFmt.format(Date(timestamp))}"
            isYesterday(now, target) -> "Yesterday, ${dayMonthFmt.format(Date(timestamp))}"
            else -> fullDateFmt.format(Date(timestamp))
        }
    }

    fun formatShortDay(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when {
            isSameDay(now, target) -> "Today"
            isYesterday(now, target) -> "Yesterday"
            else -> dayMonthFmt.format(Date(timestamp))
        }
    }

    fun formatPickerDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        val formatted = longDateFmt.format(Date(timestamp))
        return if (isSameDay(now, target)) "Today, $formatted" else formatted
    }

    fun formatTime(timestamp: Long): String = timeFmt.format(Date(timestamp))

    fun formatUpdatedTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dayLabel = if (isSameDay(now, target)) "today" else dayMonthFmt.format(Date(timestamp))
        return "Updated $dayLabel, ${timeFmt.format(Date(timestamp))}"
    }

    fun isSameDay(a: Calendar, b: Calendar): Boolean {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(now: Calendar, target: Calendar): Boolean {
        val yest = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yest, target)
    }

    fun daysInMonth(year: Int, monthZeroBased: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}

object CategoryIcons {
    fun iconFor(iconKey: String): ImageVector {
        return when (iconKey.lowercase(Locale.US)) {
            "food", "dining", "restaurant" -> Icons.Outlined.Restaurant
            "groceries", "shopping_cart" -> Icons.Outlined.ShoppingCart
            "salary", "payments", "income" -> Icons.Outlined.Payments
            "bank", "account_balance" -> Icons.Outlined.AccountBalance
            "transport", "bus", "directions_bus" -> Icons.Outlined.DirectionsBus
            "bills", "receipt", "receipt_long" -> Icons.Outlined.ReceiptLong
            "electricity", "bolt" -> Icons.Outlined.Bolt
            "wifi", "broadband" -> Icons.Outlined.Wifi
            "shopping", "shopping_bag" -> Icons.Outlined.ShoppingBag
            "health", "medical", "medical_services" -> Icons.Outlined.MedicalServices
            "entertainment", "movie" -> Icons.Outlined.Movie
            else -> Icons.Outlined.Category
        }
    }

    val availableIconChoices: List<Pair<String, String>> = listOf(
        "food" to "Food & Dining",
        "salary" to "Salary & Income",
        "transport" to "Transport",
        "bills" to "Bills & Utilities",
        "shopping" to "Shopping",
        "health" to "Health & Medical",
        "entertainment" to "Entertainment",
        "other" to "Other"
    )

    val availableColorChoices: List<Long> = listOf(
        0xFF005048L,
        0xFFB91919L,
        0xFF076D2EL,
        0xFF84D5C8L,
        0xFF3E4947L,
        0xFF016A60L,
        0xFF6E7977L,
        0xFF920009L
    )

    fun colorForCategory(name: String, customColorHex: Long? = null): Color {
        if (customColorHex != null && customColorHex != 0L) {
            return Color(customColorHex)
        }
        return when (name.lowercase(Locale.US)) {
            "food", "food & groceries", "food & dining" -> Color(0xFF005048)
            "bills", "bills & utilities" -> Color(0xFFB91919)
            "transport" -> Color(0xFF076D2E)
            "shopping" -> Color(0xFF016A60)
            "entertainment" -> Color(0xFF3E4947)
            "health" -> Color(0xFF147434)
            "salary" -> Color(0xFF076D2E)
            else -> Color(0xFF6E7977)
        }
    }
}
