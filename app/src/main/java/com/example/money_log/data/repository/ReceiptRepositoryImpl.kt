package com.example.money_log.data.repository

import com.example.money_log.data.local.ReceiptDao
import com.example.money_log.data.local.ReceiptEntity
import com.example.money_log.domain.model.Receipt
import com.example.money_log.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ReceiptRepository의 실제 구현체
 */
class ReceiptRepositoryImpl(
    private val dao: ReceiptDao
) : ReceiptRepository {

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return dao.getAllReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveReceipt(receipt: Receipt) {
        dao.insertReceipt(receipt.toEntity())
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        dao.deleteReceipt(receipt.toEntity())
    }

    override suspend fun getReceiptById(id: Int): Receipt? {
        return dao.getReceiptById(id)?.toDomain()
    }

    override fun getMonthlyTotal(month: String): Flow<Int> {
        return dao.getMonthlyTotal(month).map { it ?: 0 }
    }

    // Mapper functions
    private fun ReceiptEntity.toDomain(): Receipt = Receipt(
        id = id,
        storeName = storeName,
        amount = amount,
        date = date,
        category = category,
        paymentMethod = paymentMethod,
        imagePath = imagePath,
        createdAt = createdAt
    )

    private fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
        id = id,
        storeName = storeName,
        amount = amount,
        date = date,
        category = category,
        paymentMethod = paymentMethod,
        imagePath = imagePath,
        createdAt = createdAt
    )
}
