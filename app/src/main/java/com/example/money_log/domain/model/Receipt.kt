package com.example.money_log.domain.model

/**
 * 도메인 레이어에서 사용하는 영수증 데이터 모델
 */
data class Receipt(
    val id: Int = 0,
    val storeName: String,
    val amount: Int,
    val date: String,
    val category: String,
    val paymentMethod: String,
    val imagePath: String,
    val createdAt: Long = 0L
)
