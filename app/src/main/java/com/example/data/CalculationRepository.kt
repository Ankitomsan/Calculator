package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val allHistory: Flow<List<CalculationRecord>> = dao.getAllRecords()

    suspend fun saveCalculation(expression: String, result: String): Long {
        return dao.insertRecord(
            CalculationRecord(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteRecordById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllRecords()
    }
}
