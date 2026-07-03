package com.example.money_log

import com.example.money_log.core.utils.ReceiptParser
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ReceiptParser의 최종금액 감지률 2차 고도화(라인 단위, 노이즈 제외 기법)를 검증하는 테스트 코드
 */
class ReceiptParserTest {

    @Test
    fun testAmount_parsesLinesWithVariousFormats() {
        val lines1 = listOf("합계금액   : ₩12,500원", "승인번호: 9817265")
        val receipt1 = ReceiptParser.parse(lines1, "dummy")
        assertEquals(12500, receipt1.amount)

        val lines2 = listOf("결제액: 15,000", "거래일자: 2026.03.07")
        val receipt2 = ReceiptParser.parse(lines2, "dummy")
        assertEquals(15000, receipt2.amount)
    }

    @Test
    fun testAmount_takesMaxCandidateExcludingNoiseLines() {
        // 사업자등록번호(120-81-12345), 승인번호(9827361) 요소를 온전히 거르고, 
        // 100~500,000 범위 내의 단독 유효 금액 필드인 1,500원을 정확히 찾아내는지 검증
        val lines = listOf(
            "사업자번호 120-81-12345",
            "승인번호 9827361",
            "식사 금액 1,500",
            "카드 일시: 2026/07/03"
        )
        val receipt = ReceiptParser.parse(lines, "dummy")
        assertEquals(1500, receipt.amount)
    }

    @Test
    fun testDate_supportsDotsAndKoreanStyle() {
        val lines1 = listOf("날짜: 2026.03.07", "합계: 3,000")
        val receipt1 = ReceiptParser.parse(lines1, "dummy")
        assertEquals("2026-03-07", receipt1.date)

        val lines2 = listOf("일시: 2026. 03. 07 14:30", "금액: 3,000")
        val receipt2 = ReceiptParser.parse(lines2, "dummy")
        assertEquals("2026-03-07", receipt2.date)

        val lines3 = listOf("날짜: 2026년 03월 07일", "합계: 3,000")
        val receipt3 = ReceiptParser.parse(lines3, "dummy")
        assertEquals("2026-03-07", receipt3.date)
    }

    @Test
    fun testDateValidation_ignoresBusinessRegistrationNumber() {
        val lines = listOf(
            "등록번호: 119-85-09123",
            "매출일자: 2026.03.07",
            "결제금액: 5,000"
        )
        val receipt = ReceiptParser.parse(lines, "dummy")
        assertEquals("2026-03-07", receipt.date)
    }
}
