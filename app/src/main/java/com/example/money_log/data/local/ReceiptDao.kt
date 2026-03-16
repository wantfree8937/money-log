package com.example.money_log.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 영수증 데이터에 접근하기 위한 DAO 인터페이스
 */
@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: Int): ReceiptEntity?

    @Query("SELECT SUM(amount) FROM receipts WHERE date >= :startDate AND date <= :endDate")
    fun getTotalInDateRange(startDate: String, endDate: String): Flow<Int?>
}
