package com.example.money_log.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 데이터베이스에 저장될 영수증 정보 테이블 정의
 */
@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val storeName: String,
    val amount: Int,
    val date: String,
    val category: String,
    val paymentMethod: String,
    val imagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
