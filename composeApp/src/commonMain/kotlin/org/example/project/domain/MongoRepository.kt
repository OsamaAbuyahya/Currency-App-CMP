package org.example.project.domain

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.Currency
import org.example.project.domain.model.RequestState

interface MongoRepository {

    fun configurationRealm()
    suspend fun insertCurrencyData(currencyData: Currency)
    fun readCurrencyData(): Flow<RequestState<List<Currency>>>
    suspend fun cleanUp()

}