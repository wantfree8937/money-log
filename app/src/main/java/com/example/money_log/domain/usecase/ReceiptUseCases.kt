package com.example.money_log.domain.usecase

import com.example.money_log.domain.model.Receipt
import com.example.money_log.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow

/**
 * 모든 영수증 목록을 가져오는 유스케이스
 */
class GetReceiptsUseCase(private val repository: ReceiptRepository) {
    operator fun invoke(): Flow<List<Receipt>> = repository.getAllReceipts()
}

/**
 * 영수증을 저장하는 유스케이스
 */
class SaveReceiptUseCase(private val repository: ReceiptRepository) {
    suspend operator fun invoke(receipt: Receipt) = repository.saveReceipt(receipt)
}

/**
 * 영수증을 삭제하는 유스케이스
 */
class DeleteReceiptUseCase(private val repository: ReceiptRepository) {
    suspend operator fun invoke(receipt: Receipt) = repository.deleteReceipt(receipt)
}

/**
 * 월간 총 지출액을 가져오는 유스케이스
 */
class GetMonthlyTotalUseCase(private val repository: ReceiptRepository) {
    operator fun invoke(month: String): Flow<Int> = repository.getMonthlyTotal(month)
}
