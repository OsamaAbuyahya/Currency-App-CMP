package org.example.project.presentation.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.example.project.data.local.MongoImpl
import org.example.project.domain.CurrencyApiService
import org.example.project.domain.MongoRepository
import org.example.project.domain.PreferenceRepository
import org.example.project.domain.model.Currency
import org.example.project.domain.model.CurrencyCode
import org.example.project.domain.model.RateStatus
import org.example.project.domain.model.RequestState

sealed class HomeUiEvent{
    data object RefreshRates: HomeUiEvent()
    data object SwitchCurrencies: HomeUiEvent()
    data class SaveSourceCurrencyCode(val currencyCode: String): HomeUiEvent()
    data class SaveTargetCurrencyCode(val currencyCode: String): HomeUiEvent()
}

class HomeViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val mongoRepository: MongoRepository,
    private val currencyApiService: CurrencyApiService
): ScreenModel {
    private val _rateStatus: MutableState<RateStatus> = mutableStateOf(RateStatus.Idle)
    val rateStatus: State<RateStatus> = _rateStatus

    private val _sourceCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val sourceCurrency: State<RequestState<Currency>> = _sourceCurrency

    private val _targetCurrency: MutableState<RequestState<Currency>> = mutableStateOf(RequestState.Idle)
    val targetCurrency: State<RequestState<Currency>> = _targetCurrency

    private val _allCurrencies = mutableStateListOf<Currency>()
    val allCurrencies: List<Currency> = _allCurrencies

    init {
        screenModelScope.launch {
            fetchNewRates()
            readSourceCurrency()
            readTargetCurrency()
        }
    }

    fun sendEvent(event: HomeUiEvent) {
        when(event) {
            HomeUiEvent.RefreshRates -> {
                screenModelScope.launch {
                    fetchNewRates()
                }
            }
            HomeUiEvent.SwitchCurrencies -> {
                switchCurrencies()
            }
            is HomeUiEvent.SaveSourceCurrencyCode -> {
                saveSourceCurrencyCode(event.currencyCode)
            }
            is HomeUiEvent.SaveTargetCurrencyCode -> {
                saveTargetCurrencyCode(event.currencyCode)
            }
        }
    }

    private fun switchCurrencies() {
        val source = _sourceCurrency.value
        val target = _targetCurrency.value
        _sourceCurrency.value = target
        _targetCurrency.value = source
    }

    fun readSourceCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferenceRepository.readSourceCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name}
                if (selectedCurrency != null) {
                    _sourceCurrency.value = RequestState.Success(selectedCurrency)
                } else {
                    _sourceCurrency.value = RequestState.Error("Could not find the selected currency.")
                }
            }

        }
    }

    fun readTargetCurrency() {
        screenModelScope.launch(Dispatchers.Main) {
            preferenceRepository.readTargetCurrencyCode().collectLatest { currencyCode ->
                val selectedCurrency = _allCurrencies.find { it.code == currencyCode.name}
                if (selectedCurrency != null) {
                    _targetCurrency.value = RequestState.Success(selectedCurrency)
                } else {
                    _targetCurrency.value = RequestState.Error("Could not find the selected currency.")
                }
            }

        }
    }

    private suspend fun fetchNewRates() {
        try {
            val localCacheCurrencyData = mongoRepository.readCurrencyData().first()
            println("HomeViewModel: local cache fetch success")
            if (localCacheCurrencyData.isSuccess()) {
                if (localCacheCurrencyData.getSuccessData().isNotEmpty()) {
                    println("HomeViewModel: DATABASE IS FULL")
                    if (!preferenceRepository.isDataFresh(Clock.System.now().toEpochMilliseconds())) {
                        println("HomeViewModel: DATA IS NOT FRESH")
                        cacheTheData()
                    } else {
                        println("HomeViewModel: DATA IS FRESH")
                        _allCurrencies.clear()
                        _allCurrencies.addAll(localCacheCurrencyData.getSuccessData())                    }
                } else {
                    println("HomeViewModel: DATABASE NEEDS DATA")
                    cacheTheData()
                }
            } else if (localCacheCurrencyData.isError()) {
                println("HomeViewModel: ERROR READING LOCAL DATABASE ${localCacheCurrencyData.getErrorMessage()}")
            }
            getRatesStatus()
        } catch (e: Exception) {
            println("🚨 Exception in fetchNewRates: ${e.message}")
            println(e.message)
        }
    }

    private suspend fun cacheTheData() {
        val fetchedData = currencyApiService.getLatestExchangeRates()
        if (fetchedData.isSuccess()) {
            mongoRepository.cleanUp()
            fetchedData.getSuccessData().forEach {
                println("HomeViewModel: ADDING ${it.code}")
                mongoRepository.insertCurrencyData(it)
            }
            println("HomeViewModel: UPDATING _allCurrencies")
            _allCurrencies.clear()
            _allCurrencies.addAll(fetchedData.getSuccessData())
        } else if (fetchedData.isError()) {
            println("HomeViewModel: FETCHING FAILED: ${fetchedData.getErrorMessage()}")
        }
    }

    private suspend fun getRatesStatus() {
        _rateStatus.value = if (preferenceRepository.isDataFresh(
            currentTimestamp = Clock.System.now().toEpochMilliseconds()
        )) RateStatus.Fresh else RateStatus.Stale
    }

    private fun saveSourceCurrencyCode(currencyCode: String) {
        screenModelScope.launch {
            preferenceRepository.saveSourceCurrencyCode(currencyCode)
        }
    }

    private fun saveTargetCurrencyCode(currencyCode: String) {
        screenModelScope.launch {
            preferenceRepository.saveTargetCurrencyCode(currencyCode)
        }
    }
}