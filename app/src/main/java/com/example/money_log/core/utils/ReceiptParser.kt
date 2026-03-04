package com.example.money_log.core.utils

import com.example.money_log.domain.model.Receipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OCR 결과 텍스트를 분석하여 영수증 정보를 추출하는 유틸리티
 */
object ReceiptParser {

    fun parse(textLines: List<String>, imagePath: String): Receipt {
        val fullText = textLines.joinToString("\n")
        
        val storeName = extractStoreName(textLines)
        val amount = extractAmount(fullText)
        val date = extractDate(fullText)
        val paymentMethod = extractPaymentMethod(fullText)
        val category = classifyCategory(storeName)

        return Receipt(
            storeName = storeName,
            amount = amount,
            date = date,
            category = category,
            paymentMethod = paymentMethod,
            imagePath = imagePath
        )
    }

    private fun extractStoreName(lines: List<String>): String {
        if (lines.isEmpty()) return "알 수 없는 상호"
        
        val ignoreKeywords = listOf("전화", "주소", "사업자", "대표", "Tel", "No")
        for (line in lines.take(5)) {
            if (line.isBlank()) continue
            if (ignoreKeywords.none { line.contains(it) }) {
                return line.trim()
            }
        }
        return lines[0].trim()
    }

    private fun extractAmount(text: String): Int {
        val patterns = listOf(
            Regex("(?:합계|결제금액|금액|TOTAL|AMOUNT)[:\\s]*([\\d,]+)"),
            Regex("([\\d,]+)(?:\\s*원)")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                return amountStr.toIntOrNull() ?: 0
            }
        }

        val allNumbers = Regex("[\\d,]{4,10}").findAll(text)
            .map { it.value.replace(",", "").toIntOrNull() ?: 0 }
            .toList()
        
        return allNumbers.maxOrNull() ?: 0
    }

    private fun extractDate(text: String): String {
        val dateRegex = Regex("(\\d{2,4})[-\\./](\\d{1,2})[-\\./](\\d{1,2})")
        val match = dateRegex.find(text)
        
        if (match != null) {
            val year = match.groupValues[1].let { if (it.length == 2) "20$it" else it }
            val month = match.groupValues[2].padStart(2, '0')
            val day = match.groupValues[3].padStart(2, '0')
            return "$year-$month-$day"
        }

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun extractPaymentMethod(text: String): String {
        return when {
            text.contains("신용") || text.contains("카드") || text.contains("CARD") -> "카드"
            text.contains("현금") || text.contains("CASH") -> "현금"
            else -> "기타"
        }
    }

    private fun classifyCategory(storeName: String): String {
        return when {
            storeName.contains("편의점") || storeName.contains("마트") || storeName.contains("식당") || storeName.contains("밥") -> "식비"
            storeName.contains("카페") || storeName.contains("커피") || storeName.contains("TEA") -> "식비"
            storeName.contains("병원") || storeName.contains("약국") || storeName.contains("의원") -> "의료"
            storeName.contains("택시") || storeName.contains("버스") || storeName.contains("역") || storeName.contains("주유") -> "교통"
            storeName.contains("다이소") || storeName.contains("올리브영") -> "생활용품"
            else -> "기타"
        }
    }
}
