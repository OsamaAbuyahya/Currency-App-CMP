package org.example.project.data.local

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.example.project.domain.MongoRepository
import org.example.project.domain.mapper.toCurrency
import org.example.project.domain.mapper.toEntity
import org.example.project.domain.model.Currency
import org.example.project.domain.model.CurrencyEntity
import org.example.project.domain.model.RequestState

class MongoImpl : MongoRepository {
    private var realm: Realm? = null

    init {
        configurationRealm()
    }

    override fun configurationRealm() {
        if (realm == null || realm?.isClosed() == true) {
            val config = RealmConfiguration.Builder(
                schema = setOf(CurrencyEntity::class)
            ).compactOnLaunch().build()
            realm = Realm.open(config)
        }
    }

    override suspend fun insertCurrencyData(currencyData: Currency) {
        val currencyEntity = currencyData.toEntity()
        realm?.write {
            copyToRealm(currencyEntity)
        }
    }

    override fun readCurrencyData(): Flow<RequestState<List<Currency>>> {
        return realm?.query<CurrencyEntity>()
            ?.asFlow()
            ?.map { result ->
                RequestState.Success(data = result.list.map { it.toCurrency() })
            }
            ?: flow { RequestState.Error(message = "realm is not configred") }
    }

    override suspend fun cleanUp() {
        realm?.write {
            val currencies = query<CurrencyEntity>()
            delete(currencies)
        }
    }
}