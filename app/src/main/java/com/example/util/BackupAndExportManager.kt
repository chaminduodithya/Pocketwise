package com.example.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.local.CategoryEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.preferences.UserPrefs
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPayload(
    val bankBalance: Double,
    val bankName: String,
    val userName: String,
    val transactions: List<TransactionEntity>,
    val goals: List<SavingsGoalEntity>,
    val categories: List<CategoryEntity>
)

object BackupAndExportManager {

    private val csvDateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun buildCsv(transactions: List<TransactionEntity>, bankBalance: Double, bankName: String): String {
        val sb = StringBuilder()
        sb.appendLine("Pocketwise CSV Export")
        sb.appendLine("Bank,\"${escapeCsv(bankName)}\",Current Balance (LKR),${String.format(Locale.US, "%.2f", bankBalance)}")
        sb.appendLine()
        sb.appendLine("Date,Type,Title,Category,Payment Method,Amount (LKR),Notes")
        transactions.forEach { tx ->
            val dateStr = csvDateFmt.format(Date(tx.timestamp))
            val signAmount = if (tx.type == TransactionType.INCOME) tx.amount else -tx.amount
            sb.appendLine(
                "\"$dateStr\",${tx.type.name},\"${escapeCsv(tx.title)}\",\"${escapeCsv(tx.categoryName)}\",\"${escapeCsv(tx.paymentMethod)}\",${String.format(Locale.US, "%.2f", signAmount)},\"${escapeCsv(tx.notes)}\""
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String = value.replace("\"", "\"\"")

    fun buildBackupJson(
        prefs: UserPrefs,
        transactions: List<TransactionEntity>,
        goals: List<SavingsGoalEntity>,
        categories: List<CategoryEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "Pocketwise")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("bankBalance", prefs.bankBalance)
        root.put("bankName", prefs.bankName)
        root.put("userName", prefs.userName)

        val txArray = JSONArray()
        transactions.forEach { tx ->
            txArray.put(JSONObject().apply {
                put("title", tx.title)
                put("amount", tx.amount)
                put("type", tx.type.name)
                put("categoryName", tx.categoryName)
                put("paymentMethod", tx.paymentMethod)
                put("timestamp", tx.timestamp)
                put("receiptImageUri", tx.receiptImageUri ?: "")
                put("notes", tx.notes)
            })
        }
        root.put("transactions", txArray)

        val goalArray = JSONArray()
        goals.forEach { goal ->
            goalArray.put(JSONObject().apply {
                put("name", goal.name)
                put("targetAmount", goal.targetAmount)
                put("allocatedAmount", goal.allocatedAmount)
                put("storeUrl", goal.storeUrl)
                put("storeLabel", goal.storeLabel)
                put("imageUri", goal.imageUri ?: "")
                put("notes", goal.notes)
                put("createdAt", goal.createdAt)
                put("isPurchased", goal.isPurchased)
            })
        }
        root.put("goals", goalArray)

        val catArray = JSONArray()
        categories.forEach { cat ->
            catArray.put(JSONObject().apply {
                put("name", cat.name)
                put("iconKey", cat.iconKey)
                put("colorHex", cat.colorHex)
                put("isDefault", cat.isDefault)
            })
        }
        root.put("categories", catArray)

        return root.toString(2)
    }

    fun parseBackupJson(rawJson: String): Result<BackupPayload> = runCatching {
        val root = JSONObject(rawJson)
        val bankBalance = root.optDouble("bankBalance", 0.0)
        val bankName = root.optString("bankName", "Commercial Bank")
        val userName = root.optString("userName", "Kasun")

        val txList = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val obj = txArray.getJSONObject(i)
            txList.add(
                TransactionEntity(
                    title = obj.getString("title"),
                    amount = obj.getDouble("amount"),
                    type = TransactionType.valueOf(obj.getString("type")),
                    categoryName = obj.optString("categoryName", "Other"),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    receiptImageUri = obj.optString("receiptImageUri").takeIf { it.isNotBlank() },
                    notes = obj.optString("notes", "")
                )
            )
        }

        val goalList = mutableListOf<SavingsGoalEntity>()
        val goalArray = root.optJSONArray("goals") ?: JSONArray()
        for (i in 0 until goalArray.length()) {
            val obj = goalArray.getJSONObject(i)
            goalList.add(
                SavingsGoalEntity(
                    name = obj.getString("name"),
                    targetAmount = obj.getDouble("targetAmount"),
                    allocatedAmount = obj.optDouble("allocatedAmount", 0.0),
                    storeUrl = obj.optString("storeUrl", ""),
                    storeLabel = obj.optString("storeLabel", ""),
                    imageUri = obj.optString("imageUri").takeIf { it.isNotBlank() },
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    isPurchased = obj.optBoolean("isPurchased", false)
                )
            )
        }

        val catList = mutableListOf<CategoryEntity>()
        val catArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catArray.length()) {
            val obj = catArray.getJSONObject(i)
            catList.add(
                CategoryEntity(
                    name = obj.getString("name"),
                    iconKey = obj.optString("iconKey", "other"),
                    colorHex = obj.optLong("colorHex", 0xFF005048L),
                    isDefault = obj.optBoolean("isDefault", false)
                )
            )
        }

        BackupPayload(
            bankBalance = bankBalance,
            bankName = bankName,
            userName = userName,
            transactions = txList,
            goals = goalList,
            categories = catList
        )
    }

    fun writeMonthlyPdfReport(
        outputStream: OutputStream,
        monthLabel: String,
        bankName: String,
        bankBalance: Double,
        totalIncome: Double,
        totalExpense: Double,
        categoryBreakdown: List<Pair<String, Double>>,
        transactions: List<TransactionEntity>
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#005048")
            textSize = 20f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#3E4947")
            textSize = 12f
        }
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#171C1F")
            textSize = 14f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#171C1F")
            textSize = 11f
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#6E7977")
            textSize = 10f
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#BEC9C6")
            strokeWidth = 1f
        }

        var y = 48f
        canvas.drawText("Pocketwise — Monthly Tax & Spend Report", 40f, y, titlePaint)
        y += 20f
        canvas.drawText("Period: $monthLabel  •  Account: $bankName  •  Currency: LKR", 40f, y, subtitlePaint)
        y += 14f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 24f

        canvas.drawText("Summary", 40f, y, sectionPaint)
        y += 18f
        canvas.drawText("Current Bank Balance: ${LkrFormatter.format(bankBalance)}", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Monthly Income: ${LkrFormatter.formatSigned(totalIncome, true)}", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Monthly Spend: ${LkrFormatter.formatSigned(totalExpense, false)}", 40f, y, bodyPaint)
        y += 16f
        val netSavings = totalIncome - totalExpense
        canvas.drawText(
            "Net Monthly Savings: ${LkrFormatter.formatSigned(netSavings, netSavings >= 0)}",
            40f,
            y,
            bodyPaint
        )
        y += 26f

        canvas.drawText("Expense Breakdown by Category", 40f, y, sectionPaint)
        y += 18f
        if (categoryBreakdown.isEmpty()) {
            canvas.drawText("No expenses recorded for this period.", 40f, y, mutedPaint)
            y += 16f
        } else {
            categoryBreakdown.forEach { (cat, sum) ->
                val pct = if (totalExpense > 0) (sum / totalExpense * 100).toInt() else 0
                canvas.drawText("• $cat ($pct%): ${LkrFormatter.format(sum)}", 48f, y, bodyPaint)
                y += 16f
            }
        }

        y += 16f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 22f
        canvas.drawText("Transactions (${transactions.size} entries)", 40f, y, sectionPaint)
        y += 18f

        transactions.take(22).forEach { tx ->
            if (y > 790f) return@forEach
            val dateStr = DateFormatters.formatShortDay(tx.timestamp)
            val amtStr = LkrFormatter.formatSigned(tx.amount, tx.type == TransactionType.INCOME)
            val titleTrunc = if (tx.title.length > 34) tx.title.take(31) + "..." else tx.title
            canvas.drawText("$dateStr  |  $titleTrunc (${tx.categoryName})", 40f, y, bodyPaint)
            canvas.drawText(amtStr, 430f, y, bodyPaint)
            y += 16f
        }

        canvas.drawText("Generated offline on-device by Pocketwise", 40f, 818f, mutedPaint)
        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
