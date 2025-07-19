package org.example.project.domain

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.CurrencyCode

interface PreferenceRepository {

    suspend fun saveLatestUpdate(latestUpdate: String)
    suspend fun isDataFresh(currentTimestamp: Long): Boolean

    suspend fun saveSourceCurrencyCode(code: String)
    suspend fun saveTargetCurrencyCode(code: String)

    fun readSourceCurrencyCode(): Flow<CurrencyCode>
    fun readTargetCurrencyCode(): Flow<CurrencyCode>

}