package com.example.money_log.domain.repository

import com.example.money_log.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

/**
 * 영수증 데이터 처리를 위한 레포지토리 인터페이스
 */
interface ReceiptRepository {
    fun getAllReceipts(): Flow<List<Receipt>>
    suspend fun saveReceipt(receipt: Receipt)
    suspend fun deleteReceipt(receipt: Receipt)
    suspend fun getReceiptById(id: Int): Receipt?
    fun getTotalInDateRange(startDate: String, endDate: String): Flow<Int>
}
