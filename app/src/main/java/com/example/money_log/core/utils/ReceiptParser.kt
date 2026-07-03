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
        
        val ignoreKeywords = listOf(
            "전화", "주소", "사업자", "대표", "Tel", "No", "영수증", "매출", 
            "전표", "고객", "신용", "가맹점", "보관", "승인", "일시", "단말기"
        )
        
        // 상호명에서 제거하면 깔끔해지는 단어 목록
        val noiseWords = listOf("주식회사", "(주)", "Corporation", "Corp.")

        for (line in lines.take(8)) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            
            // 제외 키워드가 없고 문자열 크기가 영수증 상호명으로 적합한 경우 리턴
            if (ignoreKeywords.none { trimmed.contains(it) } && trimmed.length >= 2) {
                var cleanStore = trimmed
                noiseWords.forEach { noise ->
                    cleanStore = cleanStore.replace(noise, "").trim()
                }
                if (cleanStore.isNotBlank()) {
                    return cleanStore
                }
            }
        }
        return lines[0].trim()
    }

    private fun extractAmount(text: String): Int {
        val lines = text.split("\n")
        
        // 1차 시도: 최종 금액을 뜻하는 가장 명확한 키워드들이 포함된 라인부터 정밀 탐색
        val highPriorityKeywords = listOf(
            "결제대상금액", "결제금액", "결제액", "승인금액", "청구금액", 
            "받으실금액", "받을금액", "합계금액", "총합계", "총액", "합계", 
            "TOTAL AMOUNT", "TOTAL", "AMOUNT"
        )
        val numberRegex = Regex("([\\d,]+)")

        for (keyword in highPriorityKeywords) {
            for (line in lines) {
                // 키워드를 포함하는 라인에 한해 분석 (대소문자 무시)
                if (line.contains(keyword, ignoreCase = true)) {
                    val suffix = line.substringAfter(keyword)
                    val match = numberRegex.find(suffix)
                    if (match != null) {
                        val amt = match.groupValues[1].replace(",", "").toIntOrNull() ?: 0
                        // 100원 영수증 소액 제한을 통해 수량 등급 번호(예: '2', '3') 가 최종 결제액으로 잡히는 중대 버그 차단
                        if (amt >= 100) {
                            return amt
                        }
                    }
                }
            }
        }

        // 2차 시도: 텍스트 전체에서 숫자 후보군을 모으되, 카드번호나 사업자번호 오인식을 막고자 번호류 노이즈 라인은 배제
        val excludeKeywords = listOf("번호", "사업자", "일시", "일자", "시간", "날짜", "TEL", "전화", "카드")
        val candidateRegex = Regex("(?:\\b\\d{1,3}(?:,\\d{3})+\\b|\\b\\d{3,7}\\b)")
        
        val validCandidates = mutableListOf<Int>()
        for (line in lines) {
            if (excludeKeywords.any { line.contains(it) }) continue
            
            val matches = candidateRegex.findAll(line)
            for (match in matches) {
                val amt = match.value.replace(",", "").toIntOrNull() ?: 0
                if (amt in 100..500000) { // 현실 생활비 영수증 한도 (100원 ~ 50만원)
                    validCandidates.add(amt)
                }
            }
        }

        return validCandidates.maxOrNull() ?: 0
    }

    private fun extractDate(text: String): String {
        // YYYY-MM-DD, YY/MM/DD 포맷 및 YYYY년 MM월 DD일 한글 포맷을 망라하는 정규식 목록
        val regexes = listOf(
            Regex("(\\d{4}|\\d{2})[-./\\s]+(\\d{1,2})[-./\\s]+(\\d{1,2})"),
            Regex("(\\d{4})년\\s*(\\d{1,2})월\\s*(\\d{1,2})일")
        )

        for (regex in regexes) {
            val matches = regex.findAll(text)
            for (match in matches) {
                val yearGroup = match.groupValues[1]
                val monthGroup = match.groupValues[2]
                val dayGroup = match.groupValues[3]

                val year = if (yearGroup.length == 2) "20$yearGroup" else yearGroup
                val monthVal = monthGroup.toIntOrNull() ?: 0
                val dayVal = dayGroup.toIntOrNull() ?: 0
                val yearVal = year.toIntOrNull() ?: 0

                // 범위 유효성 검사 (월: 1..12, 일: 1..31, 연도: 2000..2099)
                // 이를 통해 사업자등록번호(예: 119-85-09)에서 '85'월 같이 부적합한 숫자가 걸리는 것을 필터링
                if (monthVal in 1..12 && dayVal in 1..31 && yearVal in 2000..2099) {
                    val monthStr = monthGroup.padStart(2, '0')
                    val dayStr = dayGroup.padStart(2, '0')
                    return "$year-$monthStr-$dayStr"
                }
            }
        }

        // 유효한 날짜가 끝내 검출되지 않았을 경우 오늘 날짜를 기본값으로 제공
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
            storeName.contains("다이소") || storeName.contains("올리브영") -> "생활"
            else -> "기타"
        }
    }
}
