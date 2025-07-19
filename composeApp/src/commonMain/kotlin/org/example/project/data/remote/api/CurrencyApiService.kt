package org.example.project.data.remote.api

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.domain.CurrencyApiService
import org.example.project.domain.PreferenceRepository
import org.example.project.domain.model.ApiResponse
import org.example.project.domain.model.Currency
import org.example.project.domain.model.CurrencyCode
import org.example.project.domain.model.RequestState


class CurrencyApiServiceImpl(
    private val preferenceRepository: PreferenceRepository
) : CurrencyApiService {
    companion object {
        private const val ENDPOINT = " https://api.currencyapi.com/v3/latest"
        private const val API_KEY = "cur_live_RTa1UCraLaXltMyhaOKOzXqsbwmVT8SFVNLB3xfC"

    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
        }

        install(DefaultRequest) {
            headers {
                append("apikey", API_KEY)
            }
        }
    }

    override suspend fun getLatestExchangeRates(): RequestState<List<Currency>> {
        return try {
            val response = httpClient.get(ENDPOINT)
            if (response.status.value == 200) {
                println("API Response ${response.body<String>()}")
                val apiResponse = Json.decodeFromString<ApiResponse>(response.body())

                val availableCurrencyCodes = apiResponse.data.keys
                    .filter {
                        CurrencyCode.entries
                            .map { code -> code.name }
                            .toSet()
                            .contains(it)
                    }

                val availableCurrencies = apiResponse.data.values
                    .filter { currency ->
                        availableCurrencyCodes.contains(currency.code)
                    }

                // Persist a timestamp
                val lastUpdated = apiResponse.meta.lastUpdatedAt
                preferenceRepository.saveLatestUpdate(lastUpdated)

                RequestState.Success(data = availableCurrencies)
            } else {
                RequestState.Error(message = "HTTP Error Code: ${response.status.value} ${response.status.description}")
            }
        } catch (e: Exception) {
            RequestState.Error(message = e.message.toString())
        }
    }
}