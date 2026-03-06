package com.example.money_log.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.money_log.core.utils.ReceiptParser
import com.example.money_log.data.local.AppDatabase
import com.example.money_log.data.repository.ReceiptRepositoryImpl
import com.example.money_log.domain.model.Receipt
import com.example.money_log.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 앱의 주요 상태와 비즈니스 로직을 연결하는 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReceiptRepositoryImpl(
        AppDatabase.getDatabase(application).receiptDao()
    )

    private val getReceiptsUseCase = GetReceiptsUseCase(repository)
    private val saveReceiptUseCase = SaveReceiptUseCase(repository)
    private val deleteReceiptUseCase = DeleteReceiptUseCase(repository)
    private val getMonthlyTotalUseCase = GetMonthlyTotalUseCase(repository)

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()

    private val _monthlyTotal = MutableStateFlow(0)
    val monthlyTotal: StateFlow<Int> = _monthlyTotal.asStateFlow()

    private val _parsedReceipt = MutableStateFlow<Receipt?>(null)
    val parsedReceipt: StateFlow<Receipt?> = _parsedReceipt.asStateFlow()

    init {
        loadReceipts()
        loadMonthlyTotal()
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            getReceiptsUseCase().collect {
                _receipts.value = it
            }
        }
    }

    private fun loadMonthlyTotal() {
        viewModelScope.launch {
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            getMonthlyTotalUseCase(currentMonth).collect {
                _monthlyTotal.value = it
            }
        }
    }

    fun processOcrResult(textLines: List<String>, imagePath: String) {
        val parsedReceipt = ReceiptParser.parse(textLines, imagePath)
        _parsedReceipt.value = parsedReceipt
    }

    fun saveReceipt(receipt: Receipt) {
        viewModelScope.launch {
            saveReceiptUseCase(receipt)
            _parsedReceipt.value = null
        }
    }

    fun clearParsedReceipt() {
        _parsedReceipt.value = null
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            deleteReceiptUseCase(receipt)
        }
    }
}
