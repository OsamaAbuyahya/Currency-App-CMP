package org.example.project.domain

import org.example.project.domain.model.Currency
import org.example.project.domain.model.RequestState

interface CurrencyApiService {

    suspend fun getLatestExchangeRates(): RequestState<List<Currency>>
}