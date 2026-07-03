package com.example.money_log

import com.example.money_log.core.utils.ReceiptParser
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * ReceiptParser의 최종금액 100원 미만 이상 기각 필터 및 카메라 가이드 동조 크롭 검증용 테스트 데시벨
 */
class ReceiptParserTest {

    @Test
    fun testAmount_ignoresSmallNumberNoiseOnKeywords() {
        // "합계금액 2" 같이 키워드 우측에 2장, 2개 같은 소규모 수량 노이즈가 걸렸을 때
        // 100원 미만으로 기각하여 다음 유효한 고액 숫자를 타당하게 파싱하는지 확인
        val lines = listOf(
            "식사 합계 2", // 100원 미만이므로 기각 대상
            "카드 결제금액 12,500원", // 12500원이 나와야 함
            "승인번호 9827361"
        )
        val receipt = ReceiptParser.parse(lines, "dummy")
        assertEquals(12500, receipt.amount)
    }

    @Test
    fun testAmount_takesMaxCandidateExcludingNoiseLines() {
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
